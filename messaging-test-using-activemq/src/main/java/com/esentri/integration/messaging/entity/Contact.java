package com.esentri.integration.messaging.entity;

import com.arakelian.faker.model.Address;
import com.arakelian.faker.model.Person;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
public final class Contact implements Serializable {

  private Person person;
  @EqualsAndHashCode.Exclude private Address homeAddress;
  @EqualsAndHashCode.Exclude private Address workAddress;
}
