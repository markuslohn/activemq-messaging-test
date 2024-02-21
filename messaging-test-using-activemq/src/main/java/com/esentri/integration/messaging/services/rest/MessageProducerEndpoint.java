package com.esentri.integration.messaging.services.rest;

import com.arakelian.faker.model.Address;
import com.arakelian.faker.model.Person;
import com.arakelian.faker.service.RandomAddress;
import com.arakelian.faker.service.RandomPerson;
import com.esentri.integration.messaging.entity.Contact;
import com.esentri.integration.messaging.entity.Idoc;
import com.esentri.integration.messaging.entity.PropertyNameConstants;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Log
@Path("/message")
public final class MessageProducerEndpoint {

  private ConnectionFactory connectionFactory;

  @ConfigProperty(name = "message.idoc.producer.queue.name")
  private String idocQueueName;

  @ConfigProperty(name = "message.contact.producer.queue.name")
  private String contactQueueName;

  public MessageProducerEndpoint(ConnectionFactory connectionFactory) {
    if (connectionFactory == null) {
      throw new RuntimeException(
          "Endpoint can not be instantiated because connectionFactory to message broker is null."
              + " Please check your configuration");
    }
    this.connectionFactory = connectionFactory;
  }

  @POST
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/idoc/{destination}")
  public Response publishIdoc(
      @Context HttpHeaders headers,
      @NonNull String idocText,
      @PathParam("destination") String destination)
      throws JMSException {
    Idoc idoc = new Idoc(idocText);

    JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);

    Message msgToSend = context.createTextMessage(idoc.getIdocXml());
    msgToSend.setJMSType(idoc.getIdocType());
    msgToSend.setStringProperty(PropertyNameConstants.DOCNUM_PROPERTY_NAME, idoc.getDocnum());
    context.createProducer().send(context.createTopic(destination), msgToSend);

    log.info(
        "Published Idoc "
            + idoc.getDocnum()
            + " of type "
            + idoc.getIdocType()
            + " to "
            + destination);

    return Response.ok("DOCNUM=" + idoc.getDocnum()).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/idoc")
  public Response publishIdoc(@Context HttpHeaders headers, @NonNull String idocText)
      throws JMSException {
    return publishIdoc(headers, idocText, idocQueueName);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/contact")
  public Response publishContact(@Context HttpHeaders headers) throws JMSException {

    Contact aContact = buildContact();

    JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
    Message msgToSend = context.createObjectMessage(aContact);
    msgToSend.setJMSType("CONTACT");
    msgToSend.setStringProperty(
        PropertyNameConstants.CONTACTID_PROPERTY_NAME, aContact.getPerson().getId());
    context.createProducer().send(context.createTopic(contactQueueName), msgToSend);

    log.info("Published contact " + aContact + " to " + contactQueueName);

    return Response.ok(aContact).build();
  }

  private Contact buildContact() {
    Person person = RandomPerson.get().next();
    Address homeAddress = RandomAddress.get().next();
    Address workAddress = RandomAddress.get().next();

    return Contact.builder()
        .person(person)
        .homeAddress(homeAddress)
        .workAddress(workAddress)
        .build();
  }
}
