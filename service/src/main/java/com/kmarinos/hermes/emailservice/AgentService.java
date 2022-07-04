package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.dto.EmailRequestDTO;
import com.kmarinos.hermes.emailservice.exceptionHandling.exceptions.EntityNotFoundException;
import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.emailservice.model.AgentRepository;
import com.kmarinos.hermes.emailservice.model.AgentStatus;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.serviceDto.EmailRequestGET;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import com.kmarinos.hermes.serviceDto.HeartbeatStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

  private final AgentRepository agentRepository;
  private final AgentRestClient agentRestClient;

  @Scheduled(cron = " */10 * * * * *")
  public void runAgentMaintenance() {
    log.info("Running maintenance...");
    for(Agent agent:agentRepository.findAll()){
      //agent has been registered but not yet reported a heartbeat
      if(agent.getHeartbeat()== null){
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
    };
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

  public Optional<Agent> sendWithNextAvailableAgent(EmailRequest emailRequest) {
    for (Agent agent : this.getAvailableAgents()) {
      var wasSent = agentRestClient.assignEmailToAgent(agent, emailRequest);
      agent.setCanProcess(false);
      if (!wasSent) {
        agent.setStatus(AgentStatus.NOT_RESPONDING);
      }
      agent = agentRepository.save(agent);
      return agent.getStatus().equals(AgentStatus.NOT_RESPONDING) ?
          Optional.empty()
          :Optional.of(agent);
    }
    return Optional.empty();
  }
}
