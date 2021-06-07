package theatermy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import theatermy.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler{
    @Autowired TicketRepository ticketRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverSeatReserved_TicketCreate(@Payload SeatReserved seatReserved){
        System.out.println("\n\n##### TicketRepository TicketCreate1 : " + seatReserved.toJson() + "\n\n");
        if(!seatReserved.validate()) return;

        System.out.println("\n\n##### TicketRepository TicketCreate2 : " + seatReserved.toJson() + "\n\n");

        // Sample Logic //
        Ticket ticket = new Ticket();
        ticketRepository.save(ticket);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
