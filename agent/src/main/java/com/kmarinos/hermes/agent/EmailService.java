package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.domain.email.AttachmentFileType;
import com.kmarinos.hermes.domain.email.ConditionalText;
import com.kmarinos.hermes.domain.email.Email;
import com.kmarinos.hermes.domain.email.EmailAttachment;
import com.kmarinos.hermes.domain.email.EmailContext;
import com.kmarinos.hermes.domain.email.EmailRecipient;
import com.kmarinos.hermes.domain.email.Table;
import com.kmarinos.hermes.domain.email.TablesAttachment;
import com.kmarinos.hermes.serviceDto.AttachedFileGET;
import com.kmarinos.hermes.serviceDto.AttachedFilePOST;
import com.kmarinos.hermes.serviceDto.EmailInstancePOST;
import com.kmarinos.hermes.serviceDto.RecipientPOST;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

  private final JavaMailSender sender;
  private final TableService tableService;
  private final Parser parser;
  private final HtmlRenderer renderer;

  private Map<AttachmentFileType, String> mimeMap = new HashMap<>();
  @Getter
  @Setter
  private boolean canProcess=true;
  private final BackendClient backendClient;

  public EmailService(
      JavaMailSender sender, TableService tableService, Parser parser, HtmlRenderer renderer,
      BackendClient backendClient) {
    this.sender = sender;
    this.tableService = tableService;
    this.parser = parser;
    this.renderer = renderer;
    this.backendClient = backendClient;

    this.mimeMap.put(
        AttachmentFileType.EXCEL,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    this.mimeMap.put(AttachmentFileType.CSV, "text/csv");
  }
  public void processEmail(Email email,String emailRequestId) {
    List<EmailAttachment>attachments = backendClient.fetchAttachment(emailRequestId).stream().map(this::toEmailAttachment).toList();
    email.getToRecipients().forEach(r->send(email,attachments,Map.of("TO",List.of(r)),report->{
      report.setEmailRequestId(emailRequestId);
      backendClient.reportEmailInstanceProgress(report);
    }));
    backendClient.completeEmailInstances(emailRequestId);
  }

  private EmailAttachment toEmailAttachment(AttachedFileGET attachedFileGET) {
    var attachment = new EmailAttachment();
    attachment.setName(attachedFileGET.getFilename());
    attachment.setType(AttachmentFileType.valueOf(attachedFileGET.getFiletype()));
    attachment.setName(attachedFileGET.getFilename());
    attachment.setContent(attachedFileGET.getContent());
    return attachment;
  }

  public void send(Email email,List<EmailAttachment>attachments,Map<String,List<EmailRecipient>>recipients,
      Consumer<EmailInstancePOST> reportConsumer){
    List<EmailInstancePOST>reports = new ArrayList<>();
    if (recipients.isEmpty()) {
      log.warn("No recipients were defined. No email will be sent");
      return;
    }
    // email each recipient individually



      EmailContext mailCtx = new EmailContext();
    List<EmailRecipient> toRecipients = recipients.getOrDefault("TO", new ArrayList<>());
    List<EmailRecipient> ccRecipients = recipients.getOrDefault("CC", new ArrayList<>());
    List<EmailRecipient> bccRecipients = recipients.getOrDefault("BCC", new ArrayList<>());
    //only set the recipient in EmailContext if only one person is receiving the email
    if(toRecipients.size()==1&&ccRecipients.isEmpty()&&bccRecipients.isEmpty()){
      mailCtx.setRecipient(toRecipients.get(0));
    }else{
      mailCtx.setRecipient(new EmailRecipient());
    }
      MimeMessage message = sender.createMimeMessage();
      MimeMessageHelper helper = null;
      try {
        helper = new MimeMessageHelper(message, true);
        helper.setFrom("bot1@mail.marinos.com");
        for (EmailRecipient toRecipient : toRecipients) {
          helper.setTo(toRecipient.getEmail());
        }
        for (EmailRecipient ccRecipient : ccRecipients) {
          helper.setCc(ccRecipient.getEmail());
        }
        for (EmailRecipient bccRecipient : bccRecipients) {
          helper.setBcc(bccRecipient.getEmail());
        }
        // process attachments
        // add names of attachments to EmailContext
        mailCtx.setAttachmentFilenames(
            attachments.stream()
                .map(EmailAttachment::getName)
                .collect(Collectors.toList()));
        // add attachments to email
        for (EmailAttachment preparedAttachment : attachments) {
          if (preparedAttachment.getType() != null
              && !preparedAttachment.getType().equals(AttachmentFileType.UNKNOWN)) {
            helper.addAttachment(
                preparedAttachment.getName(),
                new ByteArrayDataSource(
                    preparedAttachment.getContent(), mimeMap.get(preparedAttachment.getType())));
          } else {
            helper.addAttachment(
                preparedAttachment.getName(),
                new ByteArrayResource(preparedAttachment.getContent()));
          }
        }
        final String[] bodyText = {email.getBody().toEmailText()};
        // set empty string when value is null
        if (email.getBody().getGreeting() == null) {
          email.getBody().setGreeting("");
        }
        if (email.getBody().getMessage() == null) {
          email.getBody().setMessage("");
        }
        if (email.getBody().getClosing() == null) {
          email.getBody().setClosing("");
        }
        if (email.getBody().getSignature() == null) {
          email.getBody().setSignature("");
        }
        if (email.getBody().getPostscript() == null) {
          email.getBody().setPostscript("");
        }
        if (email.getBody().getOverrideText() == null) {
          email.getBody().setOverrideText("");
        }
        // process conditional text
        email
            .getConditionalTexts()
            .forEach(
                (name, cTexts) -> {
                  for (ConditionalText cText : cTexts) {
                    if (cText.getCondition().test(mailCtx)) {
                      bodyText[0] =
                          bodyText[0].replace(
                              "?:" + cText.getName() + ":?", cText.getActionTrue().apply(mailCtx));
                      email.setSubject(
                          email
                              .getSubject()
                              .replace(
                                  "?:" + cText.getName() + ":?",
                                  cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setGreeting(
                              email
                                  .getBody()
                                  .getGreeting()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setMessage(
                              email
                                  .getBody()
                                  .getMessage()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setClosing(
                              email
                                  .getBody()
                                  .getClosing()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setSignature(
                              email
                                  .getBody()
                                  .getSignature()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setPostscript(
                              email
                                  .getBody()
                                  .getPostscript()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setOverrideText(
                              email
                                  .getBody()
                                  .getOverrideText()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                    } else {
                      if (cText.getActionFalse() != null) {
                        bodyText[0] =
                            bodyText[0].replace(
                                "?:" + cText.getName() + ":?",
                                cText.getActionFalse().apply(mailCtx));
                        email.setSubject(
                            email
                                .getSubject()
                                .replace(
                                    "?:" + cText.getName() + ":?",
                                    cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setGreeting(
                                email
                                    .getBody()
                                    .getGreeting()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setMessage(
                                email
                                    .getBody()
                                    .getMessage()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setClosing(
                                email
                                    .getBody()
                                    .getClosing()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setSignature(
                                email
                                    .getBody()
                                    .getSignature()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setPostscript(
                                email
                                    .getBody()
                                    .getPostscript()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setOverrideText(
                                email
                                    .getBody()
                                    .getOverrideText()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                      }
                    }
                  }
                });
        // replace placeholders with values from parameters for subject and body
        email
            .getParams()
            .forEach(
                (paramName, paramValue) -> {
                  bodyText[0] =
                      bodyText[0].replace(
                          "::" + paramName + "::",
                          (paramValue.apply(mailCtx) == null ? "" : paramValue.apply(mailCtx)));
                  ;
                  email.setSubject(
                      email
                          .getSubject()
                          .replace(
                              "::" + paramName + "::",
                              (paramValue.apply(mailCtx) == null
                                  ? ""
                                  : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setGreeting(
                          email
                              .getBody()
                              .getGreeting()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setMessage(
                          email
                              .getBody()
                              .getMessage()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setClosing(
                          email
                              .getBody()
                              .getClosing()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setSignature(
                          email
                              .getBody()
                              .getSignature()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setPostscript(
                          email
                              .getBody()
                              .getPostscript()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setOverrideText(
                          email
                              .getBody()
                              .getOverrideText()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                });
        String html;
        // Apply template. Not yet implemented

        // convert markdown to html
        html = renderer.render(parser.parse(bodyText[0]));
        // set subject and body text
        helper.setSubject(email.getSubject());
        helper.setText(html, true);

        recipients.forEach((type,list)->{
          list.forEach(r->{
            var reportBuilder = EmailInstancePOST.builder();
            reportBuilder.subject(email.getSubject());
            reportBuilder.body(html);
            reportBuilder.recipientType(type);
            reportBuilder.recipientPOST(RecipientPOST.builder()
                .email(r.getEmail())
                .company(r.getCompany())
                .firstname(r.getFirstname())
                .lastname(r.getLastname())
                .build());
            reports.add(reportBuilder.build());
          });
        });
        sender.send(message);
      } catch (MailException | MessagingException e) {
        if(e instanceof MailSendException){
          var errors = ((MailSendException) e).getMessageExceptions();
          for(Exception error:errors ){
            if(error instanceof SendFailedException sendFailed){
              log.error(sendFailed.getNextException().getMessage());
            }else{
              System.err.println("unknown send failed error");
              error.printStackTrace();
            }
          }
        }else{
          System.err.println("Unknown error");
          System.err.println(e.getMessage());
        }
      }finally {
        canProcess=true;
        reports.forEach(reportConsumer);
      }

  }

  public void send(Email email) {
    canProcess=false;
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (email.getToRecipients().isEmpty()) {
      log.warn("No recipients were defined. No email will be sent");
      return;
    }
    // send an email to each recipient individually
    for (EmailRecipient recipient : email.getToRecipients()) {
      EmailContext mailCtx = new EmailContext();
      mailCtx.setRecipient(recipient);
      MimeMessage message = sender.createMimeMessage();
      MimeMessageHelper helper = null;
      try {
        helper = new MimeMessageHelper(message, true);
        helper.setFrom("bot1@mail.marinos.com");
        helper.setTo(recipient.getEmail());
        // process attachments
        List<EmailAttachment> preparedAttachments = new ArrayList<>();
        for (EmailAttachment attachment : email.getAttachments()) {
          preparedAttachments.addAll(preparedAttachment(attachment));
        }
        // add names of attachments to EmailContext
        mailCtx.setAttachmentFilenames(
            preparedAttachments.stream()
                .map(EmailAttachment::getName)
                .collect(Collectors.toList()));
        // add attachments to email
        for (EmailAttachment preparedAttachment : preparedAttachments) {
          if (preparedAttachment.getType() != null
              && !preparedAttachment.getType().equals(AttachmentFileType.UNKNOWN)) {
            helper.addAttachment(
                preparedAttachment.getName(),
                new ByteArrayDataSource(
                    preparedAttachment.getContent(), mimeMap.get(preparedAttachment.getType())));
          } else {
            helper.addAttachment(
                preparedAttachment.getName(),
                new ByteArrayResource(preparedAttachment.getContent()));
          }
        }
        final String[] bodyText = {email.getBody().toEmailText()};
        // set empty string when value is null
        if (email.getBody().getGreeting() == null) {
          email.getBody().setGreeting("");
        }
        if (email.getBody().getMessage() == null) {
          email.getBody().setMessage("");
        }
        if (email.getBody().getClosing() == null) {
          email.getBody().setClosing("");
        }
        if (email.getBody().getSignature() == null) {
          email.getBody().setSignature("");
        }
        if (email.getBody().getPostscript() == null) {
          email.getBody().setPostscript("");
        }
        if (email.getBody().getOverrideText() == null) {
          email.getBody().setOverrideText("");
        }
        // process conditional text
        email
            .getConditionalTexts()
            .forEach(
                (name, cTexts) -> {
                  for (ConditionalText cText : cTexts) {
                    if (cText.getCondition().test(mailCtx)) {
                      bodyText[0] =
                          bodyText[0].replace(
                              "?:" + cText.getName() + ":?", cText.getActionTrue().apply(mailCtx));
                      email.setSubject(
                          email
                              .getSubject()
                              .replace(
                                  "?:" + cText.getName() + ":?",
                                  cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setGreeting(
                              email
                                  .getBody()
                                  .getGreeting()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setMessage(
                              email
                                  .getBody()
                                  .getMessage()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setClosing(
                              email
                                  .getBody()
                                  .getClosing()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setSignature(
                              email
                                  .getBody()
                                  .getSignature()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setPostscript(
                              email
                                  .getBody()
                                  .getPostscript()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                      email
                          .getBody()
                          .setOverrideText(
                              email
                                  .getBody()
                                  .getOverrideText()
                                  .replace(
                                      "?:" + cText.getName() + ":?",
                                      cText.getActionTrue().apply(mailCtx)));
                    } else {
                      if (cText.getActionFalse() != null) {
                        bodyText[0] =
                            bodyText[0].replace(
                                "?:" + cText.getName() + ":?",
                                cText.getActionFalse().apply(mailCtx));
                        email.setSubject(
                            email
                                .getSubject()
                                .replace(
                                    "?:" + cText.getName() + ":?",
                                    cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setGreeting(
                                email
                                    .getBody()
                                    .getGreeting()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setMessage(
                                email
                                    .getBody()
                                    .getMessage()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setClosing(
                                email
                                    .getBody()
                                    .getClosing()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setSignature(
                                email
                                    .getBody()
                                    .getSignature()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setPostscript(
                                email
                                    .getBody()
                                    .getPostscript()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                        email
                            .getBody()
                            .setOverrideText(
                                email
                                    .getBody()
                                    .getOverrideText()
                                    .replace(
                                        "?:" + cText.getName() + ":?",
                                        cText.getActionFalse().apply(mailCtx)));
                      }
                    }
                  }
                });
        // replace placeholders with values from parameters for subject and body
        email
            .getParams()
            .forEach(
                (paramName, paramValue) -> {
                  bodyText[0] =
                      bodyText[0].replace(
                          "::" + paramName + "::",
                          (paramValue.apply(mailCtx) == null ? "" : paramValue.apply(mailCtx)));
                  ;
                  email.setSubject(
                      email
                          .getSubject()
                          .replace(
                              "::" + paramName + "::",
                              (paramValue.apply(mailCtx) == null
                                  ? ""
                                  : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setGreeting(
                          email
                              .getBody()
                              .getGreeting()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setMessage(
                          email
                              .getBody()
                              .getMessage()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setClosing(
                          email
                              .getBody()
                              .getClosing()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setSignature(
                          email
                              .getBody()
                              .getSignature()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setPostscript(
                          email
                              .getBody()
                              .getPostscript()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                  email
                      .getBody()
                      .setOverrideText(
                          email
                              .getBody()
                              .getOverrideText()
                              .replace(
                                  "::" + paramName + "::",
                                  (paramValue.apply(mailCtx) == null
                                      ? ""
                                      : paramValue.apply(mailCtx))));
                });
        String html;
        // Apply template. Not yet implemented

        // convert markdown to html
        html = renderer.render(parser.parse(bodyText[0]));
        // set subject and body text
        helper.setSubject(email.getSubject());
        helper.setText(html, true);
        sender.send(message);
      } catch (MailException | MessagingException e) {
        if(e instanceof MailSendException){
          var errors = ((MailSendException) e).getMessageExceptions();
          for(Exception error:errors ){
            if(error instanceof SendFailedException sendFailed){
              log.error(sendFailed.getNextException().getMessage());
            }else{
              System.err.println("unknown send failed error");
            }
          }
        }else{
          System.err.println("Unknown error");
          System.err.println(e.getMessage());
        }
      }finally {
        canProcess=true;
      }
    }
  }
  public void processAttachments(String emailRequestId, List<EmailAttachment> attachmentList) {
    attachmentList.stream().map(this::preparedAttachment).flatMap(Collection::stream)
        .map(attachment -> {
          return AttachedFilePOST.builder()
              .emailRequestId(emailRequestId)
              .content(attachment.getContent())
              .filename(attachment.getName())
              .fileType(attachment.getType().name())
              .build();
        })
        .forEach(backendClient::reportAttachmentProgress);
    backendClient.completeAttachments(emailRequestId);
  }
  private List<EmailAttachment> preparedAttachment(EmailAttachment attachment){
    if(attachment.getClass().equals(TablesAttachment.class)){
      return prepareTablesAttachment((TablesAttachment)attachment);
    }
    throw new IllegalStateException("Unexpected value: "+attachment.getClass());
  }
  private List<EmailAttachment> prepareTablesAttachment(TablesAttachment tablesAttachment){
    return switch(tablesAttachment.getType()){
      case EXCEL,UNKNOWN -> List.of(createExcelFile(tablesAttachment));
      case CSV -> createCSVsFromTables(tablesAttachment);
    };
  }
  private List<EmailAttachment>createCSVsFromTables(TablesAttachment tablesAttachment){
    throw new RuntimeException("CSV processing not yet implemented.");
  }
  private EmailAttachment createExcelFile(TablesAttachment tablesAttachment){
    Workbook wb = new XSSFWorkbook();
    for(Table sheet:tablesAttachment.getSheets()){
      tableService.addToExcel(sheet,wb);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try{
      wb.write(out);
      wb.close();
      out.close();
    }catch(IOException e){
      throw new RuntimeException(e);
    }
    tablesAttachment.setContent(out.toByteArray());
    return tablesAttachment;
  }


}
