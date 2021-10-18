#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/ticketing:v2 .
#docker push kidshim/ticketing:v2
kubectl delete deploy ticketing
kubectl apply -f ./kubernetes