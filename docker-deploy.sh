#!/usr/bin/env bash
#

docker login --username AWS -p $(aws ecr get-login-password --region ap-southeast-2)  879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/

cd app
mvn clean && mvn package
docker build -t kidshim/app:v1 .
docker push kidshim/app:v1
cd ..
cd pay
docker build -t kidshim/pay:v1 . 
docker push kidshim/pay:v1 
cd ..
cd movie
docker build -t kidshim/movie:v1 . 
docker push kidshim/movie:v1 
cd ..
cd theater
docker build -t kidshim/theater:v1 . 
docker push kidshim/theater:v1 
cd ..
cd gateway
docker build -t kidshim/gateway:v1 . 
docker push kidshim/gateway:v1 
cd ..
cd kakao
docker build -t kidshim/kakao:v1 .
docker push kidshim/kakao:v1
cd ..
cd customercenter
docker build -t kidshim/customercenter:v1 .
docker push kidshim/customercenter:v1
cd ..
cd ticketing
docker build -t kidshim/ticketing:v1 .
docker push kidshim/ticketing:v1
cd ..
