package com.kmarinos.hermes.domain.email;

import java.io.Serializable;
import java.util.function.Function;

public interface SerializableFunction extends Function<EmailContext,String>, Serializable {

}
