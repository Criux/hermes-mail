package com.kmarinos.hermes.serviceDto;

import lombok.Builder;
import lombok.Data;

@Data
public class ProgressReport<T> {
  String message;
  ProgressReportType type;
  String log;
  final T payload;
  final String payloadClass;

  @Builder
  public ProgressReport(T payload,String message,ProgressReportType type,String log){
    this.payload=payload;
    this.payloadClass=payload==null?"":payload.getClass().getCanonicalName();
    this.message=message;
    this.type=type;
    this.log=log;
  }

}
