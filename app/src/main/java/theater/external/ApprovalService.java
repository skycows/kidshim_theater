
package theater.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import theater.config.HttpConfiguration;
import theater.fallback.ApprovalServiceFallbackFactory;

@FeignClient(name = "pay-api", url = "${feign.pay-api.url}",
        configuration = HttpConfiguration.class,
        fallbackFactory = ApprovalServiceFallbackFactory.class)
public interface ApprovalService {

    @GetMapping("/approved")
    Approval paymentRequest(@RequestParam("bookId") String bookId
            , @RequestParam("movieId") String movieId
            , @RequestParam("seatId") String seatId
    );

    @GetMapping("/isolation")
    String isolation();
}
