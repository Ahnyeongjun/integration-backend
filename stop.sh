#!/bin/bash

echo "=========================================="
echo "  MSA Services 중지 (인프라 제외)"
echo "=========================================="

NAMESPACE="msa-platform"

# 1. AI & Image Services
echo "[1/4] AI & Image Services 중지..."
kubectl delete -f image-service/k8s/ --ignore-not-found
kubectl delete -f ai-service/k8s/ --ignore-not-found

# 2. Business Services
echo "[2/4] Business Services 중지..."
kubectl delete -f wedding-service/k8s/ --ignore-not-found
kubectl delete -f festival-service/k8s/ --ignore-not-found
kubectl delete -f travel-service/k8s/ --ignore-not-found
kubectl delete -f book-service/k8s/ --ignore-not-found
kubectl delete -f ticketing-service/k8s/ --ignore-not-found

# 3. Core Services
echo "[3/4] Core Services 중지..."
kubectl delete -f schedule-service/k8s/ --ignore-not-found
kubectl delete -f bookmark-service/k8s/ --ignore-not-found
kubectl delete -f user-service/k8s/ --ignore-not-found
kubectl delete -f auth-service/k8s/ --ignore-not-found

# 4. Gateway
echo "[4/4] Gateway 중지..."
kubectl delete -f gateway/k8s/ --ignore-not-found

echo ""
echo "=========================================="
echo "  서비스 중지 완료! (인프라는 유지됨)"
echo "=========================================="
kubectl get pods -n $NAMESPACE
