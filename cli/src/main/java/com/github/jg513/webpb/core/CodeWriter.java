package com.github.jg513.webpb.core;

import java.util.concurrent.Callable;

public abstract class CodeWriter implements Callable<Void> {
    protected CodeWriterContext context;

    private CodeWriter() {
    }

    public CodeWriter(CodeWriterContext context) {
        this.context = context;
    }
}
