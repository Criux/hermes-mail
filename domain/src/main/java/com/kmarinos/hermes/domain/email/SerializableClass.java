package com.kmarinos.hermes.domain.email;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import lombok.Data;

@Data
public class SerializableClass implements Serializable {

  String name;
  byte[] data;

  public SerializableClass(Class<?> clazz){
    this.setName(clazz.getName());
    InputStream is = clazz.getClassLoader().getResourceAsStream(this.getName().replace(".","/")+"-class");
    try{
      setData(fromInputStream(is));
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  private byte[] fromInputStream(InputStream is) throws IOException{
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[4096];
    while((nRead = is.read(data,0,data.length))!=-1){
      buffer.write(data,0,nRead);
    }
    return buffer.toByteArray();
  }
}
