#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/app:v1 .
docker push kidshim/app:v1

kubectl delete deploy app
kubectl apply -f ./kubernetes