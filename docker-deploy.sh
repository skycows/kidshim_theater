#!/usr/bin/env bash
#

cd app
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
cd notice
docker build -t kidshim/notice:v1 . 
docker push kidshim/notice:v1 
cd ..
cd gateway
docker build -t kidshim/gateway:v1 . 
docker push kidshim/gateway:v1 
cd ..
cd kakao
docker build -t kidshim/kakao:v1 .
docker push kidshim/kakao:v1
cd ..