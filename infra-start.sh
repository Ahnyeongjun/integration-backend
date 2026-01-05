#!/bin/bash

echo "=========================================="
echo "  Infrastructure 시작"
echo "=========================================="

NAMESPACE="msa-platform"

# 1. Namespace & Config
echo "[1/5] Namespace & Config 생성..."
kubectl apply -f infra/k8s/namespace.yaml
kubectl apply -f infra/k8s/configmap.yaml
kubectl apply -f infra/k8s/secrets.yaml

# 2. Nexus
echo "[2/5] Nexus Registry 시작..."
kubectl apply -f infra/k8s/nexus.yaml
kubectl wait --for=condition=ready pod -l app=nexus -n $NAMESPACE --timeout=180s

# 3. MySQL & Redis
echo "[3/5] MySQL & Redis 시작..."
kubectl apply -f infra/k8s/mysql.yaml
kubectl apply -f infra/k8s/redis.yaml
kubectl wait --for=condition=ready pod -l app=mysql -n $NAMESPACE --timeout=120s
kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=60s

# 4. Zookeeper & Kafka
echo "[4/5] Zookeeper & Kafka 시작..."
kubectl apply -f infra/k8s/zookeeper.yaml
kubectl wait --for=condition=ready pod -l app=zookeeper -n $NAMESPACE --timeout=60s
kubectl apply -f infra/k8s/kafka.yaml
kubectl wait --for=condition=ready pod -l app=kafka -n $NAMESPACE --timeout=120s

# 5. Jenkins
echo "[5/5] Jenkins 시작..."
kubectl apply -f infra/k8s/jenkins.yaml

echo ""
echo "=========================================="
echo "  Infrastructure 시작 완료!"
echo "=========================================="
kubectl get pods -n $NAMESPACE
