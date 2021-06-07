package theatermy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import theatermy.external.BookInfoService;

@RestController
public class TicketController {
  private BookInfoService bookInfoService;

  public TicketController(BookInfoService bookInfoService) {
    this.bookInfoService = bookInfoService;
  }

  @GetMapping("/print/{bookId}")
  public String print(@PathVariable String bookId) {
    return bookInfoService.searchBook(bookId);
  }
}
