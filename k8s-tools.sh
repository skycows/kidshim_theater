#!/usr/bin/env bash
#

kubectl run siege --image=apexacme/siege-nginx
kubectl exec -it pod/siege -c siege -- /bin/bash