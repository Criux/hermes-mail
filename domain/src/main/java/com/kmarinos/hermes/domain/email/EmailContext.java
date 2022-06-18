package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailContext implements Serializable {
private EMailRecipient recipient;
private List<String> attachmentFilenames;
}
