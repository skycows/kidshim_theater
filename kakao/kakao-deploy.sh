#!/usr/bin/env bash
#

docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-kakao:v2 .
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-kakao:v2

kubectl delete deploy kakao
kubectl create deploy kakao --image=879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-kakao:v2
