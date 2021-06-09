#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-gateway:v1 .
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-gateway:v1

kubectl delete deploy gateway
kubectl create deploy gateway --image=879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-gateway:v1
kubectl delete svc gateway
kubectl expose deploy gateway --type="LoadBalancer" --port=8080