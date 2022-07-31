package com.kmarinos.hermes.emailservice;

import com.github.javafaker.Faker;
import com.kmarinos.hermes.emailservice.exceptionHandling.exceptions.EntityNotFoundException;
import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.emailservice.model.AgentRepository;
import com.kmarinos.hermes.emailservice.model.AgentStatus;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import com.kmarinos.hermes.serviceDto.HeartbeatStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AgentService {

  private final AgentRepository agentRepository;
  private final AgentRestClient agentRestClient;
  private final Faker faker;

  public AgentService(AgentRepository agentRepository, AgentRestClient agentRestClient,
      Faker faker) {
    this.agentRepository = agentRepository;
    this.agentRestClient = agentRestClient;
    this.faker = faker;
  }

  @Scheduled(cron = " */10 * * * * *")
  public void runAgentMaintenance() {
    log.info("Running maintenance...");
    for (Agent agent : agentRepository.findAll()) {
      //agent has been registered but not yet reported a heartbeat
      if (agent.getHeartbeat() == null) {
        continue;
      }
      var now = LocalDateTime.now();
      //check if agent can be deleted
      if (!agent.getStatus().equals(AgentStatus.ACTIVE) && !agent.getStatus()
          .equals(AgentStatus.INACTIVE) && agent.getHeartbeat()
          .isBefore(now.minusSeconds(120))) {
        log.info("Deleting inactive agent");
        agent.setStatus(AgentStatus.INACTIVE);
        agent.setCanProcess(false);
        agentRepository.save(agent);
      }
      //check if agent has not sent a heartbeat
      else if (agent.getStatus().equals(AgentStatus.ACTIVE) && agent.getHeartbeat()
          .isBefore(now.minusSeconds(60))) {
        agent.setStatus(AgentStatus.NOT_RESPONDING);
        log.info("Setting agent to inactive");
        agentRepository.save(agent);
      }
    }
    ;
  }

  public String getRandomAgentName() {
    Supplier<String> supplier1 = () -> faker.food().vegetable().replace(" ", "");
    Supplier<String> supplier2 = () -> faker.animal().name().replace(" ", "");
    Supplier<String> supplier3 = () -> faker.color().name().replace(" ", "");
    Supplier<String> supplier4 = () -> faker.hacker().adjective().replace(" ", "");
    Supplier<String> supplier5 = () -> faker.name().firstName().replace(" ", "");
    var list = List.of(supplier1, supplier2, supplier3, supplier4, supplier5);
    var random = new Random();
    String name = "";
    var totalWords = random.nextInt(3) + 2;
    for (int i = 0; i < totalWords; i++) {
      var nextWord = list.get(random.nextInt(list.size())).get();
      name = name + nextWord.substring(0, 1).toUpperCase() + nextWord.substring(1).toLowerCase();
    }
    return name;
  }

  public Agent registerNewAgent(Agent agent) {
    return agentRepository.save(agent);
  }

  public List<Agent> getAvailableAgents() {
    return agentRepository.findAllByCanProcessAndStatus(true, AgentStatus.ACTIVE);
  }

  public Heartbeat registerHeartbeat(Agent agentReport) {
    var unknownAgent = Heartbeat.builder()
        .status(HeartbeatStatus.UNKNOWN_AGENT)
        .build();
    if (agentReport == null || agentReport.getId() == null) {
      return unknownAgent;
    }
    return agentRepository.findById(agentReport.getId()).map(agent -> {
      agent.setHeartbeat(LocalDateTime.now());
      agent.setCanProcess(agentReport.isCanProcess());
      agent.setStatus(AgentStatus.ACTIVE);
      agent = agentRepository.save(agent);
      return Heartbeat.builder()
          .status(HeartbeatStatus.ACCEPTED)
          .at(agent.getHeartbeat())
          .build();
    }).orElse(unknownAgent);
  }

  public List<Agent> getOldHeartbeatAgents(long gracePeriod) {
    var cutoff = LocalDateTime.now().minusSeconds(gracePeriod);
    return agentRepository.findAllByHeartbeatBefore(cutoff);
  }

  public List<Agent> getAvailableEmailAgents() {
    return this.getAvailableAgents();
  }

  public List<Agent> getAvailableAttachmentAgents() {
    return this.getAvailableAgents();
  }

  public BiFunction<Agent, EmailRequest, Boolean> assignEmail() {
    return agentRestClient::assignEmailToAgent;
  }

  public BiFunction<Agent, EmailRequest, Boolean> assignAttachment() {
    return agentRestClient::assignAttachmentToAgent;
  }

  public Optional<Agent> sendWithNextAvailableAgent(EmailRequest emailRequest,
      Supplier<List<Agent>> agentSupplier,
      BiFunction<Agent, EmailRequest, Boolean> callback) {
    var agents = agentSupplier.get();
    for (Agent agent : agents) {
//      var wasSent = agentRestClient.assignEmailToAgent(agent, emailRequest);
      var wasSent = callback.apply(agent, emailRequest);
      agent.setCanProcess(false);
      if (!wasSent) {
        agent.setStatus(AgentStatus.NOT_RESPONDING);
      }
      agent = agentRepository.save(agent);
      return agent.getStatus().equals(AgentStatus.NOT_RESPONDING) ?
          Optional.empty()
          : Optional.of(agent);
    }
    return Optional.empty();
  }

  public Agent getAgentFromToken(String agentToken) {
    return agentRepository.findById(agentToken).orElseThrow(()->new EntityNotFoundException(Agent.class,"id"));
  }
}
