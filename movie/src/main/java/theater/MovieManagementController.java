package theater;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class MovieManagementController {
  
  @GetMapping("/hpa")
  public String getHPA() throws NoSuchAlgorithmException {
    Random rand = SecureRandom.getInstanceStrong();
    long loopCnt = 0;

    while (loopCnt < 100) {
      double r = rand.nextFloat();
      double v = Math.sin(Math.cos(Math.sin(Math.cos(r))));
      System.out.println(String.format("r: %f, v %f", r, v));
      loopCnt++;
    }

    return "hpa";
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
