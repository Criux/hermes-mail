package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TablesAttachment extends EmailAttachment{
  private List<Table> sheets = new ArrayList<>();

  public static TablesAttachment compose(){
    var attachment = new TablesAttachment();
    attachment.setName("report.xlsx");
    return attachment;
  }
  public TablesAttachment addTable(Table table){
    sheets.add(table);
    return this;
  }
  @Override
  public EmailAttachment content(byte[] content){
    throw new RuntimeException("Setting the content directly for excel is not allowed. Try addTable()");
  }
}
