package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.domain.email.Email;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("email")
@RequiredArgsConstructor
@Slf4j
public class EmailResource {
  private final EmailService emailService;
  @GetMapping("test")
  public String testMe(){return "Hello";}
  @PostMapping(value="send",consumes = {"multipart/form-data"})
  public ResponseEntity<Void> multipart(@RequestParam("lambda")MultipartFile l, @RequestParam("email") MultipartFile e) throws IOException {
    MyClassLoader myClassLoader = null;
    try{
      Map<String,byte[]> classes = (Map<String,byte[]>)new ObjectInputStream(new ByteArrayInputStream(l.getBytes())).readObject();
      myClassLoader = new MyClassLoader(this.getClass().getClassLoader(),classes);
      for(String className:classes.keySet()){
        try{
          myClassLoader.loadClass(className);
        }catch(Exception ex){
          System.err.println("Class not found 1");
        }
      }
    }catch(Exception ex){
      System.err.println("Class not found 2");
    }
    Object obj = null;
    try{
      obj = new MyObjectInputStream(new ByteArrayInputStream(e.getBytes()),myClassLoader).readObject();
      if(obj.getClass().equals(Email.class)){
        Email email = (Email)obj;
        emailService.send(email);
      }
    }catch (IOException ex){
      ex.printStackTrace();
    }catch(ClassNotFoundException ex){
      ex.printStackTrace();
    }
    return ResponseEntity.accepted().build();
  }
  private void loadLambdas(String className,ClassLoader classLoader){
    try{
      classLoader.loadClass(className);
    }catch(ClassNotFoundException ex){
      //ignored
    }
  }
}
