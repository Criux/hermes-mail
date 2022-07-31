package com.kmarinos.hermes.agent.config;

import com.kmarinos.hermes.serviceDto.AgentGET;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

  @Bean
  public AgentGET initAgent() {
    return AgentGET.builder().build();
  }
}
