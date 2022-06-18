package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConditionalText implements Serializable {

  String name;
  SerializablePredicate condition;
  SerializableFunction actionTrue;
  SerializableFunction actionFalse;
}
