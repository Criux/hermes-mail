package com.kmarinos.hermes.emailservice.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachedFileRepository extends JpaRepository<AttachedFile,String> {

  List<AttachedFile> findAttachedFileByEmailRequest(EmailRequest emailRequest);

}
