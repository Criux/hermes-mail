package com.kmarinos.hermes.emailclient;

import com.kmarinos.hermes.domain.email.Body;
import com.kmarinos.hermes.domain.email.Excel;
import com.kmarinos.hermes.domain.email.Sheet;
import com.kmarinos.hermes.emailclient.sql.SQLClient;
import com.kmarinos.hermes.emailclient.sql.SQLClientFactory;
import java.util.Date;

public class TestMe {

  public static final String stmt1 =
      "SELECT film_id, title, description, release_year, language_id, rental_duration, rental_rate, length, replacement_cost, rating, last_update\n"
          + "FROM dvdrental.film;\n";

  public static void main(String[] args){

    SQLClient sqlClient = SQLClientFactory.connectTo("jdbc:postgresql://localhost:5432/postgres?currentSchema=dvdrental","postgres","postgres");
    for(int i = 0;i<5;i++){
      new Thread(()->{
        Email.compose().to("bot2@mail.marinos.com")
            .subject("test")
            .body(Body.compose("::someText::"))
            .attach(Excel.create().name("report.xlsx")
                .sheet(
                    Sheet.fromSQL(sqlClient.getConnection(),stmt1)
                )
            )
            .send();
      }).start();
//      Email.compose().to("bot2@mail.marinos.com")
//          .subject("test")
//          .body(Body.compose("::someText::"))
//          .param("someText",ctx->new Date().toString())
//          .attach(Excel.create().name("report.xlsx")
//              .sheet(
//                  Sheet.fromSQL(sqlClient.getConnection(),stmt1)
//              )
//          )
//          .send();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
