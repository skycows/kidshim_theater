#!/usr/bin/env bash
#

http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/approvals
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieSeats
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movies
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/mypages
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/bookInfos
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/printTickets
http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/tickets

# 영화등록
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements movieId="MOVIE-00001" title="어벤져스" status="RUNNING"
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements movieId="MOVIE-00002" title="아이언맨" status="RUNNING"
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements movieId="MOVIE-00003" title="토르" status="WAITING"

# 영화예매
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="B1003" customerId="C1003" movieId="MOVIE-00003" seatId="C-1"
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="B1001" customerId="C1001" movieId="MOVIE-00001" seatId="A-1"
http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="B1002" customerId="C1002" movieId="MOVIE-00002" seatId="B-1" 
http GET http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations


# http DELETE http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/B1001
# http GET http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations

# http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/approvals 
# http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieSeats
# http http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/movieManagements 

http POST http://a97b4a1d2512e4683b978ebc12ecd83b-646188197.ap-southeast-2.elb.amazonaws.com:8080/reservations/new bookId="B1004" customerId="C1004" movieId="MOVIE-00002" seatId="B-4" 