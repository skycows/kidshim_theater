#!/usr/bin/env bash
#

docker login --username AWS -p $(aws ecr get-login-password --region ap-southeast-2)  879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/

cd app
mvn clean && mvn package
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-app:v1 .
aws ecr create-repository --repository-name user10-app --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-app:v1
cd ..
cd pay
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-pay:v1 . 
aws ecr create-repository --repository-name user10-pay --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-pay:v1 
cd ..
cd movie
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-movie:v1 . 
aws ecr create-repository --repository-name user10-movie --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-movie:v1 
cd ..
cd theater
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-theater:v1 . 
aws ecr create-repository --repository-name user10-theater --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-theater:v1 
cd ..
cd gateway
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-gateway:v1 . 
aws ecr create-repository --repository-name user10-gateway --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-gateway:v1 
cd ..
cd kakao
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-kakao:v1 .
aws ecr create-repository --repository-name user10-kakao --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-kakao:v1
cd ..
cd customercenter
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-customercenter:v1 .
aws ecr create-repository --repository-name user10-customercenter --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-customercenter:v1
cd ..
cd ticketing
docker build -t 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-ticketing:v1 .
aws ecr create-repository --repository-name user10-ticketing --region ap-southeast-2
docker push 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user10-ticketing:v1
cd ..
