#!/usr/bin/env bash
#

http http://localhost:8081/reservations
http http://localhost:8082/approvals
http http://localhost:8083/movieManagements
http http://localhost:8084/movieSeats
http http://localhost:8084/movies
http http://localhost:8086/mypages
http http://localhost:8086/bookInfos
http http://localhost:8087/printTickets
http http://localhost:8087/tickets

# 영화등록
http POST http://localhost:8083/movieManagements movieId="MOVIE-00001" title="어벤져스" status="RUNNING"
http POST http://localhost:8083/movieManagements movieId="MOVIE-00002" title="아이언맨" status="RUNNING"
http POST http://localhost:8083/movieManagements movieId="MOVIE-00003" title="토르" status="WAITING"

# 영화예매
http POST http://localhost:8081/reservations/new bookId="B1003" customerId="C1003" movieId="MOVIE-00003" seatId="C-1"
http POST http://localhost:8081/reservations/new bookId="B1001" customerId="C1001" movieId="MOVIE-00001" seatId="A-1"
http POST http://localhost:8081/reservations/new bookId="B1002" customerId="C1002" movieId="MOVIE-00002" seatId="B-1" 
http GET http://localhost:8081/reservations


# http DELETE http://localhost:8080/reservations/B1001
# http GET http://localhost:8080/reservations

# http http://localhost:8080/approvals 
# http http://localhost:8080/movieSeats
# http http://localhost:8080/movieManagements 
