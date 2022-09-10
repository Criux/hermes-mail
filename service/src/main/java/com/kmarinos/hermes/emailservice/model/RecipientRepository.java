package com.kmarinos.hermes.emailservice.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient,String> {

  Optional<Recipient> findByEmail(String email);

}
