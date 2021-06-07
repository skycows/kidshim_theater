#!/usr/bin/env bash
#

echo "reservations: $(http http://localhost:8081/reservations | jq .page.totalElements)"
echo "approvals: $(http http://localhost:8082/approvals | jq .page.totalElements)"
echo "movieManagements: $(http http://localhost:8083/movieManagements | jq .page.totalElements)"
echo "movieSeats: $(http http://localhost:8084/movieSeats | jq .page.totalElements)"
echo "movies: $(http http://localhost:8084/movies | jq .page.totalElements)"
echo "bookInfos: $(http http://localhost:8086/bookInfos | jq .page.totalElements)"
echo "tickets: $(http http://localhost:8087/tickets | jq .page.totalElements)"
