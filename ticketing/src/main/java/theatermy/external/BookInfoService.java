
package theatermy.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="customercenter", url="http://customercenter:8080")
public interface BookInfoService {

    @RequestMapping(method= RequestMethod.GET, path="/bookInfos")
    public void searchBook(@RequestBody BookInfo bookInfo);

}