package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;
import com.kmarinos.hermes.serviceDto.AgentPUT;
import com.kmarinos.hermes.serviceDto.AttachedFilePOST;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BackendClient {

  private final RestTemplate restTemplate = new RestTemplate();
  private final ServletWebServerApplicationContext webServerApplicationContext;
  @Value("${hermes.serviceUrl}")
  String serviceUrl;


  public BackendClient(ServletWebServerApplicationContext webServerApplicationContext) {
    this.webServerApplicationContext = webServerApplicationContext;
  }

  public AgentGET registerAgent(AgentPOST agentPost) {
    int port = webServerApplicationContext.getWebServer().getPort();
    agentPost.setPort(port);

    return restTemplate.postForEntity(serviceUrl + "/agent/register", agentPost, AgentGET.class)
        .getBody();
  }

  public Heartbeat sendHeartbeat(AgentPUT reportStatus) {
    return restTemplate.postForEntity(serviceUrl + "/agent/heartbeat", reportStatus, Heartbeat.class)
        .getBody();
  }
  public void registerAttachment(AttachedFilePOST attachmentPost){
    restTemplate.postForLocation(serviceUrl+"/email/attach/"+attachmentPost.getEmailRequestId(),attachmentPost);
  }
}
