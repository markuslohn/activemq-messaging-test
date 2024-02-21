package com.esentri.integration.examples.activemq;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.core.server.transformer.Transformer;

import java.util.Map;

@Log4j
public final class IdocMessageTransformer implements Transformer {

    @Override
    public void init(Map<String, String> properties) {
        Transformer.super.init(properties);

        log.info("IdocMessageTransformer init() called.");
       properties.entrySet().forEach(property -> {log.info(property.getKey() + " = " + property.getValue());});
    }

    @Override
    public Message transform(Message message) {
        log.info("====================================");
        log.info("IdocMessageTransformer transform called for message id = " + message.getMessageID() + "/" + message.getAddress());
        log.info("Payload: ");
        log.info(message.getStringBody());
        log.info("====================================");

        return message;
    }
}
