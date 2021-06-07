package theatermy;

public class TicketPrinted extends AbstractEvent {

    private Long id;
    private String bookId;

    public TicketPrinted(){
        super();
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

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
