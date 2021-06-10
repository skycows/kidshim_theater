# AWS 설정
## 계정접속
```bash
kinux@gram-kidshim:~/study/aws$ aws configure
AWS Access Key ID [****************LROQ]: AKIA4ZVUN62G2LS63PFC
AWS Secret Access Key [****************EZgQ]: ~~~~~~~~~~~~~~~~~~~~
Default region name [ap-northeast-2]: ap-southeast-2
Default output format [json]: json
```
## 계정확인
```bash
kinux@gram-kidshim:~/study/aws$ cat ~/.aws/credentials
[default]
aws_access_key_id = AKIA4ZVUN62G2LS63PFC
aws_secret_access_key = ~~~~~~~~~~~~~~~~~~

kinux@gram-kidshim:~/study/aws$ cat ~/.aws/config
[default]
region = ap-southeast-2
output = json
kinux@gram-kidshim:~/study/aws$
```

## cluster 생성
```bash
kinux@gram-kidshim:~/study/aws$ eksctl create cluster --name user10-eks --version 1.17 --nodegroup-name standard-workers --node-type t3.medium --nodes 4 --nodes-min 1 --nodes-max 4
kinux@gram-kidshim:~/study/aws$ aws eks --region ap-southeast-2 update-kubeconfig --name user10-eks
kinux@gram-kidshim:~/study/aws$ kubectl config current-context
arn:aws:eks:ap-southeast-2:879772956301:cluster/user10-eks
```
## 노드 확인
```bash
kinux@gram-kidshim:~/study/aws$ kubectl get nodes
NAME                                                STATUS   ROLES    AGE    VERSION
ip-192-168-4-168.ap-southeast-2.compute.internal    Ready    <none>   3m8s   v1.17.12-eks-7684af
ip-192-168-57-49.ap-southeast-2.compute.internal    Ready    <none>   3m6s   v1.17.12-eks-7684af
ip-192-168-67-155.ap-southeast-2.compute.internal   Ready    <none>   3m8s   v1.17.12-eks-7684af
ip-192-168-7-159.ap-southeast-2.compute.internal    Ready    <none>   3m3s   v1.17.12-eks-7684af
```
## cluster 확인
```bash
kinux@gram-kidshim:~/study/aws$ kubectl config current-context
arn:aws:eks:ap-southeast-2:879772956301:cluster/user10-eks
```

# 이벤트스토밍
---
## 팀과제 결과
---
기능적 요구사항
1. 고객이 영화를 선택하여 예매한다.
1. 고객이 결제한다.
1. 예매가 완료되면 예매 내역이 해당 극장에 전달된다.
1. 영화가 등록되면 상영관이 지정된다.
1. 해당 극장은 해당영화의 관람관 좌석를 예매처리 한다.
1. 고객은 예매를 취소할 수 있다.
1. 예매를 취소하면 좌석예매도 취소처리한다.
1. 예매 현황은 카톡으로 알려 준다.(이벤트 발행)

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않으면 예매를 할 수 없다.
1. 장애격리
    1. 영화관리, 좌석관리 기능이 수행되지 않더라도 예매는 365일 24시간 받을 수 있어야 한다.
    1. 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다.
1. 기타
    1. 예매상태가 바뀔때마다 카톡 등으로 알림을 줄 수 있어야 한다.
    <img src="https://user-images.githubusercontent.com/81547613/119284217-1fc57b80-bc7a-11eb-8ad5-59d8efba8487.png" width="1500">

## 개인과제 결과
---
### 추가요건
- 기능적 요구사항
  1. 발권은 반드시 예약 상태를 확인 후 진행한다.
  1. 고객센터에서는 고객의 모든 예약상태를 조회할 수 있어야 한다.
  <img src="https://user-images.githubusercontent.com/80908892/120929379-c94d4800-c723-11eb-8b31-22a9984ed905.png" width="1500">
  - customercenter 서비스 추가
  - ticketing 서비스 추가

# 구현 및 테스트
## Kubernetes환경 구성
---
### namespace 설정 
```bash
kinux@gram-kidshim:~/study/aws$ kubectl create ns kafka
namespace/kafka created
kinux@gram-kidshim:~/study/aws$ kubectl create ns user10-ns
namespace/user10-ns created
kinux@gram-kidshim:~/study/aws$ kubectl get ns
NAME              STATUS   AGE
default           Active   13m
kafka             Active   61s
kube-node-lease   Active   13m
kube-public       Active   13m
kube-system       Active   13m
user10-ns         Active   18s
kinux@gram-kidshim:~/study/aws$ kubectl config set-context $(kubectl config current-context) --namespace=user10-ns
Context "arn:aws:eks:ap-southeast-2:879772956301:cluster/user10-eks" modified.
kinux@gram-kidshim:~/study/aws$ kubectl config get-contexts
CURRENT   NAME                                                         CLUSTER                                                      AUTHINFO                                                     NAMESPACE
*         arn:aws:eks:ap-southeast-2:879772956301:cluster/user10-eks   arn:aws:eks:ap-southeast-2:879772956301:cluster/user10-eks   arn:aws:eks:ap-southeast-2:879772956301:cluster/user10-eks   user10-ns
          user10@user10-eks.ap-southeast-2.eksctl.io                   user10-eks.ap-southeast-2.eksctl.io                          user10@user10-eks.ap-southeast-2.eksctl.io
```
### kafka설치
```bash
kinux@gram-kidshim:~/study/aws$ curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 11248  100 11248    0     0  59829      0 --:--:-- --:--:-- --:--:-- 59829
kinux@gram-kidshim:~/study/aws$ chmod 700 get_helm.sh
kinux@gram-kidshim:~/study/aws$ ./get_helm.sh
kinux@gram-kidshim:~/study/aws$ helm repo add incubator https://charts.helm.sh/incubator
kinux@gram-kidshim:~/study/aws$ helm install my-kafka --namespace kafka incubator/kafka
kinux@gram-kidshim:~/study/aws$ kubectl get all -n kafka
NAME                       READY   STATUS    RESTARTS   AGE
pod/my-kafka-0             1/1     Running   1          5m30s
pod/my-kafka-1             1/1     Running   0          3m23s
pod/my-kafka-2             1/1     Running   0          2m16s
pod/my-kafka-zookeeper-0   1/1     Running   0          5m30s
pod/my-kafka-zookeeper-1   1/1     Running   0          4m51s
pod/my-kafka-zookeeper-2   1/1     Running   0          4m

NAME                                  TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                      AGE
service/my-kafka                      ClusterIP   10.100.234.27    <none>        9092/TCP                     5m32s
service/my-kafka-headless             ClusterIP   None             <none>        9092/TCP                     5m32s
service/my-kafka-zookeeper            ClusterIP   10.100.211.135   <none>        2181/TCP                     5m32s
service/my-kafka-zookeeper-headless   ClusterIP   None             <none>        2181/TCP,3888/TCP,2888/TCP   5m32s

NAME                                  READY   AGE
statefulset.apps/my-kafka             3/3     5m33s
statefulset.apps/my-kafka-zookeeper   3/3     5m33s
```

