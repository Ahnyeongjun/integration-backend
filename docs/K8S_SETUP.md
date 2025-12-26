# Kubernetes MSA 플랫폼 구축 가이드

## 클러스터 구성

| 노드 | 역할 | IP | 비고 |
|------|------|-----|------|
| test1 | Master | 192.168.1.7 (LAN) / 10.0.0.1 (VPN) | Ubuntu |
| desktop-neptik6 | Worker | 10.0.0.2 (VPN) | WSL2 |

## 네트워크 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    WireGuard VPN 터널                        │
│              (암호화된 Point-to-Point 연결)                   │
└─────────────────────────────────────────────────────────────┘
         │                                      │
    desktop (WSL)                          test1 (Master)
    10.0.0.2                               10.0.0.1
         │                                      │
         └──────── Kubernetes Cluster ──────────┘
                         │
              ┌──────────┴──────────┐
              │   Flannel CNI       │
              │   10.244.0.0/16     │
              └─────────────────────┘
```

## 1. WireGuard VPN 설정

### test1 (Master) - `/etc/wireguard/wg0.conf`
```ini
[Interface]
PrivateKey = <MASTER_PRIVATE_KEY>
Address = 10.0.0.1/24
ListenPort = 51820

[Peer]
PublicKey = <DESKTOP_PUBLIC_KEY>
AllowedIPs = 10.0.0.2/32
```

### desktop (WSL) - `/etc/wireguard/wg0.conf`
```ini
[Interface]
PrivateKey = <DESKTOP_PRIVATE_KEY>
Address = 10.0.0.2/24
DNS = 8.8.8.8

[Peer]
PublicKey = <MASTER_PUBLIC_KEY>
AllowedIPs = 10.0.0.0/24, 192.168.1.7/32
Endpoint = <MASTER_PUBLIC_IP>:51820
PersistentKeepalive = 25
```

**중요**: `AllowedIPs`에 `192.168.1.7/32` 추가 필수!
- Kubernetes API 서버 endpoint가 192.168.1.7:6443
- 이 IP가 VPN 터널을 통해 라우팅되어야 ClusterIP(10.96.0.1) 접근 가능

### WireGuard 명령어
```bash
# 시작
sudo wg-quick up wg0

# 중지
sudo wg-quick down wg0

# 상태 확인
sudo wg show

# 설정 파일 권한 (보안)
sudo chmod 600 /etc/wireguard/wg0.conf
```

## 2. Kubernetes 노드 설정

### Worker 노드 (desktop WSL) 설정

#### kubelet node-ip 설정
`/etc/systemd/system/kubelet.service.d/20-node-ip.conf`:
```ini
[Service]
Environment="KUBELET_EXTRA_ARGS=--node-ip=10.0.0.2"
```

적용:
```bash
sudo systemctl daemon-reload
sudo systemctl restart kubelet
```

#### /etc/hosts 설정
```
10.0.0.1 k8s-master
```

#### Swap 비활성화
```bash
sudo swapoff -a
```

### 노드 상태 확인
```bash
kubectl get nodes -o wide
```
예상 결과:
```
NAME              STATUS   ROLES           VERSION   INTERNAL-IP
test1             Ready    control-plane   v1.x.x    192.168.1.7
desktop-neptik6   Ready    <none>          v1.x.x    10.0.0.2
```

## 3. Flannel CNI 설정

### 설치
```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

### 상태 확인
```bash
kubectl get pods -n kube-flannel -o wide
```

### 트러블슈팅: flannel CrashLoopBackOff

#### 원인 1: API 서버 접근 불가
```
Failed to create SubnetManager: error retrieving...
```
→ WireGuard AllowedIPs에 192.168.1.7/32 추가

#### 원인 2: cni0 IP 충돌
```
"cni0" already has an IP address different from 10.244.x.1/24
```
→ desktop WSL에서 cni0 삭제:
```bash
sudo ip link set cni0 down
sudo ip link delete cni0
sudo ip link del flannel.1 2>/dev/null
sudo systemctl restart containerd
```

### /run/flannel/subnet.env (자동 생성됨)
```
FLANNEL_NETWORK=10.244.0.0/16
FLANNEL_SUBNET=10.244.x.1/24
FLANNEL_MTU=1450
FLANNEL_IPMASQ=true
```

## 4. Local Path Provisioner (동적 PV 프로비저닝)

### 설치
```bash
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml
```

