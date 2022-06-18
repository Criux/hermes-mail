package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class Table implements Serializable {
String name;
List<TableHeader> headers;
List<TableRow> rows;
}
