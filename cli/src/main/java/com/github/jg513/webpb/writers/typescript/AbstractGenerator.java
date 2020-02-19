package com.github.jg513.webpb.writers.typescript;

import com.github.jg513.webpb.core.Handler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractGenerator {

    protected final TypescriptGenerator generator;

    protected final StringBuilder builder;

    protected void level(Handler handler) {
        generator.level(handler);
    }

    protected StringBuilder indent() {
        return generator.indent();
    }

    protected void closeBracket() {
        generator.closeBracket();
    }
}
