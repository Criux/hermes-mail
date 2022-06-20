package com.kmarinos.hermes.domain;

import com.kmarinos.hermes.domain.email.EmailRecipient;

public interface IdentityProvider {

  public EmailRecipient getRecipientInfo(String email);
}
