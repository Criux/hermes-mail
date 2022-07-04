package com.kmarinos.hermes.emailservice.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRequestRepository extends JpaRepository<EmailRequest,String> {

  List<EmailRequest> findAllByStatus(ProcessingStage status);
  List<EmailRequest> findAllByStatusNotIn(List<ProcessingStage> statuses);
}
