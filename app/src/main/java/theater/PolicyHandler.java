package theater;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import theater.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler {
    @Autowired
    ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverApproved_Updatestate(@Payload Approved approved) {

        if (!approved.validate())
            return;

        System.out.println("\n\n##### listener Updatestate : " + approved.toJson() + "\n\n");
    }

    @StreamListener(KafkaProcessor.INPUT)
    @Transactional
    public void wheneverPaymentCanceled_Updatestate(@Payload PaymentCanceled paymentCanceled) {

        if (!paymentCanceled.validate())
            return;

        Reservation ticket = reservationRepository.findByBookId(paymentCanceled.getBookId()).orElse(null);
        ticket.setBookedYn("N");
        System.out.println("\n\n##### listener Updatestate : " + paymentCanceled.toJson() + "\n\n");
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {
    }

}
