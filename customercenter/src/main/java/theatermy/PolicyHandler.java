package theatermy;

import theatermy.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler {
  @Autowired
  BookInfoRepository bookInfoRepository;

  @StreamListener(KafkaProcessor.INPUT)
  public void wheneverReserved_ChangeState(@Payload Reserved reserved) {
    System.out.println("\n\n##### theatermy ChangeState1 : " + reserved.toJson() + "\n\n");
    if (!reserved.validate())
      return;

    System.out.println("\n\n##### theatermy ChangeState2 : " + reserved.toJson() + "\n\n");
    /**
     * [Reserved] 
     * private String bookId; 
     * private String customerId; 
     * private String movieId; 
     * private String seatId;
     */
    BookInfo bookInfo = new BookInfo();
    bookInfo.setBookId(reserved.getBookId());
    bookInfo.setCustomerId(reserved.getCustomerId());
    bookInfo.setMovieId(reserved.getMovieId());
    bookInfo.setSeatId(reserved.getSeatId());
    bookInfo.setStatus("Reserved");
    bookInfoRepository.save(bookInfo);

  }

  @StreamListener(KafkaProcessor.INPUT)
  public void wheneverSeatCanceled_ChangeState(@Payload SeatCanceled seatCanceled) {

    if (!seatCanceled.validate())
      return;

    System.out.println("\n\n##### listener ChangeState : " + seatCanceled.toJson() + "\n\n");

    // Sample Logic //
    BookInfo bookInfo = new BookInfo();
    bookInfo.setBookId(seatCanceled.getBookId());
    bookInfo.setStatus("Canceled");
    bookInfoRepository.save(bookInfo);
  }

  // @StreamListener(KafkaProcessor.INPUT)
  // public void whatever(@Payload String eventString){
  // }
}
