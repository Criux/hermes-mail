package com.kmarinos.hermes.emailservice.model;

import lombok.Builder;
import lombok.Data;

@Data
public class ProcessingMessage<T extends BaseEntity> {
  String message;
  String log;
  String forEntityId;
  String forEntityClass;

  @Builder
  public ProcessingMessage(T entity,String message,String log){
    this.forEntityId=entity.getId();
    this.forEntityClass=entity.getClass().getCanonicalName();
    this.message=message;
    this.log=log;

  }

}




