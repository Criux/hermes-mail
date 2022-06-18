package com.kmarinos.hermes.domain;

import com.kmarinos.hermes.domain.email.SerializableFunction;
import com.kmarinos.hermes.domain.email.SerializablePredicate;
import java.io.IOException;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class SerializableLambdaUtils {
  public static void showIdentity(SerializableFunction consumer){
    String name = name(consumer);
    Class<?> clazz = consumer.getClass();
    System.out.printf("class name     : %s%n",clazz.getName());
    System.out.printf("class hashcode : %s%n",clazz.hashCode());
    System.out.printf("canonical name : %s%n",clazz.getCanonicalName());
    System.out.printf("enclosing class: %s%n",clazz.getEnclosingClass());
    System.out.printf("lambda name     : %s%n",name);
  }
  public static void showIdentity(SerializablePredicate consumer){
    String name = name(consumer);
    Class<?> clazz = consumer.getClass();
    System.out.printf("class name     : %s%n",clazz.getName());
    System.out.printf("class hashcode : %s%n",clazz.hashCode());
    System.out.printf("canonical name : %s%n",clazz.getCanonicalName());
    System.out.printf("enclosing class: %s%n",clazz.getEnclosingClass());
    System.out.printf("lambda name     : %s%n",name);
  }
  public static String getEnclosingClassName(SerializableFunction lambda){return name(lambda);}
  public static String getEnclosingClassName(SerializablePredicate lambda){return name(lambda);}

  private static String name(Object consumer){return method(consumer).getDeclaringClass().getName();}

  private static SerializedLambda serialized(Object lambda){
    try{
      Method writeMethod = lambda.getClass().getDeclaredMethod("writeReplace");
      writeMethod.setAccessible(true);
      return (SerializedLambda) writeMethod.invoke(lambda);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
  private static Class<?> getContainingClass(SerializedLambda lambda){
    try{
      String className = lambda.getImplClass().replaceAll("/",".");
      return Class.forName(className);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
  private static Method method(Object lambda){
    SerializedLambda serialized = serialized(lambda);
    Class<?> containingClass = getContainingClass(serialized);
    return Arrays.stream(containingClass.getDeclaredMethods())
        .filter(method-> Objects.equals(method.getName(),serialized.getImplMethodName()))
        .findFirst()
        .orElseThrow(RuntimeException::new);
  }
}
