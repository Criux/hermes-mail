package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Email implements Serializable {

  List<EmailRecipient> toRecipients;
  List<EmailRecipient> ccRecipients;
  List<EmailRecipient> bccRecipients;

  Map<String, SerializableFunction> params = new HashMap<>();
  Map<String, byte[]> classesToLoad = new HashMap<>();
  Map<String, List<ConditionalText>> conditionalTexts = new HashMap<>();

  List<EmailAttachment> attachments = new ArrayList<>();
  boolean allowDefaults = true;
  boolean splitRecipients = true;
  Body body;
  String subject;
  String template;

  public Email() {}

  public static EmailCompositionWorkflow compose(){return new EmailCompositionWorkflow();}
  public static EmailCompositionWorkflow compose(String template){
    return new EmailCompositionWorkflow().template(template);
  }
  public static EmailCompositionWorkflow fromTemplate(Email email){
    return new EmailCompositionWorkflow(email);
  }
}
