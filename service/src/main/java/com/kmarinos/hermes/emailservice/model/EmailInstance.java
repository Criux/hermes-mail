package com.kmarinos.hermes.emailservice.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailInstance extends BaseEntity{

  @ManyToOne
  EmailRequest emailRequest;
  @ManyToOne
  Recipient recipient;
  @Enumerated(EnumType.STRING)
  RecipientType recipientType;
  String body;
  String subject;
}
