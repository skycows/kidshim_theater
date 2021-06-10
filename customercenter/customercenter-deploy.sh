#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/customercenter:v4 .
docker push kidshim/customercenter:v4

kubectl delete deploy customercenter
kubectl apply -f ./kubernetes