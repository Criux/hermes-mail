package com.kmarinos.hermes.emailclient;

import com.kmarinos.hermes.domain.IdentityProvider;
import com.kmarinos.hermes.domain.email.EmailRecipient;

public class StandardIdentityProvider implements IdentityProvider {

  @Override
  public EmailRecipient getRecipientInfo(String email) {
    return EmailRecipient.builder()
        .email(email)
        .firstname(email.split("@")[0])
        .build();
  }
}
