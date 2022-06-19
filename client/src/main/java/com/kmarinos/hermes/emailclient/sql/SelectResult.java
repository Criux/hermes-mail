package com.kmarinos.hermes.emailclient.sql;

import java.util.LinkedHashMap;
import java.util.Map;

public class SelectResult {

  private final Map<Key<?>, Object> values = new LinkedHashMap<>();
  private final Map<String, Key<?>> keys = new LinkedHashMap<>();

  private <T> void put(Key<T> key, T value) {
    values.put(key, value);
  }
  public <T> void put(String identifier,Class<T>type,Object value){
    Key<T> key = new SelectResult.Key<>(identifier,type);
    keys.put(identifier,key);
    values.put(key,value);
  }
  private  <T> T get(Key<T> key){
    if(key==null){
      return null;
    }
    return key.type.cast(values.get(key));
  }
  @SuppressWarnings("unchecked")
  public <T> T get(String keyName){
    Key<?> key = keys.get(keyName);
    return (T) get(key);
  }
  public <T> T get(String keyName,Class<T>type){return get(keyName);}
  public Map<String,?> getAll(){
    if(keys == null){
      return new LinkedHashMap<>();
    }
    Map<String,?> map = new LinkedHashMap<>();
    for(String strKey:keys.keySet()){
      map.put(strKey,get(strKey));
    }
    return map;
    //return key.keySet().stream().filter(x->x!=null).collect(Collectors.toMap(x->x,x->get(x)));
  }
public Class<?> typeOf(String keyName){
    return keys.get(keyName).type;
}
  public class Key<T> {
    final String identifier;
    final Class<T> type;

    public Key(String identifier, Class<T> type) {
      this.identifier = identifier;
      this.type = type;
    }
  }
}
