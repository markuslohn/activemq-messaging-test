package com.esentri.integration.messaging.services.jms;

import com.esentri.integration.messaging.entity.Idoc;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.jms.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A filter is applied when consuming messages. See below for more details.
 *
 * <p>Disadvantage is that the filtered messages stay in the queue.
 */
@ApplicationScoped
@Log
public final class ThirdIdocConsumer implements Runnable {

  private static final String SUPPORTED_IDOC_TYPE = "FUNC_LOC_CREATE02";

  @ConfigProperty(name = "message.idoc.consumer3.queue.name")
  private String queueName;

  @ConfigProperty(name = "message.idoc.consumer3.queue.enable", defaultValue = "true")
  private boolean enable;

  private ConnectionFactory connectionFactory;

  private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

  public ThirdIdocConsumer(ConnectionFactory connectionFactory) {
    if (connectionFactory == null) {
      throw new RuntimeException(
          "IdocConsumer can not be instantiated because connectionFactory to message broker is"
              + " null. Please check your configuration");
    }
    this.connectionFactory = connectionFactory;
  }

  void onStart(@Observes StartupEvent ev) {
    scheduler.submit(this);
  }

  void onStop(@Observes ShutdownEvent ev) {
    scheduler.shutdown();
  }

  @Override
  public void run() {
    if (enable) {
      log.info("Now starts consuming message from queue " + queueName);

      try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
        JMSConsumer consumer =
            context.createConsumer(
                context.createQueue(queueName), "JMSType = '" + SUPPORTED_IDOC_TYPE + "' ");
        while (true) {
          Message message = consumer.receive();
          if (message != null) {
            Idoc received = new Idoc(message.getBody(String.class));
            log.info(
                "Received Idoc: "
                    + received.getDocnum()
                    + " ("
                    + received.getIdocType()
                    + ") from queue "
                    + queueName);
          }
        }
      } catch (JMSException ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}
