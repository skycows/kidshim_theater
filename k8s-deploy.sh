#!/usr/bin/env bash
#

cd app
kubectl apply -f ./kubernetes
cd ..
cd pay
kubectl apply -f ./kubernetes
cd ..
cd movie
kubectl apply -f ./kubernetes
cd ..
cd theater
kubectl apply -f ./kubernetes
cd ..
cd customercenter
kubectl apply -f ./kubernetes
cd ..
cd ticketing
kubectl apply -f ./kubernetes
cd ..
cd gateway
kubectl create deploy gateway --image=kidshim/gateway:v1
kubectl expose deploy gateway --type="LoadBalancer" --port=8080
cd ..
kubectl create deploy kakao --image=kidshim/kakao:v1
kubectl expose deploy kakao --type="ClusterIP" --port=8080
cd ..