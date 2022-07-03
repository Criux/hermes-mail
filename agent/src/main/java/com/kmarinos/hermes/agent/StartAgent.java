package com.kmarinos.hermes.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableScheduling
public class StartAgent{

  public static void main(String[] args) {
    SpringApplication.run(StartAgent.class, args);
  }

  @Bean
  public CommandLineRunner initAgent(){
    return args -> {

    };
  }
}