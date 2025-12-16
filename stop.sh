#!/bin/bash

echo "=========================================="
echo "  MSA Platform 중지"
echo "=========================================="

NAMESPACE="msa-platform"

# 1. Business Services
echo "[1/5] Business Services 중지..."
kubectl delete -f wedding-service/k8s/ --ignore-not-found
kubectl delete -f festival-service/k8s/ --ignore-not-found
kubectl delete -f travel-service/k8s/ --ignore-not-found
kubectl delete -f book-service/k8s/ --ignore-not-found
kubectl delete -f ticketing-service/k8s/ --ignore-not-found

# 2. Core Services
echo "[2/5] Core Services 중지..."
kubectl delete -f schedule-service/k8s/ --ignore-not-found
kubectl delete -f bookmark-service/k8s/ --ignore-not-found
kubectl delete -f user-service/k8s/ --ignore-not-found
kubectl delete -f auth-service/k8s/ --ignore-not-found

# 3. Gateway
echo "[3/5] Gateway 중지..."
kubectl delete -f gateway/k8s/ --ignore-not-found

# 4. Jenkins
echo "[4/5] Jenkins 중지..."
kubectl delete -f infra/k8s/jenkins.yaml --ignore-not-found

# 5. Infrastructure
echo "[5/5] Infrastructure 중지..."
kubectl delete -f infra/k8s/kafka.yaml --ignore-not-found
kubectl delete -f infra/k8s/redis.yaml --ignore-not-found
kubectl delete -f infra/k8s/mysql.yaml --ignore-not-found

echo ""
read -p "ConfigMap, Secrets, Namespace도 삭제할까요? (y/N): " confirm
if [[ "$confirm" =~ ^[Yy]$ ]]; then
    echo "설정 삭제 중..."
    kubectl delete -f infra/k8s/configmap.yaml --ignore-not-found
    kubectl delete -f infra/k8s/secrets.yaml --ignore-not-found
    kubectl delete -f infra/k8s/namespace.yaml --ignore-not-found
    echo "전체 삭제 완료!"
else
    echo "설정 유지됨 (namespace: $NAMESPACE)"
fi

echo ""
echo "=========================================="
echo "  중지 완료!"
echo "=========================================="
kubectl get pods -n $NAMESPACE 2>/dev/null || echo "Namespace가 삭제되었거나 Pod가 없습니다."
