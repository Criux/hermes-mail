package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.domain.email.EmailAttachment;
import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;
import com.kmarinos.hermes.serviceDto.AgentPUT;
import com.kmarinos.hermes.serviceDto.AttachedFileGET;
import com.kmarinos.hermes.serviceDto.AttachedFilePOST;
import com.kmarinos.hermes.serviceDto.EmailInstancePOST;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import com.kmarinos.hermes.serviceDto.ProgressReport;
import com.kmarinos.hermes.serviceDto.ProgressReportType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    return post("/agent/heartbeat", reportStatus, Heartbeat.class);
  }

  public void reportAttachmentProgress(AttachedFilePOST attachmentPost) {
    ProgressReport<AttachedFilePOST> pr = ProgressReport.<AttachedFilePOST>builder()
        .payload(attachmentPost)
        .message("Processed file successfully")
        .type(ProgressReportType.SUCCESS)
        .log("")
        .build();
    post("/email/attach/" + attachmentPost.getEmailRequestId(), pr, Void.class);

  }

  private <T> T post(String endpoint, Object body, Class<T> returnClass) {
    var headers = new HttpHeaders();
    headers.set("X-Agent-Token", agent.getId());
    var httpEntity = new HttpEntity<>(body, headers);

    var response = restTemplate.exchange(serviceUrl + endpoint, HttpMethod.POST, httpEntity,
        returnClass);

    return response.getBody();
  }

  private <T> T get(String endpoint, Class<T> returnClass) {
    var headers = new HttpHeaders();
    headers.set("X-Agent-Token", agent.getId());
    var httpEntity = new HttpEntity<>(headers);

    var response = restTemplate.exchange(serviceUrl + endpoint, HttpMethod.GET, httpEntity,
        returnClass);

    return response.getBody();
  }

  public void completeAttachments(String requestId) {
    post("/email/complete-attachments/" + requestId, Void.class, Void.class);
  }
  public void completeEmailInstances(String requestId){
    post("/email/complete-email-instances/" + requestId, Void.class, Void.class);
  }

  public List<AttachedFileGET> fetchAttachment(String emailRequestId) {
    return Arrays.asList(
        get("/email/%s/attachments".formatted(emailRequestId), AttachedFileGET[].class));
  }

  public void reportEmailInstanceProgress(EmailInstancePOST instancePOST) {
    ProgressReport<EmailInstancePOST> pr = ProgressReport.<EmailInstancePOST>builder()
        .payload(instancePOST)
        .message("Email sent successfully")
        .type(ProgressReportType.SUCCESS)
        .log("")
        .build();
    post("/email/sent/" + instancePOST.getEmailRequestId(), pr, Void.class);
  }
}
