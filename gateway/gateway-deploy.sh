#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/gateway:v1 .
docker push kidshim/gateway:v1

kubectl delete deploy gateway
kubectl create deploy gateway --image=kidshim/gateway:v1
kubectl expose deploy gateway --type="LoadBalancer" --port=8080