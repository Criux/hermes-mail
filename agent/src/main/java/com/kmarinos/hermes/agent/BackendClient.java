package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;
import com.kmarinos.hermes.serviceDto.AgentPUT;
import com.kmarinos.hermes.serviceDto.AttachedFilePOST;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BackendClient {

  private final RestTemplate restTemplate = new RestTemplate();
  private final ServletWebServerApplicationContext webServerApplicationContext;
  private final AgentGET agent;
  @Value("${hermes.email.serviceUrl}")
  String serviceUrl;



  public BackendClient(ServletWebServerApplicationContext webServerApplicationContext,
      AgentGET agent) {
    this.webServerApplicationContext = webServerApplicationContext;
    this.agent = agent;
  }

  public AgentGET registerAgent(AgentPOST agentPost) {
    int port = webServerApplicationContext.getWebServer().getPort();
    agentPost.setPort(port);

    return post("/agent/register", agentPost, AgentGET.class);

  }

  public Heartbeat sendHeartbeat(AgentPUT reportStatus) {
//    return restTemplate.postForEntity(serviceUrl + "/agent/heartbeat", reportStatus, Heartbeat.class)
//        .getBody();
    return post("/agent/heartbeat",reportStatus, Heartbeat.class);
  }
  public void registerAttachment(AttachedFilePOST attachmentPost){
    post("/email/attach/"+attachmentPost.getEmailRequestId(),attachmentPost,Void.class);

  }
  private <T> T post(String endpoint,Object body,Class<T> returnClass){
    var headers = new HttpHeaders();
    headers.set("X-Agent-Token",agent.getId());
    var httpEntity = new HttpEntity<>(body,headers);

    var response = restTemplate.exchange(serviceUrl+endpoint, HttpMethod.POST,httpEntity,returnClass);

    return response.getBody();
  }

  public void completeAttachments(String requestId) {
    post("/email/complete-attachments/"+requestId,Void.class, Void.class);
  }
}
