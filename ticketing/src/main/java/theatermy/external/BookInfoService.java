
package theatermy.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import theatermy.config.HttpConfiguration;

@FeignClient(name = "customercenter-api", url = "${feign.customercenter-api.url}",
configuration = HttpConfiguration.class,
fallbackFactory = BookInfoServiceFallbackFactory.class)
public interface BookInfoService {

  @GetMapping("/bookInfo")
  public String searchBook(@RequestParam("bookId") String bookId);

  @GetMapping("/isolation")
  String isolation();
}