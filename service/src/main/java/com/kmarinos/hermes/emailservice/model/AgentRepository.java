package com.kmarinos.hermes.emailservice.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent,String> {

  List<Agent> findAllByCanProcessAndStatus(boolean canProcess,AgentStatus status);
}
