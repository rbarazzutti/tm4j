package org.tm4j;

public class TMException extends RuntimeException {
    public TMException(String s) {
        super(s);
    }

    protected TMException(Exception e) {
        super(e);
    }
}
