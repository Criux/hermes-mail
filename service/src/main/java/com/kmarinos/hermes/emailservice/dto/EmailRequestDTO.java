package com.kmarinos.hermes.emailservice.dto;

import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.serviceDto.EmailRequestGET;
import java.io.IOException;
import java.sql.SQLException;

public class EmailRequestDTO {

  public static EmailRequestGET GET(EmailRequest emailRequest){
    try {
      return EmailRequestGET.builder()
          .id(emailRequest.getId())
          .e(emailRequest.getE().getBinaryStream().readAllBytes())
          .l(emailRequest.getL().getBinaryStream().readAllBytes())
          .build();
    } catch (IOException | SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
