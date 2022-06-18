package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailAttachment implements Serializable {
  private static final String REPORT_DEFAULT_NAME = "report";

  String name;
  AttachmentFileType type = AttachmentFileType.UNKNOWN;
  byte[] content;
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
  private boolean appendDateTime = false;

  public EmailAttachment name(String name){
    this.setName(name);
    recalculateName();
    return this;
  }
  public EmailAttachment type(String type){
    if(type!=null){
      this.setType(AttachmentFileType.valueOf(type.toUpperCase()));
    }
    recalculateName();
    return this;
  }
  public EmailAttachment content(byte[] content){
    this.setContent(content);
    return this;
  }
  public EmailAttachment appendDateTime(){
    this.setAppendDateTime(true);
    recalculateName();
    return this;
  }
  public String getName(){
    return recalculateName();
  }
  private String recalculateName(){
    String adjustedName=this.getName();
    //set default file name if it is not already set
    if(adjustedName == null || adjustedName.trim().isEmpty()){
      adjustedName = EmailAttachment.REPORT_DEFAULT_NAME;
    }
    //add file extension depending on attachment type if it is not yet set
    if(adjustedName.indexOf('.')==-1){
      adjustedName = adjustedName+this.getCorrectFileExtension();
    }
    //append date time to file name if necessary
    if(appendDateTime){
      int idx = adjustedName.lastIndexOf('.');
      if(idx>-1){
        adjustedName =  adjustedName.substring(0,idx)+"_"+sdf.format(new Date())+adjustedName.substring(idx);
      }else{
        adjustedName = adjustedName+"_"+sdf.format(new Date());
      }
    }

    return adjustedName;
  }
  private String getCorrectFileExtension(){
    switch (type){
      case EXCEL :
        return ".xlsx";
      case CSV:
        return ".csv";
      default:
        return "";
    }
  }

}
