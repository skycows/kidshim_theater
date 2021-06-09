package theatermy;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

  @GetMapping("/isolations")
  public String isolation() {
    return bookInfoService.isolation();
  }

  @GetMapping("/serviceAddress")
  public String getServiceAddress() {
    return findMyHostname() + "/" + findMyIpAddress();
  }

  private String findMyHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown host name";
    }
  }

  private String findMyIpAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return "unknown IP address";
    }
  }
}