### docker login
```bash
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/app$ docker login --username AWS -p $(aws ecr get-login-password --region ap-southeast-2)  879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/
WARNING! Using --password via the CLI is insecure. Use --password-stdin.
Login Succeeded
```
### docker build and push
```bash
mvn clean && mvn package
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-app:v1 .
aws ecr create-repository --repository-name user10-app --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-app:v1
kubectl apply -f ./kubernetes
```
### k8s deploy
```bash
cd app
kubectl apply -f ./kubernetes
cd ..
cd pay
kubectl apply -f ./kubernetes
cd ..
cd movie
kubectl apply -f ./kubernetes
cd ..
cd theater
kubectl apply -f ./kubernetes
cd ..
cd customercenter
kubectl apply -f ./kubernetes
cd ..
cd ticketing
kubectl apply -f ./kubernetes
cd ..
cd gateway
kubectl create deploy gateway --image=879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-gateway:v1
kubectl expose deploy gateway --type="LoadBalancer" --port=8080
cd ..
kubectl create deploy kakao --image=879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-kakao:v1
kubectl expose deploy kakao --type="ClusterIP" --port=8080
cd ..
kubectl run siege --image=apexacme/siege-nginx
```
```shell
Every 2.0s: kubectl get all                                                                                                                                                                                               gram-kidshim: Wed Jun  9 13:49:39 2021
NAME                                  READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4               1/1     Running   0          9m42s
pod/customercenter-7b89f74f46-srx52   1/1     Running   0          9m31s
pod/gateway-6d664c5d6f-nmg58          1/1     Running   0          4m56s
pod/kakao-794487c556-z5vzt            1/1     Running   0          4m31s
pod/movie-ccbbc5bb9-42r9k             1/1     Running   0          9m36s
pod/pay-56967ccf97-lllb4              1/1     Running   0          9m39s
pod/siege                             1/1     Running   0          2m16s
pod/theater-59b665f8d4-wpr75          1/1     Running   0          9m34s
pod/ticketing-565fb6b5bc-2zw4n        1/1     Running   0          9m28s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         104m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         26m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   25m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         18m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         22m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         22m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         20m
service/ticketing        ClusterIP      10.100.66.201    <none>                                                                        8080/TCP         20m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           9m44s
deployment.apps/customercenter   1/1     1            1           9m33s
deployment.apps/gateway          1/1     1            1           4m58s
deployment.apps/kakao            1/1     1            1           4m33s
deployment.apps/movie            1/1     1            1           9m38s
deployment.apps/pay              1/1     1            1           9m41s
deployment.apps/theater          1/1     1            1           9m36s
deployment.apps/ticketing        1/1     1            1           9m30s

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       9m45s
replicaset.apps/customercenter-7b89f74f46   1         1         1       9m34s
replicaset.apps/gateway-6d664c5d6f          1         1         1       4m59s
replicaset.apps/kakao-794487c556            1         1         1       4m34s
replicaset.apps/movie-ccbbc5bb9             1         1         1       9m39s
replicaset.apps/pay-56967ccf97              1         1         1       9m42s
replicaset.apps/theater-59b665f8d4          1         1         1       9m37s
replicaset.apps/ticketing-565fb6b5bc        1         1         1       9m31s
```

### k8s deploy 점검 및 gateway테스트
```shell
#!/usr/bin/env bash
#

echo "reservations: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations | jq .page.totalElements)"
echo "approvals: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/approvals | jq .page.totalElements)"
echo "movieManagements: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements | jq .page.totalElements)"
echo "movieSeats: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieSeats | jq .page.totalElements)"
echo "movies: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movies | jq .page.totalElements)"
echo "bookInfos: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/bookInfos | jq .page.totalElements)"
echo "tickets: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/tickets | jq .page.totalElements)"
```
```console
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ ./k8s-test-count-script.sh
reservations: 0
approvals: 0
movieManagements: 0
movieSeats: 0
movies: 0
bookInfos: 0
tickets: 0
```
---
## 동작 테스트
---
### 영화등록
```bash
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements movieId="MOVIE-00001" title="어벤져스" status="RUNNING"
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements movieId="MOVIE-00002" title="아이언맨" status="RUNNING"
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements movieId="MOVIE-00003" title="토르" status="WAITING"
```

```shell
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ ./k8s-test-count-script.sh
reservations: 0
approvals: 0
movieManagements: 3
movieSeats: 0
movies: 3
bookInfos: 0
tickets: 0
```

```shell
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Wed, 09 Jun 2021 05:15:54 GMT
transfer-encoding: chunked

{
    "_embedded": {
        "movieManagements": [
            {
                "_links": {
                    "movieManagement": {
                        "href": "http://movie:8080/movieManagements/1"
                    },
                    "self": {
                        "href": "http://movie:8080/movieManagements/1"
                    }
                },
                "movieId": "MOVIE-00001",
                "status": "RUNNING",
                "title": "어벤져스"
            },
            {
                "_links": {
                    "movieManagement": {
                        "href": "http://movie:8080/movieManagements/2"
                    },
                    "self": {
                        "href": "http://movie:8080/movieManagements/2"
                    }
                },
                "movieId": "MOVIE-00002",
                "status": "RUNNING",
                "title": "아이언맨"
            },
            {
                "_links": {
                    "movieManagement": {
                        "href": "http://movie:8080/movieManagements/3"
                    },
                    "self": {
                        "href": "http://movie:8080/movieManagements/3"
                    }
                },
                "movieId": "MOVIE-00003",
                "status": "WAITING",
                "title": "토르"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://movie:8080/profile/movieManagements"
        },
        "self": {
            "href": "http://movie:8080/movieManagements{?page,size,sort}",
            "templated": true
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 3,
        "totalPages": 1
    }
}
```
```shell
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movies
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Wed, 09 Jun 2021 05:22:25 GMT
transfer-encoding: chunked

{
    "_embedded": {
        "movies": [
            {
                "_links": {
                    "movie": {
                        "href": "http://theater:8080/movies/1"
                    },
                    "self": {
                        "href": "http://theater:8080/movies/1"
                    }
                },
                "movieId": "MOVIE-00001",
                "screenId": "어벤져스_상영관"
            },
            {
                "_links": {
                    "movie": {
                        "href": "http://theater:8080/movies/2"
                    },
                    "self": {
                        "href": "http://theater:8080/movies/2"
                    }
                },
                "movieId": "MOVIE-00002",
                "screenId": "아이언맨_상영관"
            },
            {
                "_links": {
                    "movie": {
                        "href": "http://theater:8080/movies/3"
                    },
                    "self": {
                        "href": "http://theater:8080/movies/3"
                    }
                },
                "movieId": "MOVIE-00003",
                "screenId": "토르_상영관"
            }
        ]
    },
    "_links": {
        "profile": {
            "href": "http://theater:8080/profile/movies"
        },
        "search": {
            "href": "http://theater:8080/movies/search"
        },
        "self": {
            "href": "http://theater:8080/movies{?page,size,sort}",
            "templated": true
        }
    },
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 3,
        "totalPages": 1
    }
}
```
---
### 예매하기
```shell
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="B1003" customerId="C1003" movieId="MOVIE-00003" seatId="C-1"
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Jun 2021 05:26:09 GMT
transfer-encoding: chunked

{
    "bookId": "B1003",
    "bookedYn": "Y",
    "customerId": "C1003",
    "id": 1,
    "movieId": "MOVIE-00003",
    "payId": "5e6e5b3e-00d4-4aaf-8e58-50ac08ad5da0",
    "seatId": "C-1"
}

kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="B1001" customerId="C1001" movieId="MOVIE-00001" seatId="A-1"
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Jun 2021 05:28:35 GMT
transfer-encoding: chunked

{
    "bookId": "B1001",
    "bookedYn": "Y",
    "customerId": "C1001",
    "id": 2,
    "movieId": "MOVIE-00001",
    "payId": "a09e0958-7637-4944-8da0-3a76b3532cae",
    "seatId": "A-1"
}

kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="B1002" customerId="C1002" movieId="MOVIE-00002" seatId="B-1"
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Jun 2021 05:29:02 GMT
transfer-encoding: chunked

{
    "bookId": "B1002",
    "bookedYn": "Y",
    "customerId": "C1002",
    "id": 3,
    "movieId": "MOVIE-00002",
    "payId": "f4e117e7-0447-44cb-a1e9-9994c1c3b5bd",
    "seatId": "B-1"
}
```
- 점검
```shell
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ ./k8s-test-count-script.sh
reservations: 3
approvals: 3
movieManagements: 3
movieSeats: 3
movies: 3
bookInfos: 3
tickets: 3
```
- 정상적으로 데이터가 생성된 것을 확인할 수 있다.

