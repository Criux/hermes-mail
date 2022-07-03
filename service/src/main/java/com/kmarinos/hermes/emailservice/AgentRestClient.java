package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.dto.EmailRequestDTO;
import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AgentRestClient {

  private final RestTemplate restTemplate = new RestTemplate();

  public boolean assignEmailToAgent(Agent agent, EmailRequest emailRequest) {

    try{
      restTemplate.postForEntity(agent.getListeningOn()+"/assign", EmailRequestDTO.GET(emailRequest),Void.class);
    }catch(RestClientException e){
      log.error("Cannot connect to agent with id {}",agent.getId());
      return false;
    }
    return true;
  }
  public boolean pingAgent(Agent agent){
    return restTemplate.getForEntity(agent.getListeningOn()+"/ping",Void.class).getStatusCode().is2xxSuccessful();
  }
}
