package com.kmarinos.hermes.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class MyObjectInputStream extends ObjectInputStream {

  static ClassLoader myClassLoader;
  public MyObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException{
    this(in);
    MyObjectInputStream.myClassLoader=classLoader;
  }
  public MyObjectInputStream(InputStream in) throws IOException{
    super(in);
  }
  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException{
    String name = desc.getName();
    try{
      return Class.forName(name,false,latestUserDefinedLoader());
    }catch(ClassNotFoundException ex){
      System.err.println("error");
      throw ex;
    }
  }
  private static ClassLoader latestUserDefinedLoader(){return myClassLoader;}

}
