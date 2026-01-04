#!/bin/bash

echo "=========================================="
echo "  MSA Platform Docker 이미지 푸시"
echo "=========================================="

# Nexus Docker Registry 설정
NEXUS_HOST="${NEXUS_HOST:-100.86.217.36}"
NEXUS_PORT="${NEXUS_PORT:-30500}"
REGISTRY="${NEXUS_HOST}:${NEXUS_PORT}"
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

echo ""
echo "Registry: ${REGISTRY}"
echo "Tag: ${TAG}"
echo ""

# insecure registry 설정 안내
echo "----------------------------------------"
echo "⚠️  Insecure Registry 설정 필요"
echo "----------------------------------------"
echo ""
echo "Docker Desktop (Windows/Mac):"
echo "  Settings → Docker Engine → 아래 내용 추가:"
echo '  {"insecure-registries": ["'${REGISTRY}'"]}'
echo ""
echo "Linux (/etc/docker/daemon.json):"
echo '  {"insecure-registries": ["'${REGISTRY}'"]}'
echo "  sudo systemctl restart docker"
echo ""
echo "----------------------------------------"

# Registry 연결 테스트
echo ""
echo "Registry 연결 테스트 중..."
if curl -s --connect-timeout 5 "http://${REGISTRY}/v2/" > /dev/null 2>&1; then
    echo "✅ Registry 연결 성공"
else
    echo "❌ Registry 연결 실패: ${REGISTRY}"
    echo ""
    echo "확인 사항:"
    echo "  1. Nexus 서버가 실행 중인지 확인"
    echo "  2. 방화벽/포트 확인 (${NEXUS_PORT})"
    echo "  3. insecure-registries 설정 후 Docker 재시작"
    echo ""
    read -p "그래도 푸시를 시도할까요? (y/N): " force
    if [[ ! "$force" =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 로컬 이미지 확인
echo ""
echo "로컬 이미지 확인 중..."
MISSING_IMAGES=()
for svc in "${SERVICES[@]}"; do
    if ! docker image inspect "${REGISTRY}/${svc}:${TAG}" > /dev/null 2>&1; then
        MISSING_IMAGES+=("$svc")
    fi
done

if [ ${#MISSING_IMAGES[@]} -gt 0 ]; then
    echo ""
    echo "⚠️  다음 이미지가 로컬에 없습니다:"
    for img in "${MISSING_IMAGES[@]}"; do
        echo "  - ${REGISTRY}/${img}:${TAG}"
    done
    echo ""
    echo "먼저 build.sh를 실행하세요."
    exit 1
fi

echo "✅ 모든 이미지 확인됨"

# 푸시 실행
echo ""
echo "=========================================="
echo "  Nexus Registry에 푸시 시작"
echo "=========================================="

FAILED=()
SUCCESS=()

for i in "${!SERVICES[@]}"; do
    svc="${SERVICES[$i]}"
    num=$((i + 1))

    echo ""
    echo "[${num}/${#SERVICES[@]}] ${svc} 푸시 중..."

    if docker push "${REGISTRY}/${svc}:${TAG}"; then
        echo "✅ ${svc} 푸시 성공"
        SUCCESS+=("$svc")
    else
        echo "❌ ${svc} 푸시 실패"
        FAILED+=("$svc")
    fi
done

# 결과 요약
echo ""
echo "=========================================="
echo "  푸시 결과"
echo "=========================================="
echo ""
echo "✅ 성공: ${#SUCCESS[@]}개"
for svc in "${SUCCESS[@]}"; do
    echo "  - ${REGISTRY}/${svc}:${TAG}"
done

if [ ${#FAILED[@]} -gt 0 ]; then
    echo ""
    echo "❌ 실패: ${#FAILED[@]}개"
    for svc in "${FAILED[@]}"; do
        echo "  - ${REGISTRY}/${svc}:${TAG}"
    done
    echo ""
    echo "실패 원인 확인:"
    echo "  1. insecure-registries 설정 확인"
    echo "  2. Docker 재시작 여부"
    echo "  3. Nexus 서버 상태"
    exit 1
fi

echo ""
echo "🎉 모든 이미지 푸시 완료!"
