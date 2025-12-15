#!/bin/bash

echo "=========================================="
echo "  MSA Platform 초기 설정"
echo "=========================================="

# 필수 프로그램 체크
echo ""
echo "[체크] 필수 프로그램 확인..."

# Java
if command -v java &> /dev/null; then
    echo "  ✓ Java: $(java -version 2>&1 | head -n 1)"
else
    echo "  ✗ Java 없음 - JDK 21 설치 필요"
    echo "    Ubuntu: sudo apt install openjdk-21-jdk"
    echo "    Mac: brew install openjdk@21"
    exit 1
fi

# Gradle
if command -v gradle &> /dev/null; then
    echo "  ✓ Gradle: $(gradle -v | grep Gradle)"
else
    echo "  ✗ Gradle 없음 - 설치 필요"
    echo "    Ubuntu: sudo apt install gradle"
    echo "    Mac: brew install gradle"
    exit 1
fi

# Docker
if command -v docker &> /dev/null; then
    echo "  ✓ Docker: $(docker -v)"
else
    echo "  ✗ Docker 없음 - 설치 필요"
    exit 1
fi

# kubectl
if command -v kubectl &> /dev/null; then
    echo "  ✓ kubectl: $(kubectl version --client --short 2>/dev/null || kubectl version --client)"
else
    echo "  ⚠ kubectl 없음 - K8s 배포 시 필요"
fi

echo ""
echo "[설정] Gradle Wrapper 생성 중..."

PROJECTS=(
    "common-lib"
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

for project in "${PROJECTS[@]}"; do
    echo "  - ${project}"
    cd "$project"
    gradle wrapper --gradle-version 8.5 > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "    ✓ gradlew 생성됨"
    else
        echo "    ✗ 실패"
    fi
    cd ..
done

echo ""
echo "=========================================="
echo "  설정 완료!"
echo "=========================================="
echo ""
echo "다음 단계:"
echo "  1. ./build.sh   # 빌드"
echo "  2. ./start.sh   # K8s 배포"
