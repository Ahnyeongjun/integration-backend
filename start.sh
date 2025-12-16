#!/bin/bash

echo "=========================================="
echo "  MSA Platform 시작"
echo "=========================================="

NAMESPACE="msa-platform"

# 1. Namespace & Config
echo "[1/7] Namespace & Config 생성..."
kubectl apply -f infra/k8s/namespace.yaml
kubectl apply -f infra/k8s/configmap.yaml
kubectl apply -f infra/k8s/secrets.yaml

# 2. Nexus (Docker Registry) - 먼저 시작
echo "[2/7] Nexus Registry 시작..."
kubectl apply -f infra/k8s/nexus.yaml

echo "  - Nexus 준비 대기 중... (최대 3분 소요)"
kubectl wait --for=condition=ready pod -l app=nexus -n $NAMESPACE --timeout=180s

echo ""
echo "  ============================================"
echo "  Nexus 초기 설정 필요!"
echo "  ============================================"
echo "  1. Nexus 웹 접속: http://192.168.1.7:30081"
echo "  2. 초기 비밀번호: kubectl exec -n $NAMESPACE deploy/nexus -- cat /nexus-data/admin.password"
echo "  3. Docker Registry (hosted) 생성 필요 (포트: 5000)"
echo "  4. Anonymous pull 활성화"
echo "  ============================================"
echo ""

# 3. Infrastructure (MySQL, Redis, Kafka)
echo "[3/7] Infrastructure 시작..."
kubectl apply -f infra/k8s/mysql.yaml
kubectl apply -f infra/k8s/redis.yaml
kubectl apply -f infra/k8s/kafka.yaml

echo "  - MySQL 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=mysql -n $NAMESPACE --timeout=120s

echo "  - Redis 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=60s

echo "  - Kafka 준비 대기 중..."
kubectl wait --for=condition=ready pod -l app=kafka -n $NAMESPACE --timeout=120s

# 4. Jenkins (CI/CD)
echo "[4/7] Jenkins 시작..."
kubectl apply -f infra/k8s/jenkins.yaml

# 5. Gateway
echo "[5/7] Gateway 시작..."
kubectl apply -f gateway/k8s/

# 6. Core Services
echo "[6/7] Core Services 시작..."
kubectl apply -f auth-service/k8s/
kubectl apply -f user-service/k8s/
kubectl apply -f bookmark-service/k8s/
kubectl apply -f schedule-service/k8s/

# 7. Business Services
echo "[7/7] Business Services 시작..."
kubectl apply -f ticketing-service/k8s/
kubectl apply -f book-service/k8s/
kubectl apply -f travel-service/k8s/
kubectl apply -f festival-service/k8s/
kubectl apply -f wedding-service/k8s/

echo ""
echo "=========================================="
echo "  배포 완료! Pod 상태 확인 중..."
echo "=========================================="

sleep 5
kubectl get pods -n $NAMESPACE

echo ""
echo "=========================================="
echo "  서비스 접속 정보"
echo "=========================================="
echo ""
echo "Nexus Registry:"
echo "  - 웹 UI: http://192.168.1.7:30081"
echo "  - Docker: 192.168.1.7:30500"
echo ""
echo "Gateway:"
kubectl get svc gateway -n $NAMESPACE -o jsonpath='  http://{.status.loadBalancer.ingress[0].ip}:{.spec.ports[0].port}' 2>/dev/null || echo "  kubectl port-forward svc/gateway 8080:80 -n $NAMESPACE"
echo ""
echo ""
echo "Jenkins:"
kubectl get svc jenkins -n $NAMESPACE -o jsonpath='  http://{.status.loadBalancer.ingress[0].ip}:{.spec.ports[0].port}' 2>/dev/null || echo "  kubectl port-forward svc/jenkins 8080:8080 -n $NAMESPACE"
echo ""