---
###  고객센타
- 시나리오 
  - 각각의 서비스에 필요한 정보는 고객센터로 취합된다.
  - 고객센터는 취합된 정보를 view통해 조회하여 서비스가 가능하다.
```sh
root@siege:/# http GET http://customercenter:8080/searchBook?bookId=C1002
HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Jun 2021 13:38:05 GMT
Transfer-Encoding: chunked

{
    "bookId": "C1002",
    "customerId": "D1002",
    "movieId": "MOVIE-00002",
    "seatId": "E-1",
    "status": "Reserved"
}
```
- 고객선테에서 정상응답을 경우(발권서비스 - req/res)
```sh
root@siege-5b99b44c9c-pv7ll:/# http http://ticketing:8080/print/B1003
HTTP/1.1 200
Content-Length: 3
Content-Type: text/plain;charset=UTF-8
Date: Wed, 09 Jun 2021 15:32:43 GMT

B-2
```
- 고객센터 서비스 다운(발권서비스 - req/res)
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ kubectl delete pod/customercenter-6468986d85-m2mx5
pod "customercenter-6468986d85-m2mx5" deleted
```
- 고객센터 서비스가 장애일 경우(발권서비스 - req/res)
```sh
root@siege-5b99b44c9c-pv7ll:/# http http://ticketing:8080/print/B1003
HTTP/1.1 200
Content-Length: 20
Content-Type: text/plain;charset=UTF-8
Date: Wed, 09 Jun 2021 15:37:01 GMT

Fallback - ticketing
```
- 고객선터에서 예약정보 삭제(발권서비스 - req/res)

![image](https://user-images.githubusercontent.com/80908892/121385949-8dfd8400-c984-11eb-8a6e-c4378ee2a788.png)

- 고객센터의 예약정보가 비정상일 경우 발권 불가(발권서비스 - req/res)
```sh
root@siege-5b99b44c9c-pv7ll:/# http http://ticketing:8080/print/B1003
HTTP/1.1 200
Content-Length: 17
Content-Type: text/plain;charset=UTF-8
Date: Wed, 09 Jun 2021 15:42:20 GMT

check your BookId
```

---
## 비기능 요구사항
---
### Autoscale (HPA)
- 사나리오
  - 고객센터(customercenter)에서 제공되는 서비스의 기능에 성능부하 로직을 적용한다.- isolations
  - 서비스 부하에 따른 고객센터의 Autoscale을 확인한다.
- metrics설치
```shell
kubectl apply -f  https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.7/components.yaml
```
- metrics설치 확인
```shell
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ kubectl get all -n kube-system
NAME                                  READY   STATUS    RESTARTS   AGE
pod/aws-node-mqljh                    1/1     Running   0          3h21m
pod/aws-node-q6zf2                    1/1     Running   0          3h21m
pod/aws-node-qf5t4                    1/1     Running   0          3h21m
pod/aws-node-whn2r                    1/1     Running   0          3h21m
pod/coredns-76c9876f5-4mjpj           1/1     Running   0          3h28m
pod/coredns-76c9876f5-d6jqd           1/1     Running   0          3h28m
pod/kube-proxy-56sfh                  1/1     Running   0          3h21m
pod/kube-proxy-5mt7k                  1/1     Running   0          3h21m
pod/kube-proxy-nbjnr                  1/1     Running   0          3h21m
pod/kube-proxy-snp72                  1/1     Running   0          3h21m
pod/metrics-server-6648f5454b-ph7j6   1/1     Running   0          73s

NAME                     TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)         AGE
service/kube-dns         ClusterIP   10.100.0.10     <none>        53/UDP,53/TCP   3h28m
service/metrics-server   ClusterIP   10.100.218.95   <none>        443/TCP         73s

NAME                        DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR   AGE
daemonset.apps/aws-node     4         4         4       4            4           <none>          3h28m
daemonset.apps/kube-proxy   4         4         4       4            4           <none>          3h28m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/coredns          2/2     2            2           3h28m
deployment.apps/metrics-server   1/1     1            1           75s

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/coredns-76c9876f5           2         2         2       3h28m
replicaset.apps/metrics-server-6648f5454b   1         1         1       75s
```
- 리소스 설정
```sh
apiVersion: apps/v1
kind: Deployment
metadata:
  name: customercenter
  labels:
    app: customercenter
spec:
  replicas: 1
  selector:
    matchLabels:
      app: customercenter
  template:
    metadata:
      labels:
        app: customercenter
    spec:
      containers:
        - name: customercenter
          image: 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-customercenter:v1
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
          #리스소 설정
          resources:
            limits:
              cpu: "500m"
            requests:
              cpu: "200m"
