package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableRow implements Serializable {
List<Object> values;
}
