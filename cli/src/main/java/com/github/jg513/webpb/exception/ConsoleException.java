package com.github.jg513.webpb.exception;

public class ConsoleException extends RuntimeException {
    public ConsoleException(String format, Object... args) {
        super(String.format(format, args));
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
