#!/usr/bin/env bash
#

docker build -t kidshim/kakao:v1 .
docker push kidshim/kakao:v1

kubectl delete deploy, svc kakao
kubectl create deploy kakao --image=kidshim/kakao:v1
kubectl expose deploy kakao --type="ClusterIP" --port=8080