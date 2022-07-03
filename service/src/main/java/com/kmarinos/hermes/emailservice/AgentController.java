package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.dto.AgentDTO;
import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.serviceDto.AgentGET;
import com.kmarinos.hermes.serviceDto.AgentPOST;
import com.kmarinos.hermes.serviceDto.AgentPUT;
import com.kmarinos.hermes.serviceDto.Heartbeat;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("agent")
@RequiredArgsConstructor
public class AgentController {
  private final AgentService agentService;

  @PostMapping
  @RequestMapping("register")
  public ResponseEntity<AgentGET> registerAgent(@RequestBody AgentPOST agentPOST,
      HttpServletRequest request){
    String fromUrl = "http://"+request.getRemoteAddr();
    return ResponseEntity.ok().body(AgentDTO.GET(agentService.registerNewAgent(AgentDTO.POST(agentPOST,fromUrl))));
  }
  @PostMapping
  @RequestMapping("heartbeat")
  public ResponseEntity<Heartbeat> registerHeartbeat(@RequestBody AgentPUT agentPUT){
    return ResponseEntity.ok(agentService.registerHeartbeat(AgentDTO.PUT(agentPUT)));
  }
}
