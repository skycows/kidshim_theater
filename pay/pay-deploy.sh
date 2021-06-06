#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/pay:v1 .
docker push kidshim/pay:v1

kubectl delete deploy pay
kubectl apply -f ./kubernetes