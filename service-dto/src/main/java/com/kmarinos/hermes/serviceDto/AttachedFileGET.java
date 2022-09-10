package com.kmarinos.hermes.serviceDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachedFileGET {
  String id;
  String filename;
  String filetype;
  long size;
  byte[] content;
  String emailRequestId;
}
