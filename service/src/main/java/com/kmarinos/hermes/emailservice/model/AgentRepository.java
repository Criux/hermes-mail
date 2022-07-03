package com.kmarinos.hermes.emailservice.model;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent,String> {

  List<Agent> findAllByCanProcessAndStatus(boolean canProcess,AgentStatus status);
  List<Agent> findAllByHeartbeatBefore(LocalDateTime time);
  List<Agent> findAllByStatus(AgentStatus status);
}
