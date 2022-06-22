package com.kmarinos.hermes.emailservice.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Data
public class EmailInstance {

  @Id
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
  String id;

  @ManyToOne
  EmailRequest emailRequest;
  @ManyToOne
  Recipient recipient;
  @Enumerated(EnumType.STRING)
  RecipientType recipientType;
  String body;
  String subject;
}
