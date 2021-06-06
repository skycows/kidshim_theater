package theater;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MimeTypeUtils;

import theater.config.kafka.KafkaProcessor;

public class AbstractEvent {

  String eventType;
  String timestamp;

  /**
   * Doubles the value. The long and detailed explanation what the method does.
   *
   */
  public AbstractEvent() {
    this.setEventType(this.getClass().getSimpleName());
    SimpleDateFormat defaultSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    this.timestamp = defaultSimpleDateFormat.format(new Date());
  }

  /**
   * Doubles the value. The long and detailed explanation what the method does.
   *
   */
  public final String toJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = null;

    try {
      json = objectMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON format exception", e);
    }

    return json;
  }

  public final void publish(final String json) {
    if (json != null) {

      /**
       * spring streams 방식
       */
      KafkaProcessor processor = MovieApplication.applicationContext.getBean(KafkaProcessor.class);
      MessageChannel outputChannel = processor.outboundTopic();

      outputChannel.send(MessageBuilder.withPayload(json)
          .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON).build());

    }
  }

  public final void publish() {
    this.publish(this.toJson());
  }

  public final void publishAfterCommit() {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

      @Override
      public void afterCompletion(final int status) {
        AbstractEvent.this.publish();
      }
    });
  }

  public final String getEventType() {
    return eventType;
  }

  public final void setEventType(final String eventType) {
    this.eventType = eventType;
  }

  public final String getTimestamp() {
    return timestamp;
  }

  public final void setTimestamp(final String timestamp) {
    this.timestamp = timestamp;
  }

  public final boolean validate() {
    return getEventType().equals(getClass().getSimpleName());
  }
}
