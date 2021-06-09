package theatermy.external;

import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;

@Component
public class BookInfoServiceFallbackFactory implements FallbackFactory<BookInfoService> {

  @Override
  public BookInfoService create(Throwable cause) {
    System.out.println("========= FallbackFactory called: Confirm ticketing Service =========");
    return new BookInfoService() {
      @Override
      public String searchBook(String bookId) {
        return "Fallback - ticketing";
      }

      @Override
      public String isolation() {
          return "fallback - isolation";
      }
    };
  }
}
