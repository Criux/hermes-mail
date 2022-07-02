package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Slf4j
public class StartAgent{

  @Autowired
  private ServletWebServerApplicationContext webServerApplicationContext;

  private final RestTemplate restTemplate= new RestTemplate();

  public static void main(String[] args) {
    SpringApplication.run(StartAgent.class, args);
  }

  @Bean
  public CommandLineRunner initAgent(){
    return args -> {
      int port = webServerApplicationContext.getWebServer().getPort();
      var agentPost = AgentPOST.builder()
          .port(port)
          .os(System.getProperty("os.name"))
          .maxMemory(Runtime.getRuntime().maxMemory())
          .build();
      var agent =
          restTemplate.postForEntity("http://localhost:8080/agent/register", agentPost, AgentGET.class).getBody();
      assert agent != null;
      log.info("registered agent {} with id {}",agent.getFriendlyName(),agent.getId());
    };
  }
}