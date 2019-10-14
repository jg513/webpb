package io.webpb.cli.log;

public interface Logger {
    void info(String format, Object... args);

    void error(String format, Object... args);
}
