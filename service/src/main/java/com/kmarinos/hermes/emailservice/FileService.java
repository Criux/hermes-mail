package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.model.AttachedFile;
import com.kmarinos.hermes.emailservice.model.AttachedFileRepository;
import com.kmarinos.hermes.emailservice.model.EmailRequest;
import com.kmarinos.hermes.serviceDto.AttachedFilePOST;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

  @Value("${hermes.email.file.archivePath:}")
  String rootPath;
  private final AttachedFileRepository attachedFileRepository;

  public AttachedFile createAttachedFile(AttachedFilePOST emailAttachmentPOST,
      Function<String, EmailRequest> requestProvider) {
    var request = requestProvider.apply(emailAttachmentPOST.getEmailRequestId());
    var file = attachedFileRepository.save(AttachedFile.builder()
        .emailRequest(request)
        .filename(emailAttachmentPOST.getFilename())
        .filetype(emailAttachmentPOST.getFileType())
        .build());
    this.saveFileToDisk(file, emailAttachmentPOST.getContent());
    return attachedFileRepository.save(file);
  }

  private void saveFileToDisk(AttachedFile file, byte[] content) {
    file.setPath(this.getPathFromId(file).getAbsolutePath());
    try (var out = new FileOutputStream(file.getPath())) {
      out.write(content);
      file.setSize(content.length);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("all")
  private File getPathFromId(AttachedFile file) {
    assert file.getId() != null;
    var grandparent = file.getId().substring(0, 2);
    var parent = file.getId().substring(2, 4);
    var onDisk = new File(rootPath + "/" + grandparent + "/" + parent);
    onDisk.mkdirs();
    return new File(onDisk.getAbsolutePath()+"/"+getRandomName());
  }
  private String getRandomName(){
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 20;
    Random random = new Random();

    return random.ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();

  }

  public List<AttachedFile> fetchAttachments(EmailRequest emailRequest) {
    return attachedFileRepository.findAttachedFileByEmailRequest(emailRequest);
  }
  public byte[] getFileContent(String path){
    try {
      return Files.readAllBytes(Paths.get(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new byte[0];
  }
}