```
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/customercenter$ kubectl autoscale deployment customercenter --cpu-percent=50 --min=1 --max=10
horizontalpodautoscaler.autoscaling/customercenter autoscaled
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/customercenter$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          81m
pod/customercenter-56d47c8c6-446qf   1/1     Running   0          5m26s
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          76m
pod/kakao-794487c556-z5vzt           1/1     Running   0          75m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          81m
pod/pay-56967ccf97-lllb4             1/1     Running   0          81m
pod/siege                            1/1     Running   0          73m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          81m
pod/ticketing-565fb6b5bc-2zw4n       1/1     Running   0          80m

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         175m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         97m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   97m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         90m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         93m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         94m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         92m
service/ticketing        ClusterIP      10.100.66.201    <none>                                                                        8080/TCP         91m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           81m
deployment.apps/customercenter   1/1     1            1           80m
deployment.apps/gateway          1/1     1            1           76m
deployment.apps/kakao            1/1     1            1           75m
deployment.apps/movie            1/1     1            1           81m
deployment.apps/pay              1/1     1            1           81m
deployment.apps/theater          1/1     1            1           81m
deployment.apps/ticketing        1/1     1            1           80m

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       81m
replicaset.apps/customercenter-56d47c8c6    1         1         1       5m29s
replicaset.apps/customercenter-7b89f74f46   0         0         0       81m
replicaset.apps/gateway-6d664c5d6f          1         1         1       76m
replicaset.apps/kakao-794487c556            1         1         1       76m
replicaset.apps/movie-ccbbc5bb9             1         1         1       81m
replicaset.apps/pay-56967ccf97              1         1         1       81m
replicaset.apps/theater-59b665f8d4          1         1         1       81m
replicaset.apps/ticketing-565fb6b5bc        1         1         1       80m

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   4%/50%    1         10        1          31s
```
- TARGETS이 50% 설정된 것을 확인할 수 있다. 
- 부하 발생 전
```
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          104m
pod/customercenter-56d47c8c6-446qf   1/1     Running   0          28m
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          99m
pod/kakao-794487c556-z5vzt           1/1     Running   0          99m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          104m
pod/pay-56967ccf97-lllb4             1/1     Running   0          104m
pod/siege                            1/1     Running   0          96m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          104m
pod/ticketing-565fb6b5bc-2zw4n       1/1     Running   0          103m

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         3h18m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         120m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   120m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         113m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         116m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         117m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         115m
service/ticketing        ClusterIP      10.100.66.201    <none>                                                                        8080/TCP         114m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           104m
deployment.apps/customercenter   1/1     1            1           104m
deployment.apps/gateway          1/1     1            1           99m
deployment.apps/kakao            1/1     1            1           99m
deployment.apps/movie            1/1     1            1           104m
deployment.apps/pay              1/1     1            1           104m
deployment.apps/theater          1/1     1            1           104m
deployment.apps/ticketing        1/1     1            1           103m

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       104m
replicaset.apps/customercenter-56d47c8c6    1         1         1       28m
replicaset.apps/customercenter-7b89f74f46   0         0         0       104m
replicaset.apps/gateway-6d664c5d6f          1         1         1       99m
replicaset.apps/kakao-794487c556            1         1         1       99m
replicaset.apps/movie-ccbbc5bb9             1         1         1       104m
replicaset.apps/pay-56967ccf97              1         1         1       104m
replicaset.apps/theater-59b665f8d4          1         1         1       104m
replicaset.apps/ticketing-565fb6b5bc        1         1         1       104m

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   7%/50%    1         10        1          23m
```
- 부하 생성
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/customercenter$ kubectl exec -it pod/siege -- /bin/bash
root@siege:/# siege -c30 -t30S -v http://customercenter:8080/isolation
```
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          105m
pod/customercenter-56d47c8c6-446qf   1/1     Running   0          29m
pod/customercenter-56d47c8c6-89fn7   0/1     Running   0          47s
pod/customercenter-56d47c8c6-8hlqd   0/1     Running   0          31s
pod/customercenter-56d47c8c6-phcpc   0/1     Running   0          47s
pod/customercenter-56d47c8c6-rrp9t   0/1     Running   0          47s
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          100m
pod/kakao-794487c556-z5vzt           1/1     Running   0          99m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          104m
pod/pay-56967ccf97-lllb4             1/1     Running   0          105m
pod/siege                            1/1     Running   0          97m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          104m
pod/ticketing-565fb6b5bc-2zw4n       1/1     Running   0          104m

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         3h19m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         121m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   121m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         114m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         117m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         118m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         116m
service/ticketing        ClusterIP      10.100.66.201    <none>                                                                        8080/TCP         115m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           105m
deployment.apps/customercenter   1/5     5            1           104m
deployment.apps/gateway          1/1     1            1           100m
deployment.apps/kakao            1/1     1            1           99m
deployment.apps/movie            1/1     1            1           105m
deployment.apps/pay              1/1     1            1           105m
deployment.apps/theater          1/1     1            1           104m
deployment.apps/ticketing        1/1     1            1           104m

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       105m
replicaset.apps/customercenter-56d47c8c6    5         5         1       29m
replicaset.apps/customercenter-7b89f74f46   0         0         0       104m
replicaset.apps/gateway-6d664c5d6f          1         1         1       100m
replicaset.apps/kakao-794487c556            1         1         1       99m
replicaset.apps/movie-ccbbc5bb9             1         1         1       105m
replicaset.apps/pay-56967ccf97              1         1         1       105m
replicaset.apps/theater-59b665f8d4          1         1         1       104m
replicaset.apps/ticketing-565fb6b5bc        1         1         1       104m

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   2%/50%    1         10        5          24m

kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          109m
pod/customercenter-56d47c8c6-446qf   1/1     Running   0          33m
pod/customercenter-56d47c8c6-89fn7   1/1     Running   0          5m5s
pod/customercenter-56d47c8c6-8hlqd   1/1     Running   0          4m49s
pod/customercenter-56d47c8c6-phcpc   1/1     Running   0          5m5s
pod/customercenter-56d47c8c6-rrp9t   1/1     Running   0          5m5s
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          104m
pod/kakao-794487c556-z5vzt           1/1     Running   0          104m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          109m
pod/pay-56967ccf97-lllb4             1/1     Running   0          109m
pod/siege                            1/1     Running   0          101m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          109m
pod/ticketing-565fb6b5bc-2zw4n       1/1     Running   0          109m

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         3h23m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         126m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   125m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         118m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         121m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         122m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         120m
service/ticketing        ClusterIP      10.100.66.201    <none>                                                                        8080/TCP         119m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           109m
deployment.apps/customercenter   5/5     5            5           109m
deployment.apps/gateway          1/1     1            1           104m
deployment.apps/kakao            1/1     1            1           104m
deployment.apps/movie            1/1     1            1           109m
deployment.apps/pay              1/1     1            1           109m
deployment.apps/theater          1/1     1            1           109m
deployment.apps/ticketing        1/1     1            1           109m

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       109m
replicaset.apps/customercenter-56d47c8c6    5         5         5       33m
replicaset.apps/customercenter-7b89f74f46   0         0         0       109m
replicaset.apps/gateway-6d664c5d6f          1         1         1       104m
replicaset.apps/kakao-794487c556            1         1         1       104m
replicaset.apps/movie-ccbbc5bb9             1         1         1       109m
replicaset.apps/pay-56967ccf97              1         1         1       109m
replicaset.apps/theater-59b665f8d4          1         1         1       109m
replicaset.apps/ticketing-565fb6b5bc        1         1         1       109m

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   3%/50%    1         10        5          28m
```
- sacle out
```sh 실행결과
HTTP/1.1 200     0.11 secs:       4 bytes ==> GET  /isolation
HTTP/1.1 200     0.11 secs:       4 bytes ==> GET  /isolation
HTTP/1.1 200     0.17 secs:       4 bytes ==> GET  /isolation
HTTP/1.1 200     0.07 secs:       4 bytes ==> GET  /isolation
HTTP/1.1 200     0.11 secs:       4 bytes ==> GET  /isolation
HTTP/1.1 200     0.09 secs:       4 bytes ==> GET  /isolation
HTTP/1.1 200     0.39 secs:       4 bytes ==> GET  /isolation

Lifting the server siege...
Transactions:                   5024 hits
Availability:                 100.00 %
Elapsed time:                  29.74 secs
Data transferred:               0.02 MB
Response time:                  0.18 secs
Transaction rate:             168.93 trans/sec
Throughput:                     0.00 MB/sec
Concurrency:                   29.89
Successful transactions:        5024
Failed transactions:               0
Longest transaction:            1.12
Shortest transaction:           0.00
```
---
### self-healing (liveness)
- 사나리오
  - 발권서비스(ticketing )에서 제공되는 livenessProbe로 /actuator/health를 활용한다.
  - /actuator/health를 강제로 down시킬수 있는 url를 작성하여 liveness점검시 오류를 발생시킨다.
  - 새로운 pod를 생성시키는지 확인한다.

