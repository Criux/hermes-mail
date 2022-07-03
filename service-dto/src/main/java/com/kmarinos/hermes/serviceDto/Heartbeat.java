package com.kmarinos.hermes.serviceDto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Heartbeat {
  HeartbeatStatus status;
  LocalDateTime at;

}
