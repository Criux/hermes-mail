package com.kmarinos.hermes.emailservice.model;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Immutable;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Immutable
@EntityListeners(ProcessingListener.class)
public class Processing {

  @Id
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
  String id;
  @ManyToOne(cascade = CascadeType.ALL)
  Agent agent;
  @ManyToOne
  EmailRequest emailRequest;
  @Enumerated(EnumType.STRING)
  ProcessingStage stage;
  String secondaryStage;
  String message;
  @CreatedDate
  @Column(nullable = false, updatable = false)
  @Builder.Default
  LocalDateTime createdAt = LocalDateTime.now();

}