```java
package theatermy;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator")
public class CustomHealthIndicator implements HealthIndicator {
    private final AtomicReference<Health> health = new AtomicReference<>(Health.up().build());

    @Override
    public Health health() {
        return health.get();
    }

    @PutMapping("/up")
    public Health up() {
        Health up = Health.up().build();
        this.health.set(up);
        return up;
    }

    @PutMapping("/down")
    public Health down() {
        Health down = Health.down().build();
        this.health.set(down);
        return down;
    }
}
```
```yaml
management:
  health:
    status:
      order: DOWN, MAINTENANCE, UNKOWN, UP
      http-mapping:
        DOWN: 503
        MAINTENANCE: 503
        UNKNOWN: 200
        UP: 200
  endpoints:
      web:
          exposure:
              include: "*"
spring:
  mvc:
    hiddenmethod:
      filter:
        enabled: true
```
- actuator 시뮬레이션 
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl exec -it pod/siege -- /bin/bash
root@siege:/# http http://ticketing:8080/actuator/health
HTTP/1.1 200
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Wed, 09 Jun 2021 07:13:07 GMT
Transfer-Encoding: chunked

{
    "status": "UP"
}
```
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          3h34m
pod/customercenter-56d47c8c6-89fn7   1/1     Running   0          110m
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          3h30m
pod/kakao-794487c556-z5vzt           1/1     Running   0          3h29m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          3h34m
pod/pay-56967ccf97-lllb4             1/1     Running   0          3h34m
pod/siege                            1/1     Running   0          3h27m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          3h34m
pod/ticketing-56f99d87d-lxp59        1/1     Running   0          89s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         5h9m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         3h51m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   3h50m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         3h44m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         3h47m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         3h47m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         3h45m
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP         13m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           3h34m
deployment.apps/customercenter   1/1     1            1           3h34m
deployment.apps/gateway          1/1     1            1           3h30m
deployment.apps/kakao            1/1     1            1           3h29m
deployment.apps/movie            1/1     1            1           3h34m
deployment.apps/pay              1/1     1            1           3h34m
deployment.apps/theater          1/1     1            1           3h34m
deployment.apps/ticketing        1/1     1            1           91s

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       3h34m
replicaset.apps/customercenter-56d47c8c6    1         1         1       139m
replicaset.apps/customercenter-7b89f74f46   0         0         0       3h34m
replicaset.apps/gateway-6d664c5d6f          1         1         1       3h30m
replicaset.apps/kakao-794487c556            1         1         1       3h29m
replicaset.apps/movie-ccbbc5bb9             1         1         1       3h34m
replicaset.apps/pay-56967ccf97              1         1         1       3h34m
replicaset.apps/theater-59b665f8d4          1         1         1       3h34m
replicaset.apps/ticketing-56f99d87d         1         1         1       92s

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   5%/50%    1         10        1          134m
```
```sh
root@siege:/# http put http://ticketing:8080/actuator/down
HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Jun 2021 08:15:55 GMT
Transfer-Encoding: chunked

{
    "status": "DOWN"
}
```
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          3h36m
pod/customercenter-56d47c8c6-89fn7   1/1     Running   0          112m
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          3h31m
pod/kakao-794487c556-z5vzt           1/1     Running   0          3h31m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          3h36m
pod/pay-56967ccf97-lllb4             1/1     Running   0          3h36m
pod/siege                            1/1     Running   0          3h29m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          3h36m
pod/ticketing-56f99d87d-lxp59        0/1     Running   1          3m23s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         5h11m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         3h53m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   3h52m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         3h45m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         3h49m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         3h49m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         3h47m
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP         15m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           3h36m
deployment.apps/customercenter   1/1     1            1           3h36m
deployment.apps/gateway          1/1     1            1           3h31m
deployment.apps/kakao            1/1     1            1           3h31m
deployment.apps/movie            1/1     1            1           3h36m
deployment.apps/pay              1/1     1            1           3h36m
deployment.apps/theater          1/1     1            1           3h36m
deployment.apps/ticketing        0/1     1            0           3m25s

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       3h36m
replicaset.apps/customercenter-56d47c8c6    1         1         1       141m
replicaset.apps/customercenter-7b89f74f46   0         0         0       3h36m
replicaset.apps/gateway-6d664c5d6f          1         1         1       3h31m
replicaset.apps/kakao-794487c556            1         1         1       3h31m
replicaset.apps/movie-ccbbc5bb9             1         1         1       3h36m
replicaset.apps/pay-56967ccf97              1         1         1       3h36m
replicaset.apps/theater-59b665f8d4          1         1         1       3h36m
replicaset.apps/ticketing-56f99d87d         1         1         0       3m26s

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   6%/50%    1         10        1          136m

kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          3h37m
pod/customercenter-56d47c8c6-89fn7   1/1     Running   0          112m
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          3h32m
pod/kakao-794487c556-z5vzt           1/1     Running   0          3h32m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          3h37m
pod/pay-56967ccf97-lllb4             1/1     Running   0          3h37m
pod/siege                            1/1     Running   0          3h29m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          3h37m
pod/ticketing-56f99d87d-lxp59        1/1     Running   1          3m52s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         5h11m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         3h53m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   3h53m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         3h46m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         3h49m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         3h50m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         3h48m
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP         16m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           3h37m
deployment.apps/customercenter   1/1     1            1           3h37m
deployment.apps/gateway          1/1     1            1           3h32m
deployment.apps/kakao            1/1     1            1           3h32m
deployment.apps/movie            1/1     1            1           3h37m
deployment.apps/pay              1/1     1            1           3h37m
deployment.apps/theater          1/1     1            1           3h37m
deployment.apps/ticketing        1/1     1            1           3m54s

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       3h37m
replicaset.apps/customercenter-56d47c8c6    1         1         1       141m
replicaset.apps/customercenter-7b89f74f46   0         0         0       3h37m
replicaset.apps/gateway-6d664c5d6f          1         1         1       3h32m
replicaset.apps/kakao-794487c556            1         1         1       3h32m
replicaset.apps/movie-ccbbc5bb9             1         1         1       3h37m
replicaset.apps/pay-56967ccf97              1         1         1       3h37m
replicaset.apps/theater-59b665f8d4          1         1         1       3h37m
replicaset.apps/ticketing-56f99d87d         1         1         1       3m55s

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   1%/50%    1         10        1          136m
```
```sh
root@siege:/# http http://ticketing:8080/actuator/health
HTTP/1.1 200
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Wed, 09 Jun 2021 08:19:03 GMT
Transfer-Encoding: chunked

{
    "status": "UP"
}
```
---
### zero-down (readiness)
- 사나리오
  - 발권서비스(ticketing )에서 제공되는 readinessProbe로 /actuator/health를 활용한다.(liveness는 제거)
  - 2개의 pod를 배포하여 두 서비스에 골고루 요청이 배분되는지 확인한다.
  - 중간에 하나의 서비스에 대해서 /actuator/health를 강제로 down시킬수 있는 url를 작성하여 readiness오류 발생시킨다.
  - 한쪽으로만 요청이 흘러가는지 확인한다.
```sh
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ticketing
  labels:
    app: ticketing
