package com.kmarinos.hermes.emailclient;

import com.kmarinos.hermes.domain.EmailUtils;
import com.kmarinos.hermes.domain.IdentityProvider;
import com.kmarinos.hermes.domain.SerializableLambdaUtils;
import com.kmarinos.hermes.domain.email.Body;
import com.kmarinos.hermes.domain.email.ConditionalText;
import com.kmarinos.hermes.domain.email.Email;
import com.kmarinos.hermes.domain.email.EmailAttachment;
import com.kmarinos.hermes.domain.email.EmailRecipient;
import com.kmarinos.hermes.domain.email.Excel;
import com.kmarinos.hermes.domain.email.SerializableClass;
import com.kmarinos.hermes.domain.email.SerializableFunction;
import com.kmarinos.hermes.domain.email.SerializablePredicate;
import com.kmarinos.hermes.domain.email.Sheet;
import com.kmarinos.hermes.domain.email.Table;
import com.kmarinos.hermes.domain.email.TableHeader;
import com.kmarinos.hermes.domain.email.TableRow;
import com.kmarinos.hermes.domain.email.TablesAttachment;
import com.kmarinos.hermes.emailclient.sql.SQLClient;
import com.kmarinos.hermes.emailclient.sql.SQLClientFactory;
import com.kmarinos.hermes.emailclient.sql.SelectResult;
import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCompositionWorkflow {
  List<String> toEmails = new ArrayList<>();
  Map<String, SerializableFunction> params = new HashMap<>();
  Map<String,List<ConditionalText>> conditionalTexts = new HashMap<>();
  List<EmailAttachment> attachments = new ArrayList<>();
  boolean allowDefaults = true;
  boolean splitRecipients = true;
  Body body;
  String subject;
  String template;
  Map<String,byte[]> classesToLoad = new HashMap<>();

  IdentityProvider identityProvider;

  public EmailCompositionWorkflow(){
    identityProvider = new StandardIdentityProvider();
  }

  public EmailCompositionWorkflow(Email template){
    this();
    if(template.getToRecipients()!=null){
      this.setToEmails(template.getToRecipients().stream().map(EmailRecipient::getEmail).toList());
    }
    this.setParams(template.getParams());
    this.setClassesToLoad(template.getClassesToLoad());
    this.setConditionalTexts(template.getConditionalTexts());
    this.setSubject(template.getSubject());
    this.setTemplate(template.getTemplate());
    this.setBody(template.getBody());
    this.setAllowDefaults(template.isAllowDefaults());
    this.setAttachments(template.getAttachments());

  }
  public <T> T copyNonNullProperties(T target,T in){
    if(in == null || target == null || target.getClass() != in.getClass()){
      return null;
    }
    try{
      Arrays.stream(Introspector.getBeanInfo(in.getClass()).getPropertyDescriptors()).map(
          FeatureDescriptor::getName).forEach(System.out::println);
    }catch(IntrospectionException e){
      throw new RuntimeException(e);
    }
    for(final Field property: target.getClass().getDeclaredFields()){
      Object providedObject = null;
      System.out.println("Trying for "+property.getName());
      try{
        providedObject = new PropertyDescriptor(property.getName(),in.getClass()).getReadMethod().invoke(in);
        System.out.println("Object is "+providedObject);
        if(providedObject !=null && !(providedObject instanceof Collection<?>)){
          new PropertyDescriptor(property.getName(),target.getClass()).getWriteMethod().invoke(target,providedObject);
        }
      } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
        System.err.println(e.getMessage());
      }
    }
    return target;
  }
  public Email get(){
    Email email = new Email();
    email.setToRecipients(this.toEmails.stream().map(identityProvider::getRecipientInfo).toList());
    email.setParams(params);
    email.setClassesToLoad(classesToLoad);
    email.setConditionalTexts(conditionalTexts);
    email.setSubject(subject);
    email.setTemplate(template);
    email.setBody(body);
    email.setAllowDefaults(allowDefaults);
    email.setAttachments(attachments);

    return email;
  }
  public Email send(){
    Email email = get();
    if(this.getToEmails().isEmpty()){
      System.err.println("[Warning] No recipients were defined. No email will be sent.");
      return email;
    }
    System.out.println("Trying to send email...");
    MailService.getInstance().send(classesToLoad,email);
    return email;
  }
  public EmailCompositionWorkflow template(String template){
    this.setTemplate(template);
    return this;
  }
  public EmailCompositionWorkflow to(String email){
    if(email.contains(",")){
      to(email.split(","));
    }else{
      if(EmailUtils.isValid(email)){
        if(!toEmails.contains(email)){
          toEmails.add(email);
        }else{
          System.err.println("[Warning] \""+email+"\" has already been added to the list of recipients.");
        }
      }else{
        System.err.println("[Warning] \""+email+"\" is not a valid address.");
      }
    }
    return this;
  }
  public EmailCompositionWorkflow to(String... emails){
    for(String email:emails){
      this.to(email);
    }
    return this;
  }
  public EmailCompositionWorkflow attach(EmailAttachment attachment){
    this.getAttachments().add(attachment);
    return this;
  }
  //belongs to client
  public EmailCompositionWorkflow attach(Excel excel){
    TablesAttachment ta = TablesAttachment.compose();
    for(Sheet sheet: excel.getSheets()){
      ta.addTable(getTableFromInputSheet(sheet));
    }
    ta.name(excel.getName()).type("EXCEL");
    if(excel.isAppendDateTime()){
      ta.appendDateTime();
    }
    return attach(ta);
  }
  public EmailCompositionWorkflow body(Body body){
    if(this.getBody() == null){
      this.setBody(body);
    }else{
      this.setBody(copyNonNullProperties(this.getBody(),body));
    }
    return this;
  }
  public EmailCompositionWorkflow subject(String subject){
    this.setSubject(subject);
    return this;
  }
  public EmailCompositionWorkflow disableDefaults(){
    this.setAllowDefaults(false);
    return this;
  }
  public EmailCompositionWorkflow param(String name,String value){
    return this.param(name,ctx->value);
  }
  public EmailCompositionWorkflow param(String name, Function mailCtx){
    SerializableFunction f = ctx -> mailCtx.apply(ctx).toString();
    param(name,f);
    return this;
  }
  public EmailCompositionWorkflow param(String name,SerializableFunction mailCtx){
    addToClassesToLoad(mailCtx);
    params.put(name,mailCtx);
    return this;
  }
  public EmailCompositionWorkflow conditionalParam(String name, SerializablePredicate condition, String valueTrue){
    return this.conditionalParam(name,condition,ctx->valueTrue,null);
  }

  public EmailCompositionWorkflow conditionalParam(String name, SerializablePredicate condition,String valueTrue,String valueFalse){
    return this.conditionalParam(name,condition,ctx->valueTrue,ctx->valueFalse);
  }
  public EmailCompositionWorkflow conditionalParam(String name,SerializablePredicate condition, SerializableFunction actionTrue){
    return this.conditionalParam(name,condition,actionTrue,null);
  }
  public EmailCompositionWorkflow conditionalParam(String name,SerializablePredicate condition, SerializableFunction actionTrue,SerializableFunction actionFalse){
    List<ConditionalText> registeredConditions = conditionalTexts.getOrDefault(name,new ArrayList<>());
    addToClassesToLoad(condition);
    addToClassesToLoad(actionTrue);
    addToClassesToLoad(actionFalse);
    registeredConditions.add(ConditionalText.builder()
            .name(name)
            .condition(condition)
            .actionTrue(actionTrue)
            .actionFalse(actionFalse)
        .build());
    conditionalTexts.putIfAbsent(name,registeredConditions);
    return this;
  }
  private void addToClassesToLoad(SerializablePredicate lambda){
    if(lambda == null){
      return;
    }
    SerializableClass classToLoad = null;
    try{
      classToLoad = new SerializableClass(Class.forName(SerializableLambdaUtils.getEnclosingClassName(lambda)));
    }catch(ClassNotFoundException e){
      throw new RuntimeException(e);
    }
    if(classToLoad!=null){
      classesToLoad.put(classToLoad.getName(),classToLoad.getData());
    }
  }
  private void addToClassesToLoad(SerializableFunction lambda){
    if(lambda == null){
      return;
    }
    SerializableClass classToLoad = null;
    try{
      classToLoad = new SerializableClass(Class.forName(SerializableLambdaUtils.getEnclosingClassName(lambda)));
    }catch(ClassNotFoundException e){
      throw new RuntimeException(e);
    }
    if(classToLoad!=null){
      classesToLoad.put(classToLoad.getName(),classToLoad.getData());
    }
  }
  private Table getTableFromInputSheet(Sheet sheet){
    Table table = new Table();
    if (sheet.getName() != null && !sheet.getName().isEmpty()){
      table.setName(sheet.getName());
    }
    table.setHeaders(new ArrayList<>());
    table.setRows(new ArrayList<>());
    try{
      SQLClient sqlClient = SQLClientFactory.connectTo(sheet.getConnection());
      List<SelectResult> sqlRows = sqlClient.select(sheet.getSql()).andGet();
      for(int i = 0;i<sqlRows.size();i++){
        SelectResult ctx = sqlRows.get(i);
        //get headers form first line
        if(i==0){
          for(String key:ctx.getAll().keySet()){
            TableHeader header = TableHeader.builder()
                .name(key)
                .columnType(ctx.typeOf(key).getName())
                .build();
            table.getHeaders().add(header);
          }
        }
        TableRow tableRow = new TableRow(new ArrayList<>(ctx.getAll().values()));
        table.getRows().add(tableRow);
      }
    }catch(Exception e){
     e.printStackTrace();
    }
    return table;
  }
}
