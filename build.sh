#!/bin/bash

echo "=========================================="
echo "  MSA Platform 멀티 아키텍처 빌드"
echo "=========================================="

# Nexus Docker Registry 설정
NEXUS_HOST="${NEXUS_HOST:-100.67.243.29}"
NEXUS_PORT="${NEXUS_PORT:-30500}"
REGISTRY="${NEXUS_HOST}:${NEXUS_PORT}"
TAG="${BUILD_TAG:-latest}"
PLATFORMS="${PLATFORMS:-linux/amd64,linux/arm64}"
BUILDER_NAME="msa-multiarch-builder"

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
echo "Platforms: ${PLATFORMS}"
echo ""

# buildx 빌더 확인 및 생성
echo "----------------------------------------"
echo "  Buildx 설정"
echo "----------------------------------------"
if ! docker buildx inspect ${BUILDER_NAME} > /dev/null 2>&1; then
    echo "멀티 아키텍처 빌더 생성 중..."
    docker buildx create --name ${BUILDER_NAME} --driver docker-container --use
    docker buildx inspect --bootstrap
else
    docker buildx use ${BUILDER_NAME}
fi
echo "✅ Buildx 준비 완료"
echo ""

# insecure registry 설정 안내
echo "----------------------------------------"
echo "⚠️  Insecure Registry 설정 필요"
echo "----------------------------------------"
echo ""
echo "buildx에서 insecure registry 사용하려면:"
echo ""
echo "1. /etc/docker/daemon.json:"
echo '   {"insecure-registries": ["'${REGISTRY}'"]}'
echo ""
echo "2. buildx 설정 (~/.docker/buildx/buildkitd.toml):"
echo '   [registry."'${REGISTRY}'"]'
echo '     http = true'
echo '     insecure = true'
echo ""
echo "----------------------------------------"

# 1. common-lib 빌드 & 배포
echo ""
echo "[0/${#SERVICES[@]}] common-lib 빌드..."
cd common-lib
./gradlew publishToMavenLocal
if [ $? -ne 0 ]; then
    echo "❌ common-lib 빌드 실패!"
    exit 1
fi
cd ..
echo "✅ common-lib 완료"

# 2. 각 서비스 빌드 (Gradle만)
echo ""
echo "=========================================="
echo "  Gradle 빌드"
echo "=========================================="
for i in "${!SERVICES[@]}"; do
    svc="${SERVICES[$i]}"
    num=$((i + 1))

    echo ""
    echo "[${num}/${#SERVICES[@]}] ${svc} Gradle 빌드 중..."

    cd "$svc"
    ./gradlew clean build -x test
    if [ $? -ne 0 ]; then
        echo "❌ ${svc} Gradle 빌드 실패!"
        exit 1
    fi
    echo "✅ ${svc} Gradle 완료"
    cd ..
done

# 3. Docker 멀티 아키텍처 빌드 & 푸시
echo ""
echo "=========================================="
echo "  Docker 멀티 아키텍처 빌드 & 푸시"
echo "=========================================="
echo ""
echo "⚠️  buildx는 멀티 아키텍처 빌드 시 직접 레지스트리에 푸시합니다."
read -p "계속 진행할까요? (y/N): " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo "취소됨"
    exit 0
fi

FAILED=()
SUCCESS=()

for i in "${!SERVICES[@]}"; do
    svc="${SERVICES[$i]}"
    num=$((i + 1))

    echo ""
    echo "[${num}/${#SERVICES[@]}] ${svc} 멀티 아키텍처 빌드 & 푸시 중..."
    echo "  Platforms: ${PLATFORMS}"
    echo "  Image: ${REGISTRY}/${svc}:${TAG}"
    echo "----------------------------------------"

    cd "$svc"

    docker buildx build \
        --platform ${PLATFORMS} \
        --tag "${REGISTRY}/${svc}:${TAG}" \
        --push \
        .

    if [ $? -ne 0 ]; then
        echo "❌ ${svc} 빌드/푸시 실패!"
        FAILED+=("$svc")
    else
        echo "✅ ${svc} 완료!"
        SUCCESS+=("$svc")
    fi
    cd ..
done

# 결과 요약
echo ""
echo "=========================================="
echo "  빌드 결과"
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
        echo "  - ${svc}"
    done
    echo ""
    echo "실패 원인 확인:"
    echo "  1. insecure-registries 설정"
    echo "  2. buildkitd.toml 설정"
    echo "  3. Nexus 서버 상태"
    exit 1
fi

echo ""
echo "🎉 모든 이미지 멀티 아키텍처 빌드 & 푸시 완료!"
echo ""
echo "지원 아키텍처: ${PLATFORMS}"
