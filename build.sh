#!/bin/bash

echo "=========================================="
echo "  MSA Platform 전체 빌드"
echo "=========================================="

REGISTRY="${DOCKER_REGISTRY:-msa-platform}"
TAG="${BUILD_TAG:-latest}"

SERVICES=(
    "gateway"
    "auth-service"
    "user-service"
    "bookmark-service"
    "schedule-service"
    "ticketing-service"
    "book-service"
    "travel-service"
    "festival-service"
    "wedding-service"
)

# 1. common-lib 빌드 & 배포
echo "[0/${#SERVICES[@]}] common-lib 빌드..."
cd common-lib
./gradlew publishToMavenLocal
if [ $? -ne 0 ]; then
    echo "common-lib 빌드 실패!"
    exit 1
fi
cd ..

# 2. 각 서비스 빌드
for i in "${!SERVICES[@]}"; do
    svc="${SERVICES[$i]}"
    num=$((i + 1))

    echo ""
    echo "[${num}/${#SERVICES[@]}] ${svc} 빌드 중..."
    echo "----------------------------------------"

    cd "$svc"

    # Gradle 빌드
    ./gradlew clean build -x test
    if [ $? -ne 0 ]; then
        echo "${svc} Gradle 빌드 실패!"
        exit 1
    fi

    # Docker 이미지 빌드
    docker build -t "${REGISTRY}/${svc}:${TAG}" .
    if [ $? -ne 0 ]; then
        echo "${svc} Docker 빌드 실패!"
        exit 1
    fi

    echo "${svc} 완료!"
    cd ..
done

echo ""
echo "=========================================="
echo "  빌드 완료!"
echo "=========================================="
echo ""
echo "생성된 이미지:"
for svc in "${SERVICES[@]}"; do
    echo "  - ${REGISTRY}/${svc}:${TAG}"
done

echo ""
read -p "Docker Registry에 푸시할까요? (y/N): " confirm
if [[ "$confirm" =~ ^[Yy]$ ]]; then
    echo ""
    echo "푸시 중..."
    for svc in "${SERVICES[@]}"; do
        echo "  - ${REGISTRY}/${svc}:${TAG}"
        docker push "${REGISTRY}/${svc}:${TAG}"
    done
    echo "푸시 완료!"
fi
