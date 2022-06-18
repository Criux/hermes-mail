package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Body implements Serializable {

  public static final String lineSeperator = System.lineSeparator();
  String greeting;
  String message;
  String closing;
  String signature;
  String postscript;
  String overrideText;

  private Body() {}

  public String toEmailText() {
    if (overrideText != null && !overrideText.trim().isEmpty()) {
      return overrideText;
    }
    var sb = new StringBuilder();
    sb.append(greeting);
    sb.append(lineSeperator);
    sb.append(lineSeperator);
    sb.append(message);
    sb.append(lineSeperator);
    sb.append(lineSeperator);
    sb.append(closing);
    if (signature != null && signature.trim().isEmpty()) {
      sb.append(lineSeperator);
      sb.append(lineSeperator);
      sb.append(signature);
    }
    if (postscript != null && postscript.trim().isEmpty()) {
      sb.append(lineSeperator);
      sb.append(lineSeperator);
      sb.append("---");
      sb.append(postscript);
      sb.append(lineSeperator);
    }
    return sb.toString();
  }

  public static Body compose() {
    return new Body();
  }

  public static Body compose(String text) {
    var body = new Body();
    body.setOverrideText(text);
    return body;
  }
  public Body greeting(String greeting){
    this.setGreeting(greeting);
    return this;
  }
  public Body message(String message){
    this.setMessage(message);
    return this;
  }
  public Body closing(String closing){
    this.setClosing(closing);
    return this;
  }
  public Body signature(String signature){
    this.setSignature(signature);
    return this;
  }
  public Body postscript(String postscript){
    this.setPostscript(postscript);
    return this;
  }
}
