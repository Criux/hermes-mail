package com.kmarinos.hermes.emailservice.exceptionHandling.exceptions;

public class OperationNotAllowedException extends RuntimeException{
    public OperationNotAllowedException(String message){
        super(message);
    }
}
