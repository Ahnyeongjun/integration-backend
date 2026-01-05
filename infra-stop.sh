#!/bin/bash

echo "=========================================="
echo "  Infrastructure 중지"
echo "=========================================="

NAMESPACE="msa-platform"

echo "[1/5] Jenkins 중지..."
kubectl delete -f infra/k8s/jenkins.yaml --ignore-not-found

echo "[2/5] Kafka 중지..."
kubectl delete -f infra/k8s/kafka.yaml --ignore-not-found

echo "[3/5] Zookeeper 중지..."
kubectl delete -f infra/k8s/zookeeper.yaml --ignore-not-found

echo "[4/5] Redis 중지..."
kubectl delete -f infra/k8s/redis.yaml --ignore-not-found

echo "[5/5] MySQL 중지..."
kubectl delete -f infra/k8s/mysql.yaml --ignore-not-found

echo ""
read -p "Nexus, ConfigMap, Secrets, Namespace도 삭제할까요? (y/N): " confirm
if [[ "$confirm" =~ ^[Yy]$ ]]; then
    echo "전체 삭제 중..."
    kubectl delete -f infra/k8s/nexus.yaml --ignore-not-found
    kubectl delete -f infra/k8s/configmap.yaml --ignore-not-found
    kubectl delete -f infra/k8s/secrets.yaml --ignore-not-found
    kubectl delete -f infra/k8s/namespace.yaml --ignore-not-found
    echo "전체 삭제 완료!"
else
    echo "Nexus, 설정 유지됨"
fi

echo ""
echo "=========================================="
echo "  Infrastructure 중지 완료!"
echo "=========================================="
