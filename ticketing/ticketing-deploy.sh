#!/usr/bin/env bash
#

mvn clean && mvn package
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-ticketing:v4 .
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-ticketing:v4
# kubectl delete deploy ticketing
# kubectl apply -f ./kubernetes