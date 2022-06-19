package com.kmarinos.hermes.emailclient.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLExecutor {
  String sql;
  List<Object> inputParams = new ArrayList<>();
  long parameterCount = 0;
  final SQLClient client;
  Function<SelectResult, ?> reflectionMapper =
      a -> {
        throw new RuntimeException("This method is not yet implemented.");
      };

  public SQLExecutor(String sql, SQLClient client) {
    this.sql = sql;
    this.client = client;
    this.parameterCount = sql.chars().filter(c -> c == '?').count();
  }

  public SQLExecutor withParams(List<? extends Object> inputParams) {
    if (inputParams == null || inputParams.size() != parameterCount) {
      throw new RuntimeException(
          "The input parameters don't match the parameters defined in the sql statement.");
    }
    inputParams.forEach(this::withParam);
    return this;
  }

  public SQLExecutor withParam(Object inputParam) {
    this.getInputParams().add(inputParam);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> andGetAs(Class<T> resultClass) {
    return (List<T>) this.andCollect(reflectionMapper);
  }

  public void andConsume(Consumer<SelectResult> consumer) {
    this.andCollect(
        s -> {
          consumer.accept(s);
          return null;
        });
  }

  public List<SelectResult> andGet() {
    return this.andCollect(Function.identity());
  }

  public <T> List<T> andCollect(Function<SelectResult, T> c) {
    List<T> resultList = new ArrayList<>();
    try (PreparedStatement pstmt = client.getValidConnection().prepareStatement(this.sql)) {
      for (int i = 0; i < inputParams.size(); i++) {
        pstmt.setString(i + 1, inputParams.get(i) + "");
      }
      ResultSet rs = pstmt.executeQuery();
      ResultSetMetaData meta = rs.getMetaData();
      while (rs.next()) {
        SelectResult ctx = new SelectResult();
        for (int idx = 0; idx < meta.getColumnCount(); idx++) {
          String columnName = meta.getColumnName(idx + 1);
          Class<?> columnType;
          Object value;
          try {
            columnType =
                Class.forName(JDBC_TYPE_MAP.get(meta.getColumnTypeName(idx + 1).toUpperCase()));
            value = columnType.cast(rs.getObject(idx + 1));
          } catch (ClassNotFoundException e) {
            System.err.println("Couldn't find class:" + columnName);
            columnType = String.class;
            value = rs.getString(idx + 1);
          }
          ctx.put(columnName, columnType, value);
        }
        resultList.add(c.apply(ctx));
      }
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
    return resultList;
  }

  public String getSQL() {
    return this.sql;
  }

  public List<Object> getInputParams() {
    return this.inputParams;
  }
  /*
  Created from https://download.oracle.com/otn-pub/jcp/jdbc-4_2-mrel2-spec/jdbc4.2-fr-spec.pdf?AuthParam=1655625721_5d7223a16d41b2af60c3608ec4c13a06
  Appendix B3
   */
  private static final Map<String, String> JDBC_TYPE_MAP =
      Stream.of(new String[][] {
          {"CHAR","java.lang.String"},
          {"VARCHAR","java.lang.String"},
          {"VARCHAR2","java.lang.String"},
          {"LONGVARCHAR","java.lang.String"},
          {"NCHAR","java.lang.String"},
          {"NVARCHAR","java.lang.String"},
          {"LONGNVARCHAR","java.lang.String"},
          {"GRAPHIC","java.lang.String"},
          {"VARGRAPHIC","java.lang.String"},
          {"NUMERIC","java.math.BigDecimal"},
          {"DECIMAL","java.math.BigDecimal"},
          {"NUMBER","java.math.BigDecimal"},
          {"BIT","java.lang.Boolean"},
          {"BOOLEAN","java.lang.Boolean"},
          {"TINYINT","java.lang.Integer"},
          {"SMALLINT","java.lang.Integer"},
          {"INTEGER","java.lang.Integer"},
          {"BIGINT","java.lang.Long"},
          {"REAL","java.lang.Float"},
          {"FLOAT","java.lang.Double"},
          {"Double","java.lang.Double"},
          {"BINARY","byte[]"},
          {"VARBINARY","byte[]"},
          {"LONGVARBINARY","byte[]"},
          {"DATE","java.sql.Date"},
          {"TIME","java.sql.Time"},
          {"TIMESTAMP","java.sql.Timestamp"},
          {"DISTINCT","java.lang.Object"},
          {"CLOB","java.sql.Clob"},
          {"NCLOB","java.sql.NClob"},
          {"BLOB","java.sql.Blob"},
          {"ARRAY","java.sql.Array"},
          {"STRUCT","java.sql.Struct"},
          {"REF","java.sql.Ref"},
          {"DATALINK","java.net.URL"},
          {"ROWID","java.sql.RowId"},
          {"SQLXML","java.sql.SQLXML"},
      }).collect(Collectors.toMap(data->data[0],data->data[1]));
}
