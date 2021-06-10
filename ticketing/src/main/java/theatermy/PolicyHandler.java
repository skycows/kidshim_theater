package theatermy;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import theatermy.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler {
  @Autowired
  TicketRepository ticketRepository;

  @StreamListener(KafkaProcessor.INPUT)
  public void wheneverSeatReserved_TicketCreate(@Payload SeatReserved seatReserved) {
    System.out.println("\n\n##### TicketRepository TicketCreate1 : " + seatReserved.toJson() + "\n\n");

    if (!seatReserved.validate())
      return;

    Optional<Ticket> opTicket = ticketRepository.findByBookId(seatReserved.getBookId());
    if (opTicket.isPresent()) {
      Ticket ticket = opTicket.get();
      ticket.setSeatId("cancelled");
      ticketRepository.save(ticket);
    }

    System.out.println("\n\n##### TicketRepository TicketCreate2 : " + seatReserved.toJson() + "\n\n");
  }

  @StreamListener(KafkaProcessor.INPUT)
  public void wheneverReserved_TicketCreate(@Payload Reserved reserved) {
    System.out.println("\n\n##### TicketRepository reserved1 : " + reserved.toJson() + "\n\n");
    if (!reserved.validate())
      return;

    Ticket ticket = new Ticket();
    ticket.setBookId(reserved.getBookId());
    ticket.setMovieId(reserved.getMovieId());
    ticket.setSeatId(reserved.getSeatId());
    ticketRepository.save(ticket);
    System.out.println("\n\n##### TicketRepository reserved2 : " + reserved.toJson() + "\n\n");
  }

  @StreamListener(KafkaProcessor.INPUT)
  public void whatever(@Payload String eventString) {
  }

}
