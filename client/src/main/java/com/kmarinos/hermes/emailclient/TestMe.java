package com.kmarinos.hermes.emailclient;

import com.kmarinos.hermes.domain.email.Body;
import java.util.Date;

public class TestMe {

  public static void main(String[] args){
    Email.compose().to("bot2@mail.marinos.com")
        .subject("test")
        .body(Body.compose("::someText::"))
        .param("someText",ctx->new Date().toString())

        .send();
  }
}
