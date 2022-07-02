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

  public Agent registerNewAgent(Agent agent) {
    return agentRepository.save(agent);
  }
  public Agent getNextAvailable(){
    return agentRepository.findAllByCanProcessAndStatus(true,AgentStatus.NOT_RESPONDING).get(0);
  }
}
