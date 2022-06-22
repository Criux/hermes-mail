package com.kmarinos.hermes.emailservice.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Data
public class AttachedFile {

  @Id
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
  String id;
  String filename;
  String filetype;
  long size;
  String path;
  @CreatedDate
  LocalDateTime createdAt;
  @ManyToOne
  EmailRequest emailRequest;
}
