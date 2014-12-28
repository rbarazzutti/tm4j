package org.tm4j;

public class TMException extends RuntimeException {
    public TMException(String s){
        super(s);
    }

    public TMException(Exception e){super(e);}
}
