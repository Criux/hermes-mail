package com.kmarinos.hermes.emailclient.sql;

import java.sql.Connection;

public class SQLClientFactory {

  public static SQLClient connectTo(Connection connection){
    return new SimpleSQLClient(connection);
  }
  private static SQLClient connectTo(String jdbc,String username, String password){
    return new SimpleSQLClient(deriveDriverClassFromConnectionString(jdbc),jdbc,username,password);
  }
  private static String deriveDriverClassFromConnectionString(String connectionString){
    if(connectionString == null){
      return "";
    }
    if(connectionString.contains("jdbc:")){
      String driverIdentifier = connectionString.split(":",2)[1];
      if(driverIdentifier.startsWith("oracle")){
        return "oracle.jdbc.driver.OracleDriver";
      }else if(driverIdentifier.startsWith("as400")){
        return "com.ibm.as400.access.AS400JDBCDriver";
      } else{
        return "";
      }
    }
    return "";
  }

}
