package com.esentri.integration.messaging.application;


import com.arakelian.faker.model.Address;
import com.arakelian.faker.model.Person;
import com.arakelian.faker.service.RandomAddress;
import com.arakelian.faker.service.RandomPerson;
import com.esentri.integration.messaging.entity.Contact;
import lombok.extern.log4j.Log4j;
import org.apache.activemq.artemis.api.core.ICoreMessage;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.server.transformer.Transformer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public final class IdocToContactTransformer implements Transformer {

    @Override
    public void init(Map<String, String> properties) {
    }

    @Override
    public Message transform(Message message) {
        ICoreMessage coreMessage = message.toCore();
        SimpleString mySimpleString = coreMessage.getBodyBuffer().readNullableSimpleString();
             System.out.println(mySimpleString);

             try {
                 Contact aContact = buildContact();

                 // turn serializable object into byte array and write it to the message
                 ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                 ObjectOutputStream oos = new ObjectOutputStream(baos);
                 oos.writeObject(aContact);
                 oos.flush();
                 byte[] data = baos.toByteArray();
                 coreMessage.getBodyBuffer().clear();
                 coreMessage.getBodyBuffer().writeInt(data.length);
                 coreMessage.getBodyBuffer().writeBytes(data);
                 coreMessage.setType(Message.OBJECT_TYPE);

                 System.out.println("Transformed to contact " + aContact);
                 return coreMessage;
             }
             catch(Exception ex) {
                 ex.printStackTrace();
                 return message;
             }
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
