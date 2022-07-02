package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.model.Agent;
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
  public ResponseEntity<Agent> registerAgent(){
    return ResponseEntity.ok().body(agentService.registerNewAgent());
  }
}
