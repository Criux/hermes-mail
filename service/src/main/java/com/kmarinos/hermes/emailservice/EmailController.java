package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.dto.AttachedFileDTO;
import com.kmarinos.hermes.emailservice.model.Agent;
import com.kmarinos.hermes.emailservice.model.AttachedFile;
import com.kmarinos.hermes.emailservice.model.EmailInstance;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.emailservice.model.Processing;
import com.kmarinos.hermes.emailservice.model.ProcessingMessage;
import com.kmarinos.hermes.serviceDto.AttachedFileGET;
import com.kmarinos.hermes.serviceDto.AttachedFilePOST;
import com.kmarinos.hermes.serviceDto.EmailInstancePOST;
import com.kmarinos.hermes.serviceDto.ProgressReport;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {
  private final EmailRequestService emailRequestService;
  private final RecipientService recipientService;
  private final FileService fileService;
  private final AgentService agentService;


  @GetMapping("test")
  public String testMe() {
    return "Hello";
  }
  @GetMapping("{id}/attachments")
  public ResponseEntity<List<AttachedFileGET>> getAttachmentsForEmail(@PathVariable("id")String id,@RequestHeader("X-Agent-Token") String agentToken){
    agentService.getAgentFromToken(agentToken);
    final var emailRequest = emailRequestService.getEmailRequestById(id);
    final var attachments =fileService.fetchAttachments(emailRequest);
    return ResponseEntity.ok(AttachedFileDTO.GET(attachments,fileService::getFileContent));
  }
  @PostMapping("progress")
  public ResponseEntity<Void> reportProgress(@RequestBody Processing progressPOST){
    return ResponseEntity.accepted().build();
  }
  @PostMapping("attach/{erid}")
  public ResponseEntity<Void> registerProcessedAttachment(@PathVariable("erid")String emailRequestId,@RequestBody
  ProgressReport<AttachedFilePOST> pr, @RequestHeader("X-Agent-Token") String agentToken){
    Agent agent=agentService.getAgentFromToken(agentToken);
    log.info("Attachment registered...");
    var attachedFile =fileService.createAttachedFile(pr.getPayload(),emailRequestService::getEmailRequestById);
    emailRequestService.registerAttachmentProcessed(attachedFile,pr,agent);
    return ResponseEntity.accepted().build();
  }
  @PostMapping("sent/{erid}")
  public ResponseEntity<Void> registerProcessedEmailInstance(@PathVariable("erid")String emailRequestId,@RequestBody
  ProgressReport<EmailInstancePOST> pr, @RequestHeader("X-Agent-Token") String agentToken){
    Agent agent=agentService.getAgentFromToken(agentToken);
    EmailInstance emailInstance = emailRequestService.createEmailInstance(pr.getPayload(),()->recipientService.getByRequestOrCreate(pr.getPayload().getRecipientPOST()));
    emailRequestService.registerEmailInstanceProcessed(emailInstance,pr,agent);
    log.info("Email instance registered...");
    return ResponseEntity.accepted().build();
  }
  @PostMapping("complete-attachments/{erid}")
  public ResponseEntity<Void>completeAttachments(@PathVariable("erid")String emailRequestId,@RequestHeader("X-Agent-Token") String agentToken){
    Agent agent=agentService.getAgentFromToken(agentToken);
    var emailRequest=emailRequestService.getEmailRequestById(emailRequestId);
    emailRequestService.completeAttachmentProcessing(emailRequest,agent);
    return ResponseEntity.accepted().build();
  }
  @PostMapping("complete-email-instances/{erid}")
  public ResponseEntity<Void>completeEmailInstances(@PathVariable("erid")String emailRequestId,@RequestHeader("X-Agent-Token") String agentToken){
    Agent agent=agentService.getAgentFromToken(agentToken);
    var emailRequest=emailRequestService.getEmailRequestById(emailRequestId);
    emailRequestService.completeEmailInstanceProcessing(emailRequest,agent);
    return ResponseEntity.accepted().build();
  }

  @PostMapping(
      value = "send",
      consumes = {"multipart/form-data"})
  public ResponseEntity<Void> persistAndProcess(
      @RequestParam("lambda") MultipartFile l, @RequestParam("email") MultipartFile e, @RequestParam(required = false,name="attachments") MultipartFile a)
      throws IOException, SQLException {
    EmailRequest request = a==null?
        emailRequestService.registerEmailRequest(null, l.getBytes(), e.getBytes()):
        emailRequestService.registerEmailRequest(null, l.getBytes(), e.getBytes(),a.getBytes());

//    agentService.sendWithNextAvailableAgent(request);

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
