package com.kmarinos.hermes.emailservice.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRequestRepository extends JpaRepository<EmailRequest,String> {

}
