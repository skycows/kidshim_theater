#!/usr/bin/env bash
#

kubectl get nodes
kubectl config current-context
kubectl get all
kubectl create ns kafka
kubectl create ns kidshim-ns
kubectl get ns
kubectl config set-context $(kubectl config current-context) --namespace=kidshim-ns

echo "install helm ..."
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh

echo "install kafka ..."
helm repo add incubator https://charts.helm.sh/incubator
helm install my-kafka --namespace kafka incubator/kafka
kubectl get all -n kafka