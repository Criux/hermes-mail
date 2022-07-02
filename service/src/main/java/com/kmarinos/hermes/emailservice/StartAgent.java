package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.model.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartAgent {
  private final RestTemplate restTemplate= new RestTemplate();


  @Bean
  public CommandLineRunner initAgent(){
    return args -> {
      var agent =
          restTemplate.postForEntity("http://localhost:8080/agent/register", null, Agent.class).getBody();
      log.info("registered agent {} with id {}",agent.getFriendlyName(),agent.getId());
    };
  }
}