spec:
  #레플리카 2개로
  replicas: 2
  selector:
    matchLabels:
      app: ticketing
  template:
    metadata:
      labels:
        app: ticketing
    spec:
      containers:
        - name: ticketing
          image: 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-ticketing:v2
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          # livenessProbe:
          #   httpGet:
          #     path: '/actuator/health'
          #     port: 8080
          #   initialDelaySeconds: 120
          #   timeoutSeconds: 2
          #   periodSeconds: 5
          #   failureThreshold: 5
```
- 서비스 pod를 확인할 수 있도록 코드 추가
```java
  @GetMapping("/serviceAddress")
  public String getServiceAddress() {
    return findMyHostname() + "/" + findMyIpAddress();
  }

  private String findMyHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown host name";
    }
  }

  private String findMyIpAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return "unknown IP address";
    }
  }
```
- 파드 생성 확인
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          3h55m
pod/customercenter-56d47c8c6-89fn7   1/1     Running   0          130m
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          3h50m
pod/kakao-794487c556-z5vzt           1/1     Running   0          3h49m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          3h54m
pod/pay-56967ccf97-lllb4             1/1     Running   0          3h55m
pod/siege                            1/1     Running   0          3h47m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          3h54m
pod/ticketing-6769d67cdd-5bv9q       1/1     Running   0          3m44s
pod/ticketing-6769d67cdd-qtdv5       1/1     Running   0          2m15s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         5h29m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         4h11m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   4h11m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         4h4m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         4h7m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         4h8m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         4h6m
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP         34m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           3h55m
deployment.apps/customercenter   1/1     1            1           3h54m
deployment.apps/gateway          1/1     1            1           3h50m
deployment.apps/kakao            1/1     1            1           3h49m
deployment.apps/movie            1/1     1            1           3h55m
deployment.apps/pay              1/1     1            1           3h55m
deployment.apps/theater          1/1     1            1           3h54m
deployment.apps/ticketing        2/2     2            2           21m

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       3h55m
replicaset.apps/customercenter-56d47c8c6    1         1         1       159m
replicaset.apps/customercenter-7b89f74f46   0         0         0       3h54m
replicaset.apps/gateway-6d664c5d6f          1         1         1       3h50m
replicaset.apps/kakao-794487c556            1         1         1       3h49m
replicaset.apps/movie-ccbbc5bb9             1         1         1       3h55m
replicaset.apps/pay-56967ccf97              1         1         1       3h55m
replicaset.apps/theater-59b665f8d4          1         1         1       3h54m
replicaset.apps/ticketing-56f99d87d         0         0         0       21m
replicaset.apps/ticketing-6769d67cdd        2         2         2       3m47s

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   1%/50%    1         10        1          154m
```
- 1초 단위로 요청 전송
```sh
kinux@gram-kidshim:/mnt/c/Users/kidshim$ kubectl exec -it pod/siege -- /bin/bash
root@siege:/# while true; do curl http://ticketing:8080/serviceAddress; echo ""; sleep 1; done
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51   << health down
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-5bv9q/192.168.25.110
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
ticketing-6769d67cdd-qtdv5/192.168.80.51
```
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl exec -it pod/siege -- /bin/bash
root@siege:/# http put http://ticketing:8080/actuator/down
TTP/1.1 200HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Jun 2021 08:41:21 GMT
Transfer-Encoding: chunked

{
    "status": "DOWN"
}
```
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl get all
NAME                                 READY   STATUS    RESTARTS   AGE
pod/app-8f78b644d-kpbq4              1/1     Running   0          4h3m
pod/customercenter-56d47c8c6-89fn7   1/1     Running   0          139m
pod/gateway-6d664c5d6f-nmg58         1/1     Running   0          3h59m
pod/kakao-794487c556-z5vzt           1/1     Running   0          3h58m
pod/movie-ccbbc5bb9-42r9k            1/1     Running   0          4h3m
pod/pay-56967ccf97-lllb4             1/1     Running   0          4h3m
pod/siege                            1/1     Running   0          3h56m
pod/theater-59b665f8d4-wpr75         1/1     Running   0          4h3m
pod/ticketing-6769d67cdd-5bv9q       0/1     Running   0          12m
pod/ticketing-6769d67cdd-qtdv5       1/1     Running   0          11m

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP         5h38m
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP         4h20m
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP   4h20m
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP         4h13m
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP         4h16m
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP         4h16m
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP         4h15m
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP         43m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           4h3m
deployment.apps/customercenter   1/1     1            1           4h3m
deployment.apps/gateway          1/1     1            1           3h59m
deployment.apps/kakao            1/1     1            1           3h58m
deployment.apps/movie            1/1     1            1           4h3m
deployment.apps/pay              1/1     1            1           4h3m
deployment.apps/theater          1/1     1            1           4h3m
deployment.apps/ticketing        1/2     2            1           30m

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-8f78b644d               1         1         1       4h3m
replicaset.apps/customercenter-56d47c8c6    1         1         1       168m
replicaset.apps/customercenter-7b89f74f46   0         0         0       4h3m
replicaset.apps/gateway-6d664c5d6f          1         1         1       3h59m
replicaset.apps/kakao-794487c556            1         1         1       3h58m
replicaset.apps/movie-ccbbc5bb9             1         1         1       4h3m
replicaset.apps/pay-56967ccf97              1         1         1       4h3m
replicaset.apps/theater-59b665f8d4          1         1         1       4h3m
replicaset.apps/ticketing-56f99d87d         0         0         0       30m
replicaset.apps/ticketing-6769d67cdd        2         2         1       12m

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   1%/50%    1         10        1          163m
```
- 배포
  - 버전을 변경하여 배포시 siege를 이용하여 서비스 가용성 여부를 확인한다.
