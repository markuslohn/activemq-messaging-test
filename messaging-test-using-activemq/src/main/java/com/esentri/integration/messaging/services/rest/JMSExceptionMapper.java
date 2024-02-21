package com.esentri.integration.messaging.services.rest;

import jakarta.jms.JMSException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public final class JMSExceptionMapper implements ExceptionMapper<JMSException> {

  @Override
  public Response toResponse(JMSException ex) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(ex.getMessage())
        .build();
  }
}
