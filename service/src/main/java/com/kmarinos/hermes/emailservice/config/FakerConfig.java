package com.kmarinos.hermes.emailservice.config;

import com.github.javafaker.Faker;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FakerConfig {

  @Bean
  public Faker initFaker(){
    return new Faker(new Locale("en-US"));
  }
}
