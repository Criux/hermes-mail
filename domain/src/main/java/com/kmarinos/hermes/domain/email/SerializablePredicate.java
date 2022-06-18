package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.util.function.Predicate;

public interface SerializablePredicate extends Predicate<EmailContext>, Serializable {

}
