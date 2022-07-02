package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.emailservice.model.AgentRepository;
import com.kmarinos.hermes.emailservice.model.AgentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentService {

  private final AgentRepository agentRepository;

  public Agent registerNewAgent() {
    var agent = Agent.builder()
        .status(AgentStatus.NOT_RESPONDING)
        .canProcess(true)
        .friendlyName("ImpressiveCantaloupe")
        .build();
    return agentRepository.save(agent);
  }
}
