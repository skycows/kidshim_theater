package theater;

import javax.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import theater.external.Approval;
import theater.external.ApprovalService;

@Entity
@Table(name = "Reservation_table")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String movieId;
    
    @Column(nullable = false, unique = true)
    private String payId;

    @Column(nullable = false, unique = true)
    private String seatId;

    private String bookedYn;

    @PostLoad
    public void onPostLoad() {
        Logger logger = LoggerFactory.getLogger("Reservation");
        logger.info("Load");
    }

//    @PrePersist
//    public void onPrePersist() throws JsonProcessingException {
//        Logger logger = LoggerFactory.getLogger("Reservation");
//        logger.info("Make Reservation");
//
//        Reserved reserved = new Reserved();
//        BeanUtils.copyProperties(this, reserved);
//
//        Approval approvalFromPay = AppApplication.applicationContext.getBean(ApprovalService.class)
//                .paymentRequest(this.bookId, this.movieId, this.seatId);
//
//        if (approvalFromPay != null) {
//            this.setBookedYn("Y");
//            this.setPayId(approvalFromPay.getPayId());
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            String approvalMessage = objectMapper.writeValueAsString(approvalFromPay);
//            String reservedMessage = objectMapper.writeValueAsString(reserved);
//
//            logger.info("======= Pay Success. " + approvalMessage);
//            logger.info("======= Pay Success. " + reservedMessage);
//        } else {
//            logger.info("======= Pay didn't Approve. Confirm Pay Service.=======");
//        }
//    }
}