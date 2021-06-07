package theatermy;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookInfoController {

  @Autowired
  BookInfoRepository bookInfoRepository;

  @RequestMapping(value = "/bookInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
  public String bookInfos(HttpServletRequest request, HttpServletResponse response) throws Exception {

    String bookId = request.getParameter("bookId");
    Optional<BookInfo> bookInfo = bookInfoRepository.findByBookId(bookId);

    return bookInfo.isPresent()?bookInfo.get().getSeatId():"check your BookId";
  }
}
