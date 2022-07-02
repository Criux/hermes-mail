package com.kmarinos.hermes.emailservice.dto;

import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.emailservice.model.AgentStatus;
import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;

public class AgentDTO {
  public static AgentGET GET(Agent agent){
    return AgentGET.builder()
        .id(agent.getId())
        .friendlyName(agent.getFriendlyName())
        .build();
  }
  public static Agent POST(AgentPOST agentPOST,String fromUrl){
    return Agent.builder()
        .status(AgentStatus.NOT_RESPONDING)
        .canProcess(true)
        .friendlyName("ImpressiveCantaloupe")
        .os(agentPOST.getOs())
        .listeningOn(fromUrl+":"+agentPOST.getPort())
        .maxMemory(agentPOST.getMaxMemory())
        .build();
  }

}
