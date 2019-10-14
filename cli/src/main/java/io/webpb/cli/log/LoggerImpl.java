package io.webpb.cli.log;

import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;

@RequiredArgsConstructor(staticName = "of")
public class LoggerImpl implements Logger {
    private final PrintWriter out;

    private final PrintWriter err;

    private final boolean quiet;

    public void info(String format, Object... args) {
        this.out.println(String.format(format, args));
    }

    public void error(String format, Object... args) {
        this.err.println(String.format(format, args));
    }
}
