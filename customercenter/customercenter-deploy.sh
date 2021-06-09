#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-customercenter:v4 .
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-customercenter:v4

kubectl delete deploy customercenter
kubectl apply -f ./kubernetes