package com.esentri.integration.messaging.entity;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Idoc {

  private String idocXml;
  private String idocType;
  private String docnum;
  private String readObjNr;
  private String category;

  public Idoc(@NonNull String idocXml) {
    idocType = findIdocType(idocXml);
    if (Objects.isNull(idocType)) {
      throw new IllegalArgumentException(
          "A valid Idoc as XML representation from SAP was expected.");
    }
    docnum = findDocnum(idocXml);
    readObjNr = findReadObjNr(idocXml);
    category = findCategory(idocXml);
    this.idocXml = idocXml;
  }

  private String findIdocType(String idoc) {
    return lookupValueFromStringByPattern("<IDOCTYP>(.+?)</IDOCTYP>", idoc);
  }

  private String findCategory(String idoc) {
    String category = lookupValueFromStringByPattern("<CATEGORY>(.+?)</CATEGORY>", idoc);
    if (Objects.isNull(category)) {
      category = lookupValueFromStringByPattern("<EQUICATGRY>(.+?)</EQUICATGRY>", idoc);
    }
    return category;
  }

  private String findDocnum(String idoc) {
    return lookupValueFromStringByPattern("<DOCNUM>(.+?)</DOCNUM>", idoc);
  }

  private String findReadObjNr(String idoc) {
    String objectId = lookupValueFromStringByPattern("<READ_OBJNR>(.+?)</READ_OBJNR>", idoc);
    if (!isEmpty(objectId)) {
      if (objectId.startsWith("IF?") || objectId.startsWith("IE")) {
        objectId = objectId.substring(2, objectId.length());
      }
    }
    return objectId;
  }

  private String lookupValueFromStringByPattern(String patternDefinition, String source) {
    final Pattern pattern = Pattern.compile(patternDefinition, Pattern.DOTALL);
    final Matcher matcher = pattern.matcher(source);
    String value = null;
    if (matcher.find()) {
      value = matcher.group(1);
    }
    return value;
  }

  private boolean isEmpty(String valueToCheck) {
    return valueToCheck == null || valueToCheck.isEmpty() || valueToCheck.isBlank();
  }
}
