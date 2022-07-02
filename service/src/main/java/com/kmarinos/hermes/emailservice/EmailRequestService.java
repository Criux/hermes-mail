package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.model.Client;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.emailservice.model.EmailRequestRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailRequestService {

  private final EmailRequestRepository emailRequestRepository;
  public EmailRequest registerEmailRequest(Client client,byte[] lBytes,byte[]eBytes){
    return emailRequestRepository.save(EmailRequest.builder()
            .client(client)
        .l(BlobProxy.generateProxy(lBytes))
        .e(BlobProxy.generateProxy(eBytes))
        .build());
  }
  public EmailRequest getEmailRequestById(String id){
    return emailRequestRepository.findById(id).orElseThrow(()->new RuntimeException("Cant find email request with id "+id));
  }
}
