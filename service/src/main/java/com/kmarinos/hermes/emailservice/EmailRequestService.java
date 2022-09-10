package com.kmarinos.hermes.emailservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.emailservice.model.AttachedFile;
import com.kmarinos.hermes.emailservice.model.Client;
import com.kmarinos.hermes.emailservice.model.EmailInstance;
import com.kmarinos.hermes.emailservice.model.EmailInstanceRepository;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.emailservice.model.EmailRequestRepository;
import com.kmarinos.hermes.emailservice.model.Processing;
import com.kmarinos.hermes.emailservice.model.ProcessingCompletionType;
import com.kmarinos.hermes.emailservice.model.ProcessingMessage;
import com.kmarinos.hermes.emailservice.model.ProcessingRepository;
import com.kmarinos.hermes.emailservice.model.ProcessingStage;
import com.kmarinos.hermes.emailservice.model.Recipient;
import com.kmarinos.hermes.emailservice.model.RecipientType;
import com.kmarinos.hermes.serviceDto.EmailInstancePOST;
import com.kmarinos.hermes.serviceDto.ProgressReport;
import com.kmarinos.hermes.serviceDto.ProgressReportType;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailRequestService {

  private final EmailRequestRepository emailRequestRepository;
  private final EmailInstanceRepository emailInstanceRepository;
  private final ProcessingRepository processingRepository;

  private final AgentService agentService;
  ObjectMapper objectMapper = new ObjectMapper();

  @Scheduled(cron = "*/10 * * * * *")
  @Transactional
  public void processLoop() {
    log.info("Checking attachments queue");
    checkIfAttachmentsNeedToBeAssigned();
    log.info("Check email queue...");
    checkIfEmailNeedsToBeAssigned();
    log.info("Check completed emails...");
    checkIfEmailNeedsToBeCompleted();
  }

  public void registerAttachmentProcessed(AttachedFile attachedFile, ProgressReport<?> pr,
      Agent agent) {
    var pm = ProcessingMessage.builder()
        .entity(attachedFile)
        .log(pr.getLog())
        .message(pr.getMessage())
        .build();
    var processing = Processing.builder()
        .emailRequest(attachedFile.getEmailRequest())
        .stage(ProcessingStage.PROCESSING_ATTACHMENTS)
        .agent(agent)
        .secondaryStage("ATTACHMENT_PROCESSED")
        .build();
    processing.setType(
        pr.getType().equals(ProgressReportType.SUCCESS) ? ProcessingCompletionType.SUCCESS
            : ProcessingCompletionType.ERROR);
    try {
      processing.setMessage(objectMapper.writeValueAsString(pm));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    processingRepository.saveAndFlush(processing);
    log.info("Processed attachment {} in {}", attachedFile.getFilename(), attachedFile.getPath());
  }

  public void registerEmailInstanceProcessed(EmailInstance emailInstance,
      ProgressReport<EmailInstancePOST> pr, Agent agent) {
    var pm = ProcessingMessage.builder()
        .entity(emailInstance)
        .log(pr.getLog())
        .message(pr.getMessage())
        .build();
    var processing = Processing.builder()
        .emailRequest(emailInstance.getEmailRequest())
        .stage(ProcessingStage.PROCESSING_EMAIL_INSTANCES)
        .agent(agent)
        .secondaryStage("EMAIL_INSTANCE_SENT")
        .build();
    processing.setType(
        pr.getType().equals(ProgressReportType.SUCCESS) ? ProcessingCompletionType.SUCCESS
            : ProcessingCompletionType.ERROR);
    try {
      processing.setMessage(objectMapper.writeValueAsString(pm));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    processingRepository.saveAndFlush(processing);
    log.info("Email instance sent {} to {}", emailInstance.getSubject(),
        emailInstance.getRecipient().getEmail());
  }

  public void completeAttachmentProcessing(EmailRequest emailRequest, Agent agent) {
    var processing = Processing.builder()
        .emailRequest(emailRequest)
        .stage(ProcessingStage.COMPLETED_ATTACHMENTS)
        .agent(agent)
        .build();
    processingRepository.saveAndFlush(processing);
    log.info("Closing processing of attachments");
  }

  public void completeEmailInstanceProcessing(EmailRequest emailRequest, Agent agent) {
    var processing = Processing.builder()
        .emailRequest(emailRequest)
        .stage(ProcessingStage.COMPLETED_EMAIL_INSTANCES)
        .agent(agent)
        .build();
    processingRepository.saveAndFlush(processing);
    log.info("Closing processing of email instances");
  }

  public void checkIfEmailNeedsToBeAssigned() {
    this.advanceRequest(ProcessingStage.COMPLETED_ATTACHMENTS, ProcessingStage.ACCEPTED_EMAIL,
        agentService::getAvailableEmailAgents, agentService.assignEmail());
  }

  public void checkIfEmailNeedsToBeCompleted() {
    this.advanceRequest(ProcessingStage.COMPLETED_EMAIL_INSTANCES, ProcessingStage.COMPLETED);
  }

  public void checkIfAttachmentsNeedToBeAssigned() {
    this.advanceRequest(ProcessingStage.REQUESTED, ProcessingStage.ACCEPTED_ATTACHMENTS,
        agentService::getAvailableAttachmentAgents, agentService.assignAttachment());
  }

  private void advanceRequest(ProcessingStage start, ProcessingStage end) {
    this.advanceRequest(start,end,null,null);
  }

  private void advanceRequest(ProcessingStage start, ProcessingStage end,
      Supplier<List<Agent>> availableAgents,
      BiFunction<Agent, EmailRequest, Boolean> callback) {
    var requests = this.emailRequestRepository.findAllByStatus(start);
    log.info("Total requests to process " + requests.size());
    for (EmailRequest emailRequest : requests) {
      //make sure that no agent has accepted it first
      var shouldAssign = processingRepository.findAllByEmailRequest(emailRequest).stream()
          .anyMatch(processing -> {
            return processing.getStage().equals(start);
          });
      if (shouldAssign) {
        if (availableAgents == null && callback == null) {
          processingRepository.save(Processing.builder()
              .stage(end)
              .emailRequest(emailRequest)
              .build());
        } else {
          var agentOptional = agentService.sendWithNextAvailableAgent(emailRequest,
              availableAgents, callback);
          if (agentOptional.isPresent()) {
            var agent = agentOptional.get();
            log.info("Assigned request to " + agent.getId());
            var accepted = Processing.builder()
                .stage(end)
                .agent(agent)
                .emailRequest(emailRequest)
                .build();
            processingRepository.save(accepted);
          } else {
            break;
          }
        }

      }
    }
  }

  public EmailRequest registerEmailRequest(Client client, byte[] lBytes, byte[] eBytes) {
    return this.registerEmailRequest(client, lBytes, eBytes, null);
  }

  public EmailRequest registerEmailRequest(Client client, byte[] lBytes, byte[] eBytes,
      byte[] aBytes) {

    var request = EmailRequest.builder()
        .client(client)
        .l(BlobProxy.generateProxy(lBytes))
        .e(BlobProxy.generateProxy(eBytes))
        .build();
    if (aBytes != null) {
      request.setA(BlobProxy.generateProxy(aBytes));
    }
    var created = Processing.builder()
        .emailRequest(request)
        .stage(ProcessingStage.REQUESTED)
        .build();

    return processingRepository.save(created).getEmailRequest();
  }

  public EmailRequest getEmailRequestById(String id) {
    return emailRequestRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Cant find email request with id " + id));
  }


  public EmailInstance createEmailInstance(EmailInstancePOST payload,
      Supplier<Recipient> recipientSupplier) {
    return emailInstanceRepository.save(EmailInstance.builder()
        .body(payload.getBody())
        .subject(payload.getSubject())
        .recipientType(RecipientType.valueOf(payload.getRecipientType()))
        .emailRequest(this.getEmailRequestById(payload.getEmailRequestId()))
        .recipient(recipientSupplier.get())
        .build());
  }


}

