package com.kmarinos.hermes.emailservice.dto;


import com.kmarinos.hermes.emailservice.model.AttachedFile;
import com.kmarinos.hermes.serviceDto.AttachedFileGET;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class AttachedFileDTO {
  public static AttachedFileGET GET(AttachedFile attachedFile, Function<String,byte[]> contentSupplier){
    return AttachedFileGET.builder()
        .id(attachedFile.getId())
        .size(attachedFile.getSize())
        .filetype(attachedFile.getFiletype())
        .filename(attachedFile.getFilename())
        .emailRequestId(attachedFile.getEmailRequest().getId())
        .content(contentSupplier.apply(attachedFile.getPath()))
        .build();
  }
  public static List<AttachedFileGET> GET(List<AttachedFile> attachedFiles,Function<String,byte[]> contentSupplier){
    if(attachedFiles==null){
      return new ArrayList<>();
    }
    return attachedFiles.stream().map(a->AttachedFileDTO.GET(a,contentSupplier)).toList();
  }

}
