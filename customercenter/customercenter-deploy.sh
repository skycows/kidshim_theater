#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/customercenter:v2 .
# docker push kidshim/customercenter:v2

kubectl delete deploy customercenter
kubectl apply -f ./kubernetes