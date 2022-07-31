package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;
import com.kmarinos.hermes.serviceDto.AgentPUT;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import com.kmarinos.hermes.serviceDto.HeartbeatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgentManager {

  private final EmailService emailService;
  private final BackendClient backendClient;


  private final AgentGET activeAgent;
  @Value("${hermes.email.agent.friendlyName}")
  String friendlyName;


  @Scheduled(cron = "0/2 * * * * *")
  public void heartbeat() {
    log.info("Sending heartbeat");
    try {
      var heartbeat = sendHeartbeat();
      if (heartbeat == null || heartbeat.getStatus().equals(HeartbeatStatus.UNKNOWN_AGENT)) {
        registerNewAgent();
      }
    } catch (Exception e) {
      if (e instanceof ResourceAccessException) {
        log.error(e.getMessage());
      } else {
        throw e;
      }
    }

  }

  public void registerNewAgent() {
    var agentPost = AgentPOST.builder()
        .os(System.getProperty("os.name"))
        .friendlyName(friendlyName)
        .maxMemory(Runtime.getRuntime().maxMemory())
        .build();
    assert backendClient.registerAgent(agentPost) != null;
    activeAgent.setId(backendClient.registerAgent(agentPost).getId());
    log.info("registered agent {} with id {}", activeAgent.getFriendlyName(), activeAgent.getId());
  }

  public Heartbeat sendHeartbeat() {
    var reportStatus = AgentPUT.builder()
        .id(activeAgent.getId())
        .canProcess(emailService.isCanProcess())
        .build();
    return backendClient.sendHeartbeat(reportStatus);
  }
}
