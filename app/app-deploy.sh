#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-app:v2 .
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-app:v2

kubectl delete deploy app
kubectl apply -f ./kubernetes