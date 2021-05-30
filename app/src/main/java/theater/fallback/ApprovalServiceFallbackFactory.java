package theater.fallback;

import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import theater.external.Approval;
import theater.external.ApprovalService;

@Slf4j
@Component
public class ApprovalServiceFallbackFactory implements FallbackFactory<ApprovalService> {

    @Override
    public ApprovalService create(Throwable cause) {
        log.info("========= FallbackFactory called: Confirm Pay Service =========");
//        log.info("Error Message: "+cause.getMessage());
        return new ApprovalService() {
            @Override
            public Approval paymentRequest(String bookId, String movieId, String seatId) {
                return null;
            }

            @Override
            public String isolation() {
                return "fallback";
            }
        };
    }
}
