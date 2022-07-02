package com.kmarinos.hermes.agent;

import java.util.Map;

public class MyClassLoader extends ClassLoader{

  Map<String,byte[]> dataMap;
  public MyClassLoader(ClassLoader parent,Map<String,byte[]>dataMap){
    super(parent);
    this.dataMap=dataMap;
  }
  public Class findClass(String name){
    System.out.println("Try to load for"+name);
    return defineClass(null,dataMap.get(name),0,dataMap.get(name).length);
  }
}
