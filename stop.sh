#!/bin/bash

echo "=========================================="
echo "  MSA Services 중지 (인프라 제외)"
echo "=========================================="

NAMESPACE="msa-platform"

# 1. Business Services
echo "[1/3] Business Services 중지..."
kubectl delete -f wedding-service/k8s/ --ignore-not-found
kubectl delete -f festival-service/k8s/ --ignore-not-found
kubectl delete -f travel-service/k8s/ --ignore-not-found
kubectl delete -f book-service/k8s/ --ignore-not-found
kubectl delete -f ticketing-service/k8s/ --ignore-not-found

# 2. Core Services
echo "[2/3] Core Services 중지..."
kubectl delete -f schedule-service/k8s/ --ignore-not-found
kubectl delete -f bookmark-service/k8s/ --ignore-not-found
kubectl delete -f user-service/k8s/ --ignore-not-found
kubectl delete -f auth-service/k8s/ --ignore-not-found

# 3. Gateway
echo "[3/3] Gateway 중지..."
kubectl delete -f gateway/k8s/ --ignore-not-found

echo ""
echo "=========================================="
echo "  서비스 중지 완료! (인프라는 유지됨)"
echo "=========================================="
kubectl get pods -n $NAMESPACE
