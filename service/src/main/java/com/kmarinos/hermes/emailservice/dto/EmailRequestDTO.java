package com.kmarinos.hermes.emailservice.dto;

import com.kmarinos.hermes.emailservice.model.AttachedFile;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.serviceDto.AttachedFilePOST;
import com.kmarinos.hermes.serviceDto.AttachmentRequestGET;
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
  public static AttachmentRequestGET AttachmentGET(EmailRequest emailRequest){
    try{
      return AttachmentRequestGET.builder()
          .emailRequestId(emailRequest.getId())
          .a(emailRequest.getA().getBinaryStream().readAllBytes())
          .build();
    }catch (IOException | SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
