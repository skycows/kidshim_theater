package theatermy;

public class Reserved extends AbstractEvent {

    private Long id;
    private String bookId;
    private String movieId;
    private String customerId;
    private String seatId;
    
    public Long getId() {
      return id;
    }
    public void setId(Long id) {
      this.id = id;
    }
    public String getBookId() {
      return bookId;
    }
    public void setBookId(String bookId) {
      this.bookId = bookId;
    }
    public String getMovieId() {
      return movieId;
    }
    public void setMovieId(String movieId) {
      this.movieId = movieId;
    }
    public String getCustomerId() {
      return customerId;
    }
    public void setCustomerId(String customerId) {
      this.customerId = customerId;
    }
    public String getSeatId() {
      return seatId;
    }
    public void setSeatId(String seatId) {
      this.seatId = seatId;
    }
 
    
}
