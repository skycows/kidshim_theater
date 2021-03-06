package theater;

import java.util.Optional;

import javax.transaction.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import theater.exception.ApprovalBadRequestException;
import theater.exception.ReservationNotFoundException;
import theater.external.Approval;
import theater.external.ApprovalService;

@RestController
@Slf4j
public class ReservationController {
    private ReservationRepository reservationRepository;
    private ApprovalService approvalService;

    public ReservationController(ReservationRepository reservationRepository, ApprovalService approvalService){
        this.reservationRepository = reservationRepository;
        this.approvalService = approvalService;
    }

    @GetMapping("/isolations")
    String isolation() {
        return approvalService.isolation();
    }

    @PostMapping("/reservations/new")
    public ResponseEntity<?> createReservation(@RequestBody Book book) throws JsonProcessingException {
        log.info("Make Reservation");

        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(book, reserved);

        Approval approvalFromPay = AppApplication.applicationContext.getBean(ApprovalService.class)
                .paymentRequest(book.getBookId(), book.getMovieId(), book.getSeatId());

        if (approvalFromPay != null) {
            Reservation reservation = Reservation.builder()
                    .bookId(book.getBookId())
                    .movieId(book.getMovieId())
                    .seatId(book.getSeatId())
                    .payId(approvalFromPay.getPayId())
                    .bookedYn("Y")
                    .customerId(book.getCustomerId())
                    .build();

//            this.setBookedYn("Y");
//            this.setPayId(approvalFromPay.getPayId());
            reservationRepository.save(reservation);
            ObjectMapper objectMapper = new ObjectMapper();
            String approvalMessage = objectMapper.writeValueAsString(approvalFromPay);
            String reservedMessage = objectMapper.writeValueAsString(reserved);

            log.info("======= Pay Success. " + approvalMessage);
            log.info("======= Pay Success. " + reservedMessage);

            //Send Event
            reserved.setBookId(book.getBookId());
            reserved.setMovieId(book.getMovieId());
            reserved.setCustomerId(book.getCustomerId());
            reserved.setSeatId(book.getSeatId());
            reserved.publish();            

            return ResponseEntity.ok(reservation);
        } else {

            log.info("========= Pay didn't Approve. Confirm Pay Service.=========");
//            return ResponseEntity.badRequest().build();
            throw new ApprovalBadRequestException(String.format("[%s] Pay disapproved, You must confirm Pay Service", book.getBookId()));
        }
    }

    @DeleteMapping("/reservations/{bookId}")
    @Transactional
    public ResponseEntity<?> cancelReservation(@PathVariable("bookId")String bookId) throws JsonProcessingException {
        Optional<Reservation> optional = reservationRepository.findByBookId(bookId);

        if(optional.isPresent()){
            Reservation ticket = optional.get();

            log.info("Make Canceled");

            Canceled canceled = new Canceled();
            BeanUtils.copyProperties(ticket, canceled);

            ObjectMapper objectMapper = new ObjectMapper();
            String canceledMessage = objectMapper.writeValueAsString(canceled);
            log.info(canceledMessage);

            canceled.publishAfterCommit();

            return ResponseEntity.noContent().build();
        }else{
            log.info("Cancel is Fail");
            throw new ReservationNotFoundException(String.format("[%s] Reservation Not Found", bookId));
        }
    }
}
