package com.kmarinos.hermes.emailservice.exceptionHandling;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class ApiErrorClassNameResolver extends TypeIdResolverBase {

    @Override
    public String idFromValue(Object value) {
        return "error";
    }
    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}