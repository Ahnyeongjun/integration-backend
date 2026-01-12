import os
import uuid
from fastapi import FastAPI, UploadFile, File, HTTPException, Query
from fastapi.responses import JSONResponse
from minio import Minio
from minio.error import S3Error
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="Image Service", version="1.0.0")

# MinIO 설정
MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "localhost:9000")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minio")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minio123!")
MINIO_BUCKET = os.getenv("MINIO_BUCKET", "images")
MINIO_SECURE = os.getenv("MINIO_SECURE", "false").lower() == "true"

# MinIO 클라이언트
minio_client = Minio(
    MINIO_ENDPOINT,
    access_key=MINIO_ACCESS_KEY,
    secret_key=MINIO_SECRET_KEY,
    secure=MINIO_SECURE
)


def ensure_bucket_exists():
    """버킷이 없으면 생성하고 public 정책 설정"""
    try:
        if not minio_client.bucket_exists(MINIO_BUCKET):
            minio_client.make_bucket(MINIO_BUCKET)
            # Public read 정책
            policy = f'''{{
                "Version": "2012-10-17",
                "Statement": [{{
                    "Effect": "Allow",
                    "Principal": {{"AWS": ["*"]}},
                    "Action": ["s3:GetObject"],
                    "Resource": ["arn:aws:s3:::{MINIO_BUCKET}/*"]
                }}]
            }}'''
            minio_client.set_bucket_policy(MINIO_BUCKET, policy)
    except S3Error as e:
        print(f"Bucket setup error: {e}")


@app.on_event("startup")
async def startup():
    ensure_bucket_exists()


@app.get("/health")
async def health():
    return {"status": "healthy"}


@app.post("/upload")
async def upload_image(
    file: UploadFile = File(...),
    directory: str = Query(default="", description="저장 디렉토리")
):
    """이미지 업로드"""
    # 파일 확장자 추출
    ext = file.filename.split(".")[-1] if "." in file.filename else "jpg"

    # 객체 이름 생성
    object_name = f"{directory}/{uuid.uuid4()}.{ext}" if directory else f"{uuid.uuid4()}.{ext}"

    try:
        # MinIO에 업로드
        file_data = await file.read()
        from io import BytesIO
        minio_client.put_object(
            MINIO_BUCKET,
            object_name,
            BytesIO(file_data),
            length=len(file_data),
            content_type=file.content_type or "application/octet-stream"
        )

        return {
            "success": True,
            "data": {
                "objectName": object_name,
                "url": f"/{MINIO_BUCKET}/{object_name}"
            }
        }
    except S3Error as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/upload/multiple")
async def upload_multiple_images(
    files: list[UploadFile] = File(...),
    directory: str = Query(default="", description="저장 디렉토리")
):
    """다중 이미지 업로드"""
    results = []

    for file in files:
        ext = file.filename.split(".")[-1] if "." in file.filename else "jpg"
        object_name = f"{directory}/{uuid.uuid4()}.{ext}" if directory else f"{uuid.uuid4()}.{ext}"

        try:
            file_data = await file.read()
            from io import BytesIO
            minio_client.put_object(
                MINIO_BUCKET,
                object_name,
                BytesIO(file_data),
                length=len(file_data),
                content_type=file.content_type or "application/octet-stream"
            )
            results.append({
                "objectName": object_name,
                "url": f"/{MINIO_BUCKET}/{object_name}"
            })
        except S3Error as e:
            results.append({
                "error": str(e),
                "filename": file.filename
            })

    return {"success": True, "data": results}


@app.delete("/delete")
async def delete_image(object_name: str = Query(..., description="삭제할 객체명")):
    """이미지 삭제"""
    try:
        minio_client.remove_object(MINIO_BUCKET, object_name)
        return {"success": True, "message": "삭제 완료"}
    except S3Error as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/presigned-url")
async def get_presigned_url(
    filename: str = Query(..., description="파일명"),
    directory: str = Query(default="", description="디렉토리"),
    expires: int = Query(default=3600, description="만료 시간(초)")
):
    """Presigned URL 발급 (직접 업로드용)"""
    from datetime import timedelta

    object_name = f"{directory}/{filename}" if directory else filename

    try:
        url = minio_client.presigned_put_object(
            MINIO_BUCKET,
            object_name,
            expires=timedelta(seconds=expires)
        )
        return {
            "success": True,
            "data": {
                "objectName": object_name,
                "uploadUrl": url,
                "accessUrl": f"/{MINIO_BUCKET}/{object_name}"
            }
        }
    except S3Error as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8100)
