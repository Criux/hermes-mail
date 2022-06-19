package com.kmarinos.hermes.emailclient.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleSQLClient extends SQLClient{

  String driverClass;
  String jdbc;
  String username;
  String password;

  public SimpleSQLClient(String driverClass,String jdbc,String username,String password){
    this.driverClass=driverClass;
    this.jdbc=jdbc;
    this.username=username;
    this.password=password;
  }
  public SimpleSQLClient(Connection connection){
    this.conn=connection;
  }
  @Override
  void initConnection(){
    try{
      if(conn!=null && !conn.isClosed()){
        return;
      }
      if(driverClass!=null && !driverClass.isEmpty()){
        Class.forName(driverClass);
      }
      conn= DriverManager.getConnection(jdbc,username,password);
      System.out.println("Connection established:"+jdbc);
    }catch(ClassNotFoundException e){
      System.err.println("No database Driver!");
      return;
    }catch (SQLException e){
      e.printStackTrace();
    }
  }
}
