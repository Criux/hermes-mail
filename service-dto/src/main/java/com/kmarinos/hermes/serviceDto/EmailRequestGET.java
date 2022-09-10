package com.kmarinos.hermes.serviceDto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequestGET {

  String id;
  byte[] l;
  byte[] e;
}
