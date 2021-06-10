package theatermy;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Ticket_table")
public class Ticket {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String bookId;
    private String movieId;
    private String screenId;
    private String seatId;

    @PostPersist
    public void onPostPersist(){
        TicketPrinted ticketPrinted = new TicketPrinted();
        BeanUtils.copyProperties(this, ticketPrinted);
        ticketPrinted.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        //theatermy.external.BookInfo bookInfo = new theatermy.external.BookInfo();//
        // mappings goes here
        //Application.applicationContext.getBean(theatermy.external.BookInfoService.class)
        //    .searchBook(bookInfo);


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getBookId() {
        return bookId;
    }

    public String getMovieId() {
      return movieId;
    }


    public void setMovieId(String movieId) {
      this.movieId = movieId;
    }


    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }
    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }




}
