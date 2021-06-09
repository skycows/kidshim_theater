#!/usr/bin/env bash
#

echo "reservations: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations | jq .page.totalElements)"
echo "approvals: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/approvals | jq .page.totalElements)"
echo "movieManagements: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements | jq .page.totalElements)"
echo "movieSeats: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieSeats | jq .page.totalElements)"
echo "movies: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movies | jq .page.totalElements)"
echo "bookInfos: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/bookInfos | jq .page.totalElements)"
echo "tickets: $(http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/tickets | jq .page.totalElements)"
