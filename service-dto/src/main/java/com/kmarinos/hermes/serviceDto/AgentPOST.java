package com.kmarinos.hermes.serviceDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentPOST {

  int port;
  String os;
  String friendlyName;
  long maxMemory;

}
