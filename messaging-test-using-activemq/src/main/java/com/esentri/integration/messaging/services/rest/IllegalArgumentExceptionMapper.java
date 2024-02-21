package com.esentri.integration.messaging.services.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public final class IllegalArgumentExceptionMapper
    implements ExceptionMapper<IllegalArgumentException> {
  @Override
  public Response toResponse(IllegalArgumentException ex) {
    return Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .entity(ex.getMessage())
        .build();
  }
}
