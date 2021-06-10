#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t kidshim/ticketing:v4 .
docker push kidshim/ticketing:v4
# kubectl delete deploy ticketing
# kubectl apply -f ./kubernetes