```sh
NAME                                  READY   STATUS    RESTARTS   AGE
pod/app-7f854969-chfdn                1/1     Running   0          8h
pod/customercenter-6468986d85-8c4hw   1/1     Running   0          8h
pod/gateway-6d664c5d6f-t4crg          1/1     Running   0          9h
pod/kakao-67647f56b9-bpx7x            1/1     Running   0          9h
pod/movie-ccbbc5bb9-52vtm             1/1     Running   0          9h
pod/pay-56967ccf97-mslcp              1/1     Running   0          9h
pod/siege-5b99b44c9c-pv7ll            1/1     Running   0          9h
pod/theater-59b665f8d4-nxflt          1/1     Running   0          9h
pod/ticketing-5894c96f9-c57w5         1/1     Running   0          8h

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)
 AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP
 21h
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP
 19h
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP
 19h
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP
 19h
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP
 19h
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP
 19h
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP
 19h
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP
 16h

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           8h
deployment.apps/customercenter   1/1     1            1           11h
deployment.apps/gateway          1/1     1            1           19h
deployment.apps/kakao            1/1     1            1           13h
deployment.apps/movie            1/1     1            1           19h
deployment.apps/pay              1/1     1            1           19h
deployment.apps/siege            1/1     1            1           9h
deployment.apps/theater          1/1     1            1           19h
deployment.apps/ticketing        1/1     1            1           8h

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-7f854969                1         1         1       8h
replicaset.apps/customercenter-6468986d85   1         1         1       11h
replicaset.apps/gateway-6d664c5d6f          1         1         1       19h
replicaset.apps/kakao-67647f56b9            1         1         1       13h
replicaset.apps/movie-ccbbc5bb9             1         1         1       19h
replicaset.apps/pay-56967ccf97              1         1         1       19h
replicaset.apps/siege-5b99b44c9c            1         1         1       9h
replicaset.apps/theater-59b665f8d4          1         1         1       19h
replicaset.apps/ticketing-5894c96f9         1         1         1       8h
replicaset.apps/ticketing-5bc97d5d7d        1         1         0       2s

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   1%/50%    1         10        1          18h
```
- 버전 배포 중
```sh
Every 2.0s: kubectl get all                                                                                      gram-kidshim: Thu Jun 10 09:10:01 2021

NAME                                  READY   STATUS        RESTARTS   AGE
pod/app-7f854969-chfdn                1/1     Running       0          9h
pod/customercenter-6468986d85-8c4hw   1/1     Running       0          8h
pod/gateway-6d664c5d6f-t4crg          1/1     Running       0          9h
pod/kakao-67647f56b9-bpx7x            1/1     Running       0          9h
pod/movie-ccbbc5bb9-52vtm             1/1     Running       0          9h
pod/pay-56967ccf97-mslcp              1/1     Running       0          9h
pod/siege-5b99b44c9c-pv7ll            1/1     Running       0          9h
pod/theater-59b665f8d4-nxflt          1/1     Running       0          9h
pod/ticketing-5894c96f9-c57w5         0/1     Terminating   0          8h
pod/ticketing-5bc97d5d7d-glzj4        1/1     Running       0          34s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)
 AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP
 21h
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP
 19h
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP
 19h
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP
 19h
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP
 19h
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP
 19h
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP
 19h
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP
 16h

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           9h
deployment.apps/customercenter   1/1     1            1           11h
deployment.apps/gateway          1/1     1            1           19h
deployment.apps/kakao            1/1     1            1           13h
deployment.apps/movie            1/1     1            1           19h
deployment.apps/pay              1/1     1            1           19h
deployment.apps/siege            1/1     1            1           9h
deployment.apps/theater          1/1     1            1           19h
deployment.apps/ticketing        1/1     1            1           8h

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-7f854969                1         1         1       9h
replicaset.apps/customercenter-6468986d85   1         1         1       11h
replicaset.apps/gateway-6d664c5d6f          1         1         1       19h
replicaset.apps/kakao-67647f56b9            1         1         1       13h
replicaset.apps/movie-ccbbc5bb9             1         1         1       19h
replicaset.apps/pay-56967ccf97              1         1         1       19h
replicaset.apps/siege-5b99b44c9c            1         1         1       9h
replicaset.apps/theater-59b665f8d4          1         1         1       19h
replicaset.apps/ticketing-5894c96f9         0         0         0       8h
replicaset.apps/ticketing-5bc97d5d7d        1         1         1       37s

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   1%/50%    1         10        1          18h
```
- 버전 배포 완료
```sh
Every 2.0s: kubectl get all                                                                                      gram-kidshim: Thu Jun 10 09:10:17 2021

NAME                                  READY   STATUS    RESTARTS   AGE
pod/app-7f854969-chfdn                1/1     Running   0          9h
pod/customercenter-6468986d85-8c4hw   1/1     Running   0          8h
pod/gateway-6d664c5d6f-t4crg          1/1     Running   0          9h
pod/kakao-67647f56b9-bpx7x            1/1     Running   0          9h
pod/movie-ccbbc5bb9-52vtm             1/1     Running   0          9h
pod/pay-56967ccf97-mslcp              1/1     Running   0          9h
pod/siege-5b99b44c9c-pv7ll            1/1     Running   0          9h
pod/theater-59b665f8d4-nxflt          1/1     Running   0          9h
pod/ticketing-5bc97d5d7d-glzj4        1/1     Running   0          51s

NAME                     TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)
 AGE
service/app              ClusterIP      10.100.87.107    <none>                                                                        8080/TCP
 21h
service/customercenter   ClusterIP      10.100.226.126   <none>                                                                        8080/TCP
 19h
service/gateway          LoadBalancer   10.100.153.200   a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com   8080:32682/TCP
 19h
service/kakao            ClusterIP      10.100.158.132   <none>                                                                        8080/TCP
 19h
service/movie            ClusterIP      10.100.105.95    <none>                                                                        8080/TCP
 19h
service/pay              ClusterIP      10.100.190.109   <none>                                                                        8080/TCP
 19h
service/theater          ClusterIP      10.100.48.162    <none>                                                                        8080/TCP
 19h
service/ticketing        ClusterIP      10.100.139.237   <none>                                                                        8080/TCP
 16h

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app              1/1     1            1           9h
deployment.apps/customercenter   1/1     1            1           11h
deployment.apps/gateway          1/1     1            1           19h
deployment.apps/kakao            1/1     1            1           13h
deployment.apps/movie            1/1     1            1           19h
deployment.apps/pay              1/1     1            1           19h
deployment.apps/siege            1/1     1            1           9h
deployment.apps/theater          1/1     1            1           19h
deployment.apps/ticketing        1/1     1            1           8h

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/app-7f854969                1         1         1       9h
replicaset.apps/customercenter-6468986d85   1         1         1       11h
replicaset.apps/gateway-6d664c5d6f          1         1         1       19h
replicaset.apps/kakao-67647f56b9            1         1         1       13h
replicaset.apps/movie-ccbbc5bb9             1         1         1       19h
replicaset.apps/pay-56967ccf97              1         1         1       19h
replicaset.apps/siege-5b99b44c9c            1         1         1       9h
replicaset.apps/theater-59b665f8d4          1         1         1       19h
replicaset.apps/ticketing-5894c96f9         0         0         0       8h
replicaset.apps/ticketing-5bc97d5d7d        1         1         1       54s

NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/customercenter   Deployment/customercenter   1%/50%    1         10        1          18h
```
- 요청 유실 없이 정상 배포된 것을 확인 할 수 있다.
``` sh
HTTP/1.1 200     0.00 secs:     228 bytes ==> GET  /printTickets

Lifting the server siege...
Transactions:                  16648 hits
Availability:                 100.00 %
Elapsed time:                  59.21 secs
Data transferred:               3.62 MB
Response time:                  0.00 secs
Transaction rate:             281.17 trans/sec
Throughput:                     0.06 MB/sec
Concurrency:                    0.98
Successful transactions:       16648
Failed transactions:               0
Longest transaction:            0.49
Shortest transaction:           0.00
```
---
### 장애격리(Circuit Breaker)
- 시나리오
  - Spring FeignClient + Hystrix 옵션을 사용
  - 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```yaml
hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
feign:
  customercenter-api:
    url: http://customercenter:8080
  httpclient:
    connection-timeout: 1
  hystrix:
    enabled: true
  client:
    config:
      default:
        loggerLevel: FULL
```
- ticketing(feign client)
```java
package theatermy.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import theatermy.config.HttpConfiguration;

@FeignClient(name = "customercenter-api", url = "${feign.customercenter-api.url}",
configuration = HttpConfiguration.class,
fallbackFactory = BookInfoServiceFallbackFactory.class)
public interface BookInfoService {

  @GetMapping("/bookInfo")
  public String searchBook(@RequestParam("bookId") String bookId);

  @GetMapping("/isolation")
  String isolation();
}
```
```java
//fallback
package theatermy.external;

import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;

@Component
public class BookInfoServiceFallbackFactory implements FallbackFactory<BookInfoService> {

  @Override
  public BookInfoService create(Throwable cause) {
    System.out.println("========= FallbackFactory called: Confirm ticketing Service =========");
    return new BookInfoService() {
      @Override
      public String searchBook(String bookId) {
        return "Fallback - ticketing";
      }

      @Override
      public String isolation() {
          return "fallback - isolation";
      }
    };
  }
}
```
- customercenter(feign server)
```java
  @RequestMapping(value = "/isolation", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
  public String isolation(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Random rng = new Random();
    long loopCnt = 0;

    while (loopCnt < 100) {
      double r = rng.nextFloat();
      double v = Math.sin(Math.cos(Math.sin(Math.cos(r))));
      System.out.println(String.format("r: %f, v %f", r, v));
      loopCnt++;
    }

    return "real";
  }
```
- 부하테스트 실행
```sh
root@siege:/# siege -c50 -t20S -r10 -v --content-type "application/json" http://ticketing:8080/isolations
HTTP/1.1 200     0.02 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.01 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.01 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.07 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.09 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.09 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.10 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 500     0.18 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 200     0.05 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.06 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.06 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.05 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.08 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.05 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.14 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.02 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.05 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.08 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.06 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.07 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.06 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.07 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.05 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.07 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.07 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.10 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.24 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 500     0.34 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 500     0.22 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 200     0.05 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.08 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.07 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.10 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.05 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 500     0.32 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 500     0.34 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 200     0.08 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 500     0.22 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 500     0.48 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 500     0.38 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 500     0.48 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 200     0.11 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.10 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.08 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.03 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.04 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.07 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.06 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.12 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.13 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.18 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 500     0.68 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 200     0.14 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.14 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.11 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.10 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.13 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.15 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 200     0.14 secs:      20 bytes ==> GET  /isolations
HTTP/1.1 500     0.52 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 500     0.62 secs:     179 bytes ==> GET  /isolations
HTTP/1.1 500     0.13 secs:     179 bytes ==> GET  /isolations

Lifting the server siege...
Transactions:                   4931 hits
Availability:                  85.25 %
Elapsed time:                  19.97 secs
Data transferred:               0.21 MB
Response time:                  0.20 secs
Transaction rate:             246.92 trans/sec
Throughput:                     0.01 MB/sec
Concurrency:                   48.49
Successful transactions:        4931
Failed transactions:             853
Longest transaction:            1.89
Shortest transaction:           0.00
```
---
### Config Map 
- 사나리오
  - 고객센터의 번호를 config map에 등록하여 사용한다.(고객 센터 전화번호는 가끔 바뀔 수 있다.)
