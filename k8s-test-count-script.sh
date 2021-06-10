#!/usr/bin/env bash
#

echo "reservations: $(http http://localhost:8080/reservations | jq .page.totalElements)"
echo "approvals: $(http http://localhost:8080/approvals | jq .page.totalElements)"
echo "movieManagements: $(http http://localhost:8080/movieManagements | jq .page.totalElements)"
echo "movieSeats: $(http http://localhost:8080/movieSeats | jq .page.totalElements)"
echo "movies: $(http http://localhost:8080/movies | jq .page.totalElements)"
echo "bookInfos: $(http http://localhost:8080/bookInfos | jq .page.totalElements)"
echo "tickets: $(http http://localhost:8080/tickets | jq .page.totalElements)"
