#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/movie:v1 .
docker push kidshim/movie:v1

kubectl delete deploy movie
kubectl apply -f ./kubernetes