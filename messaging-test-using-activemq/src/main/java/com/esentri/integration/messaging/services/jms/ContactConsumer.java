package com.esentri.integration.messaging.services.jms;

import com.esentri.integration.messaging.entity.Contact;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.jms.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Log
public final class ContactConsumer implements Runnable {

  @ConfigProperty(name = "message.contact.consumer1.queue.name")
  private String queueName;

  @ConfigProperty(name = "message.contact.consumer1.queue.enable", defaultValue = "true")
  private boolean enable;

  private ConnectionFactory connectionFactory;

  private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

  public ContactConsumer(ConnectionFactory connectionFactory) {
    if (connectionFactory == null) {
      throw new RuntimeException(
          "ContactConsumer can not be instantiated because connectionFactory to message broker is"
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
        JMSConsumer consumer = context.createConsumer(context.createQueue(queueName));
        while (true) {
          Message message = consumer.receive();
          if (message != null) {
            Contact received = message.getBody(Contact.class);
            log.info("Received contact: " + received + " from queue " + queueName);
          }
        }
      } catch (JMSException ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}
