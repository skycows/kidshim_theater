#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/theater:v1 .
docker push kidshim/theater:v1

kubectl delete deploy theater
kubectl apply -f ./kubernetes