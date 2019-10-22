package com.github.jg513.webpb.exception;

public class ConsoleErrorException extends RuntimeException {
    public ConsoleErrorException(String format, Object... args) {
        super(String.format(format, args));
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
