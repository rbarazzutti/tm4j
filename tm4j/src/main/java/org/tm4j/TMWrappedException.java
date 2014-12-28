package org.tm4j;

public class TMWrappedException extends TMException {
    final private Exception wrappedException;

    public TMWrappedException(Exception e) {
        super(e);
        wrappedException = e;
    }

    public Exception getWrappedException() {
        return wrappedException;
    }
}
