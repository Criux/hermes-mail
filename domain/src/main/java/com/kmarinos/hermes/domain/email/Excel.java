package com.kmarinos.hermes.domain.email;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class Excel {
  List<Sheet> sheets = new ArrayList<>();
  String name;
  boolean appendDateTime = false;

  public static Excel create() {
    return new Excel();
  }
  public Excel sheet(Sheet sheet){
    this.getSheets().add(sheet);
    return this;
  }
  public Excel name(String name){
    this.name = name;
    return this;
  }
  public Excel appendDateTime(){
    this.appendDateTime=true;
    return this;
  }
}
