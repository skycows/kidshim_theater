package theatermy;

import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookInfoController {

  @Autowired
  BookInfoRepository bookInfoRepository;

  @Value("${call.number}")
  String callNumber;
  
  /**
   * Feign용
   * 
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
  @RequestMapping(value = "/bookInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
  public String bookInfos(HttpServletRequest request, HttpServletResponse response) {

    String bookId = request.getParameter("bookId");
    Optional<BookInfo> bookInfo = bookInfoRepository.findByBookId(bookId);

    return bookInfo.isPresent() ? bookInfo.get().getSeatId() : "check your BookId";
  }

  /**
   * Command용
   * 
   * @param request
   * @param response
   * @return
   * @throws Exception
   */
  @RequestMapping(value = "/searchBook", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
  public BookInfo searchBook(HttpServletRequest request, HttpServletResponse response) {
    String bookId = request.getParameter("bookId");
    Optional<BookInfo> bookInfo = bookInfoRepository.findByBookId(bookId);

    return bookInfo.orElse(null);
  }

  /**
   * 부하테스트용
   * 
   * @param request
   * @param response
   * @return
   * @throws Exception
   */

  @RequestMapping(value = "/isolation", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
  public String isolation(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Random rng = new Random();
    long loopCnt = 0;

    while (loopCnt < 100) {
      double r = rng.nextFloat();
      double v = Math.sin(Math.cos(Math.sin(Math.cos(r))));
      System.out.println(String.format("r: %f, v %f", r, v));
      loopCnt++;
    }

    return "real";
  }

  @GetMapping("/call-number")
  public String getCallNumber() {
    return callNumber;
  }
}
