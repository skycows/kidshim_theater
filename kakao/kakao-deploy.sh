#!/usr/bin/env bash
#

docker build -t kidshim/kakao:v2 .
docker push kidshim/kakao:v2

kubectl delete deploy kakao
kubectl create deploy kakao --image=kidshim/kakao:v2
