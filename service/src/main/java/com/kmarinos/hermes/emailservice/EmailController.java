package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.dto.EmailRequestDTO;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import java.io.IOException;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {
  private final EmailRequestService emailRequestService;
  private final AgentService agentService;
  private final RestTemplate restTemplate = new RestTemplate();

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
    var agent = agentService.getNextAvailable();
    restTemplate.postForEntity(agent.getListeningOn()+"/email/assign", EmailRequestDTO.GET(request),Void.class);

//    sendEmail(
//        request.getL().getBinaryStream().readAllBytes(),
//        request.getE().getBinaryStream().readAllBytes());
    return ResponseEntity.accepted().build();
    //    return multipart(l,e);
  }

//  public void sendEmail(byte[] l, byte[] e) {
//    MyClassLoader myClassLoader = null;
//    try {
//      Map<String, byte[]> classes =
//          (Map<String, byte[]>) new ObjectInputStream(new ByteArrayInputStream(l)).readObject();
//      myClassLoader = new MyClassLoader(this.getClass().getClassLoader(), classes);
//      for (String className : classes.keySet()) {
//        try {
//          myClassLoader.loadClass(className);
//        } catch (Exception ex) {
//          System.err.println("Class not found 1");
//        }
//      }
//    } catch (Exception ex) {
//      System.err.println("Class not found 2");
//    }
//    Object obj = null;
//    try {
//      System.out.println((e.length / 1024) + " KB");
//      obj = new MyObjectInputStream(new ByteArrayInputStream(e), myClassLoader).readObject();
//      if (obj.getClass().equals(Email.class)) {
//        Email email = (Email) obj;
//        emailService.send(email);
//      }
//    } catch (IOException ex) {
//      ex.printStackTrace();
//    } catch (ClassNotFoundException ex) {
//      ex.printStackTrace();
//    }
//  }
//
//  public ResponseEntity<Void> multipart(
//      @RequestParam("lambda") MultipartFile l, @RequestParam("email") MultipartFile e)
//      throws IOException {
//
//    MyClassLoader myClassLoader = null;
//    try {
//      Map<String, byte[]> classes =
//          (Map<String, byte[]>)
//              new ObjectInputStream(new ByteArrayInputStream(l.getBytes())).readObject();
//      myClassLoader = new MyClassLoader(this.getClass().getClassLoader(), classes);
//      for (String className : classes.keySet()) {
//        try {
//          myClassLoader.loadClass(className);
//        } catch (Exception ex) {
//          System.err.println("Class not found 1");
//        }
//      }
//    } catch (Exception ex) {
//      System.err.println("Class not found 2");
//    }
//    Object obj = null;
//    try {
//      System.out.println((e.getBytes().length / 1024) + " KB");
//      obj =
//          new MyObjectInputStream(new ByteArrayInputStream(e.getBytes()), myClassLoader)
//              .readObject();
//      if (obj.getClass().equals(Email.class)) {
//        Email email = (Email) obj;
//        emailService.send(email);
//      }
//    } catch (IOException ex) {
//      ex.printStackTrace();
//    } catch (ClassNotFoundException ex) {
//      ex.printStackTrace();
//    }
//    return ResponseEntity.accepted().build();
//  }
//
//  private void loadLambdas(String className, ClassLoader classLoader) {
//    try {
//      classLoader.loadClass(className);
//    } catch (ClassNotFoundException ex) {
//      // ignored
//    }
//  }
}
