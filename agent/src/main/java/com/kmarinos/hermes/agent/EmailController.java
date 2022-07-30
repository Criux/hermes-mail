package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.domain.email.Email;
import com.kmarinos.hermes.domain.email.EmailAttachment;
import com.kmarinos.hermes.serviceDto.AttachmentRequestGET;
import com.kmarinos.hermes.serviceDto.EmailRequestGET;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EmailController {
  private final EmailService emailService;

  @GetMapping("ping")
  public ResponseEntity<Void> ping() {
    return ResponseEntity.accepted().build();
  }

  @PostMapping("assign-email")
  public ResponseEntity<Void> webhookSendEmail(@RequestBody EmailRequestGET requestPOST){
    new Thread(()->{
      log.info("Trying to send email");
      sendEmail(requestPOST.getL(), requestPOST.getE());
      log.info("Email sent");
    }).start();


    return ResponseEntity.accepted().build();
  }
  @PostMapping("assign-attachments")
  public ResponseEntity<Void>webhookProcessAttachements(@RequestBody AttachmentRequestGET requestPOST){
    new Thread(()->{
      log.info("Prossessing attachments");
      processAttachments(requestPOST.getEmailRequestId(), requestPOST.getA());
      log.info("Attachments done");
    }).start();
    return ResponseEntity.accepted().build();
  };

  @PostMapping(
      value = "send",
      consumes = {"multipart/form-data"})
  public ResponseEntity<Void> send(
      @RequestParam("lambda") MultipartFile l, @RequestParam("email") MultipartFile e)
      throws IOException, SQLException {
    MyClassLoader myClassLoader = null;
    try {
      Map<String, byte[]> classes =
          (Map<String, byte[]>) new ObjectInputStream(new ByteArrayInputStream(l.getBytes())).readObject();
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
      obj = new MyObjectInputStream(new ByteArrayInputStream(e.getBytes()), myClassLoader).readObject();
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
  private void processAttachments(String emailRequestId,byte[] a){
    Object obj = null;
    try {
      System.out.println((a.length / 1024) + " KB");
      obj = new MyObjectInputStream(new ByteArrayInputStream(a), this.getClass().getClassLoader()).readObject();
      if (obj.getClass().equals(ArrayList.class)) {
        List<?> list = (ArrayList<?>) obj;
        var attachmentList = new ArrayList<EmailAttachment>();
        for(Object att:list){
          if(att instanceof EmailAttachment){
            attachmentList.add((EmailAttachment) att);
          }
        }
        emailService.processAttachments(emailRequestId,attachmentList);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }
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