- Config Map 생성 및 확인
```sh
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl create configmap call-number-cm --from-literal=callNum=010-123-4567
configmap/call-number-cm created
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim/ticketing$ kubectl get cm
NAME             DATA   AGE
call-number-cm   1      9s
```
- deployment.yml
```yml
      containers:
        - name: customercenter
          image: 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-customercenter:v1
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
          resources:
            limits:
              cpu: "500m"
            requests:
              cpu: "200m"
          env:
          - name: CALLNUM
            valueFrom:
              configMapKeyRef:
                name: call-number-cm
                key: callNum
```
- apllication.yml
```yaml
call:
  number: ${CALLNUM}
```
- BookInfoController.java
```java
@Value("${call.number}")
String callNumber;

@GetMapping("/call-number")
public String getCallNumber() {
    return callNumber;
}
```
- CM값 확인
```sh
root@siege:/# http http://customercenter:8080/call-number
HTTP/1.1 200
Content-Length: 12
Content-Type: text/plain;charset=UTF-8
Date: Wed, 09 Jun 2021 09:51:02 GMT

010-123-4567
```
---
###  Polyglot
- 시나리오
  - 고객센터 서비스는 외부저장소(mariadb)를 이용하여 서비스 한다.
- 헥사고날 아키텍처 변화(폴리글랏 퍼시스턴스)
![image](https://user-images.githubusercontent.com/80908892/121360217-2ee14480-c96f-11eb-9d25-2d99ca9bfa8d.png)

```yaml
spring:
  profiles: docker
  datasource:
    name: mantis
    initialization-mode: always      
    password: XXXXXXX
    tomcat:
      max-active: 50
      max-idle: 20
      max-wait: 20000
      min-idle: 15
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://confidy.cafe24.com:3306/mantis?characterEncoding=UTF-8&serverTimezone=UTC
    username: neoshim
  jpa:
    open-in-view: false
    generate-ddl: true
    show-sql: true
    hibernate:
      ddl-auto: update
```
```log
2021-06-09 12:36:18.971  INFO 1 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate Core {5.3.12.Final}
2021-06-09 12:36:18.974  INFO 1 --- [           main] org.hibernate.cfg.Environment            : HHH000206: hibernate.properties not found
2021-06-09 12:36:19.784  INFO 1 --- [           main] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.0.4.Final}
2021-06-09 12:36:21.350  INFO 1 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.MariaDB53Dialect
Hibernate: create table book_info_table (book_id varchar(255) not null, customer_id varchar(255), movie_id varchar(255), seat_id varchar(255), status varchar(255), primary key (book_id)) engine=InnoDB
Hibernate: create table mypage_table (id bigint not null, book_id varchar(255), customer_id varchar(255), movie_id varchar(255), pay_id varchar(255), screen_id varchar(255), seat_id varchar(255), status varchar(255), primary key (id)) engine=InnoDB
2021-06-09 12:36:25.231  INFO 1 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2021-06-09 12:36:30.662  WARN 1 --- [           main] o.s.c.n.a.ArchaiusAutoConfiguration      : No spring.application.name found, defaulting to 'application'
2021-06-09 12:36:30.669  WARN 1 --- [           main] c.n.c.sources.URLConfigurationSource     : No URLs will be polled as dynamic configuration sources.
2021-06-09 12:36:30.669  INFO 1 --- [           main] c.n.c.sources.URLConfigurationSource     : To enable URLs as dynamic configuration sources, define System property archaius.configurationSource.additionalUrls or make config.properties available on classpath.
```

```shell
kinux@gram-kidshim:/mnt/d/dev_room/study/assessment/kidshim$ http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="C1002" customerId="D1002" movieId="MOVIE-00002" seatId="E-1"
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Wed, 09 Jun 2021 12:41:43 GMT
transfer-encoding: chunked

{
    "bookId": "C1002",
    "bookedYn": "Y",
    "customerId": "D1002",
    "id": 6,
    "movieId": "MOVIE-00002",
    "payId": "e2bdec83-7a9c-4e78-808b-627ad786e6d4",
    "seatId": "E-1"
}
```
![image](https://user-images.githubusercontent.com/80908892/121360412-5df7b600-c96f-11eb-8c9a-0dcd052760c9.png)
