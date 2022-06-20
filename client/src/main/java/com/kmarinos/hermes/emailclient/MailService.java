package com.kmarinos.hermes.emailclient;

import com.kmarinos.hermes.domain.email.Email;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class MailService {

  CloseableHttpClient client;
  private static MailService instance;
  String url = "http://localhost:8080/email";

  private MailService(){
    client = HttpClients.createDefault();
  }
  public static MailService getInstance(){
    if(instance==null){
      instance = new MailService();
    }
    return instance;
  }
  public void send(Map<String,byte[]> classes, Email email){
    try{
      this.sendMultipart(classes,email);
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  private void sendMultipart(Map<String,byte[]>classes,Email email)throws IOException{
    HttpPost httpPost = new HttpPost(url+"/send");
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    //upload classes that contain lambda expressions
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(classes);
    out.flush();
    builder.addBinaryBody("lambda",bos.toByteArray(), ContentType.DEFAULT_BINARY,"lambda");

    //upload email object
    bos = new ByteArrayOutputStream();
    out = new ObjectOutputStream(bos);
    out.writeObject(email);
    out.flush();
    builder.addBinaryBody("email",bos.toByteArray(),ContentType.DEFAULT_BINARY,"email");

    httpPost.setEntity(builder.build());
    CloseableHttpResponse response = client.execute(httpPost);
    int statusCode = response.getStatusLine().getStatusCode();
    if(statusCode==202){
      System.out.println("Email sent to all recipients");
    }else{
      System.err.println("Cannot send Email. Response Code:"+response.getStatusLine().getStatusCode());
    }
  }
  private <T> void post(T object,String url){
    HttpPost httpPost = new HttpPost(url);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = null;
    try{
      out = new ObjectOutputStream(bos);
      out.writeObject(object);
      out.flush();

      httpPost.setEntity(new ByteArrayEntity(bos.toByteArray()));

      CloseableHttpResponse response = client.execute(httpPost);
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  private byte[] fromInputStream(InputStream is)throws IOException{
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[4096];
    while((nRead=is.read(data,0,data.length))!=-1){
      buffer.write(data,0,nRead);
    }
    return buffer.toByteArray();
  }
}
