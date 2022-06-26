package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.domain.email.Email;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.emailservice.service.EmailRequestService;
import com.kmarinos.hermes.emailservice.service.EmailService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SerializationUtils;
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
  private final EmailRequestService emailRequestService;

  @GetMapping("test")
  public String testMe() {
    return "Hello";
  }

  @PostMapping(
      value = "send",
      consumes = {"multipart/form-data"})
  public ResponseEntity<Void> persistAndProcess(
      @RequestParam("lambda") MultipartFile l, @RequestParam("email") MultipartFile e)
      throws IOException, SQLException {
    EmailRequest request =
        emailRequestService.registerEmailRequest(null, l.getBytes(), e.getBytes());

    sendEmail(
        request.getL().getBinaryStream().readAllBytes(),
        request.getE().getBinaryStream().readAllBytes());
    return ResponseEntity.accepted().build();
    //    return multipart(l,e);
  }

  public void sendEmail(byte[] l, byte[] e) {
    MyClassLoader myClassLoader = null;
    try {
      Map<String, byte[]> classes =
          (Map<String, byte[]>) new ObjectInputStream(new ByteArrayInputStream(l)).readObject();
      myClassLoader = new MyClassLoader(this.getClass().getClassLoader(), classes);
      for (String className : classes.keySet()) {
        try {
          myClassLoader.loadClass(className);
        } catch (Exception ex) {
          System.err.println("Class not found 1");
        }
      }
    } catch (Exception ex) {
      System.err.println("Class not found 2");
    }
    Object obj = null;
    try {
      System.out.println((e.length / 1024) + " KB");
      obj = new MyObjectInputStream(new ByteArrayInputStream(e), myClassLoader).readObject();
      if (obj.getClass().equals(Email.class)) {
        Email email = (Email) obj;
        emailService.send(email);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }
  }

  public ResponseEntity<Void> multipart(
      @RequestParam("lambda") MultipartFile l, @RequestParam("email") MultipartFile e)
      throws IOException {

    MyClassLoader myClassLoader = null;
    try {
      Map<String, byte[]> classes =
          (Map<String, byte[]>)
              new ObjectInputStream(new ByteArrayInputStream(l.getBytes())).readObject();
      myClassLoader = new MyClassLoader(this.getClass().getClassLoader(), classes);
      for (String className : classes.keySet()) {
        try {
          myClassLoader.loadClass(className);
        } catch (Exception ex) {
          System.err.println("Class not found 1");
        }
      }
    } catch (Exception ex) {
      System.err.println("Class not found 2");
    }
    Object obj = null;
    try {
      System.out.println((e.getBytes().length / 1024) + " KB");
      obj =
          new MyObjectInputStream(new ByteArrayInputStream(e.getBytes()), myClassLoader)
              .readObject();
      if (obj.getClass().equals(Email.class)) {
        Email email = (Email) obj;
        emailService.send(email);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }
    return ResponseEntity.accepted().build();
  }

  private void loadLambdas(String className, ClassLoader classLoader) {
    try {
      classLoader.loadClass(className);
    } catch (ClassNotFoundException ex) {
      // ignored
    }
  }
}
