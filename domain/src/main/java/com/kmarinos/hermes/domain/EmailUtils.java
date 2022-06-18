package com.kmarinos.hermes.domain;

import java.util.regex.Pattern;

public class EmailUtils {
  private static final String REGEX_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
      + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

  public static boolean isValid(String email){
    return Pattern.compile(REGEX_PATTERN)
        .matcher(email)
        .matches();
  }
}
