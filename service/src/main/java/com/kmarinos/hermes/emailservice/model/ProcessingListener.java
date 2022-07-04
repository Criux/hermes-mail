package com.kmarinos.hermes.emailservice.model;

import java.time.LocalDateTime;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessingListener {
  @Autowired
  public void init(EmailRequestRepository emailRequestRepository){
    ProcessingListener.requestRepository=emailRequestRepository;
  }
  private static EmailRequestRepository requestRepository;

  @PrePersist
  public void prePersist(Processing processing){
    var request= processing.getEmailRequest();
    request.setStatus(processing.getStage());
    requestRepository.saveAndFlush(request);
  }

//  @PostLoad
//  public void postLoad(Processing processing) {
//    var request = processing.getEmailRequest();
//    if(request!=null){
//      request.setStatus(processing.getStage());
//      processing.setEmailRequest(requestRepository.saveAndFlush(request));
//    }
//  }

}
