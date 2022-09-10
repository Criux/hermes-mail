package com.kmarinos.hermes.serviceDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailInstancePOST {

  String body;
  String subject;
  String recipientType;
  String emailRequestId;
  RecipientPOST recipientPOST;

}