### 기본 StorageClass로 설정
```bash
kubectl patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

### 상태 확인
```bash
kubectl get pods -n local-path-storage -o wide
kubectl get storageclass
```

### 트러블슈팅: Provisioner가 PV 생성 못함

증상:
```
Waiting for a volume to be created either by the external provisioner 'rancher.io/local-path'...
```

로그 확인:
```bash
kubectl logs -n local-path-storage -l app=local-path-provisioner
```

에러:
```
dial tcp 10.96.0.1:443: i/o timeout
```

원인: desktop 노드에서 실행 중인데 ClusterIP 접근 불가
해결: WireGuard + Flannel 네트워크 정상화 후 pod 재시작

## 5. MSA 플랫폼 배포

### 네임스페이스 및 시크릿
```bash
kubectl create namespace msa-platform
kubectl create secret generic msa-secrets -n msa-platform \
  --from-literal=MYSQL_PASSWORD=<password> \
  --from-literal=JWT_SECRET=<secret>
```

### 인프라 컴포넌트 배포 순서
1. MySQL (StatefulSet)
2. Redis
3. Zookeeper
4. Kafka

### 서비스 목록
| 서비스 | 포트 | 설명 |
|--------|------|------|
| discovery-server | 8761 | Eureka |
| gateway | 8080 | API Gateway |
| auth-service | 8081 | 인증 |
| user-service | 8082 | 사용자 |
| bookmark-service | 8083 | 북마크 |
| schedule-service | 8084 | 일정 |
| ticketing-service | 8085 | 티켓팅 |
| book-service | 8086 | 도서 |
| travel-service | 8087 | 여행 |
| festival-service | 8088 | 축제 |
| wedding-service | 8089 | 웨딩 |

## 6. 유용한 명령어

### 전체 상태 확인
```bash
kubectl get nodes -o wide
kubectl get pods -A -o wide
kubectl get pvc -A
kubectl get svc -A
```

### 특정 네임스페이스
```bash
kubectl get all -n msa-platform
kubectl get all -n kube-flannel
kubectl get all -n local-path-storage
```

### 로그 확인
```bash
kubectl logs <pod-name> -n <namespace>
kubectl logs <pod-name> -n <namespace> --previous  # 이전 크래시 로그
kubectl logs <pod-name> -n <namespace> -f          # 실시간
```

### 이벤트 확인
```bash
kubectl get events -n <namespace> --sort-by='.lastTimestamp'
kubectl describe pod <pod-name> -n <namespace>
```

### Pod 재시작
```bash
kubectl delete pod <pod-name> -n <namespace>
kubectl rollout restart deployment <deployment-name> -n <namespace>
```

## 7. 문제 해결 체크리스트

### 노드 NotReady
- [ ] WireGuard 실행 중? (`sudo wg show`)
- [ ] kubelet 실행 중? (`systemctl status kubelet`)
- [ ] Swap 비활성화? (`free -h`로 확인)
- [ ] node-ip 설정 올바른가?

### Pod Pending (PVC 대기)
- [ ] local-path-provisioner Running?
- [ ] StorageClass 존재?
- [ ] Provisioner가 API 서버 접근 가능?

### Pod CrashLoopBackOff
- [ ] 로그 확인 (`kubectl logs`)
- [ ] describe로 이벤트 확인
- [ ] 리소스 제한 확인 (메모리/CPU)
- [ ] 네트워크 연결 확인

### Flannel 문제
- [ ] flannel pod Running?
- [ ] /run/flannel/subnet.env 존재?
- [ ] cni0, flannel.1 인터페이스 존재?
- [ ] API 서버 접근 가능? (192.168.1.7:6443)

## 8. 아키텍처 다이어그램

```
                                Internet
                                    │
                         ┌──────────┴──────────┐
                         │   WireGuard VPN     │
                         │   (암호화 터널)      │
                         └──────────┬──────────┘
                ┌───────────────────┴───────────────────┐
                │                                       │
        ┌───────┴───────┐                       ┌───────┴───────┐
        │    test1      │                       │   desktop     │
        │   (Master)    │                       │   (Worker)    │
        │  10.0.0.1     │                       │   10.0.0.2    │
        │ 192.168.1.7   │                       │    (WSL2)     │
        └───────┬───────┘                       └───────┬───────┘
                │                                       │
                │         Kubernetes Cluster            │
                │    ┌─────────────────────────┐       │
                │    │     Flannel Network     │       │
                │    │     10.244.0.0/16       │       │
                │    └─────────────────────────┘       │
                │                                       │
        ┌───────┴───────────────────────────────┴───────┐
        │              MSA Platform (Namespace)          │
        │  ┌─────────┐ ┌─────────┐ ┌─────────────────┐  │
        │  │  MySQL  │ │  Redis  │ │ Kafka+Zookeeper │  │
        │  └─────────┘ └─────────┘ └─────────────────┘  │
        │  ┌─────────┐ ┌─────────┐ ┌─────────┐         │
        │  │ Gateway │ │  Auth   │ │  User   │  ...    │
        │  └─────────┘ └─────────┘ └─────────┘         │
        └───────────────────────────────────────────────┘
```
