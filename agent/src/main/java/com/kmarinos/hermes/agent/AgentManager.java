package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;
import com.kmarinos.hermes.serviceDto.AgentPUT;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import com.kmarinos.hermes.serviceDto.HeartbeatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgentManager {

  private final ServletWebServerApplicationContext webServerApplicationContext;
  private final EmailService emailService;

  private final RestTemplate restTemplate= new RestTemplate();
  private AgentGET activeAgent = AgentGET.builder().build();
  String serviceUrl = "http://localhost:8080/agent";

  @Scheduled(cron = "0/2 * * * * *")
  public void heartbeat(){
    log.info("Sending heartbeat");
    try{
      var heartbeat = sendHeartbeat();
      if(heartbeat == null || heartbeat.getStatus().equals(HeartbeatStatus.UNKNOWN_AGENT)){
        registerNewAgent();
      }
    }catch (Exception e){
     if (e instanceof ResourceAccessException){
        log.error(e.getMessage());
      }else{
       throw e;
     }
    }

  }

  public void registerNewAgent(){
    int port = webServerApplicationContext.getWebServer().getPort();
    var agentPost = AgentPOST.builder()
        .port(port)
        .os(System.getProperty("os.name"))
        .maxMemory(Runtime.getRuntime().maxMemory())
        .build();
    activeAgent =
        restTemplate.postForEntity(serviceUrl+"/register", agentPost, AgentGET.class).getBody();
    assert activeAgent != null;
    log.info("registered agent {} with id {}",activeAgent.getFriendlyName(),activeAgent.getId());
  }
  public Heartbeat sendHeartbeat(){
    var reportStatus = AgentPUT.builder()
        .id(activeAgent.getId())
        .canProcess(emailService.isCanProcess())
        .build();
    return restTemplate.postForEntity(serviceUrl+"/heartbeat", reportStatus, Heartbeat.class).getBody();
  }
}
