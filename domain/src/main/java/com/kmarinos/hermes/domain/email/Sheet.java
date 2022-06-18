package com.kmarinos.hermes.domain.email;

import java.sql.Connection;
import lombok.Getter;

@Getter
public class Sheet {
  Connection connection;
  String sql;
  String name;

  private Sheet() {}

  public static Sheet fromSQL(Connection connection, String sql) {
    Sheet sheet = new Sheet();
    sheet.connection = connection;
    sheet.sql = sql;
    return sheet;
  }
  public Sheet name(String name){
    this.name = name;
    return this;
  }
}
