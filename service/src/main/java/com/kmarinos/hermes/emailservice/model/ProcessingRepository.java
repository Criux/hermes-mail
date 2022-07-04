package com.kmarinos.hermes.emailservice.model;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingRepository extends JpaRepository<Processing,String> {

  List<Processing> findAllByEmailRequest(EmailRequest request);

}
