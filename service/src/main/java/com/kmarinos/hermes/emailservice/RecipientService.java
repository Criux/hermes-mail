package com.kmarinos.hermes.emailservice;

import com.kmarinos.hermes.emailservice.model.Recipient;
import com.kmarinos.hermes.emailservice.model.RecipientRepository;
import com.kmarinos.hermes.serviceDto.RecipientPOST;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class RecipientService {

  private final RecipientRepository recipientRepository;

  public Recipient getByRequestOrCreate(RecipientPOST request) {
    return recipientRepository.findByEmail(request.getEmail())
        .orElseGet(() -> recipientRepository.save(Recipient.builder()
            .company(request.getCompany())
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .build()));
  }

}
