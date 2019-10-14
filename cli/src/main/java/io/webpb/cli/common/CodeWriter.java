package io.webpb.cli.common;

import java.util.concurrent.Callable;

public abstract class CodeWriter implements Callable<Void> {
    protected CodeWriterContext context;

    private CodeWriter() {
    }

    public CodeWriter(CodeWriterContext context) {
        this.context = context;
    }
}
