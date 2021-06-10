#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/app:v2 .
docker push kidshim/app:v2

kubectl delete deploy app
kubectl apply -f ./kubernetes