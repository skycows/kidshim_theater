package theater;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
public class Book {
    private String bookId;
    private String customerId;
    private String movieId;
    private String payId;
    private String seatId;
    private String bookedYn;
}
