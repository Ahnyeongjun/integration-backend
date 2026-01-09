#!/bin/bash

echo "=========================================="
echo "  MSA Services 시작 (인프라 제외)"
echo "=========================================="

NAMESPACE="msa-platform"

# 1. Gateway
echo "[1/4] Gateway 시작..."
kubectl apply -f gateway/k8s/

# 2. Core Services
echo "[2/4] Core Services 시작..."
kubectl apply -f auth-service/k8s/
kubectl apply -f user-service/k8s/
kubectl apply -f bookmark-service/k8s/
kubectl apply -f schedule-service/k8s/

# 3. Business Services
echo "[3/4] Business Services 시작..."
kubectl apply -f ticketing-service/k8s/
kubectl apply -f book-service/k8s/
kubectl apply -f travel-service/k8s/
kubectl apply -f festival-service/k8s/
kubectl apply -f wedding-service/k8s/

# 4. AI & Image Services
echo "[4/4] AI & Image Services 시작..."
kubectl apply -f ai-service/k8s/
kubectl apply -f image-service/k8s/

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
echo "Gateway:"
kubectl get svc gateway -n $NAMESPACE -o jsonpath='  http://{.status.loadBalancer.ingress[0].ip}:{.spec.ports[0].port}' 2>/dev/null || echo "  kubectl port-forward svc/gateway 8080:80 -n $NAMESPACE"
echo ""
