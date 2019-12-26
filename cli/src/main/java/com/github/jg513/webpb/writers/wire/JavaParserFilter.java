package com.github.jg513.webpb.writers.wire;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import static com.github.javaparser.ast.Modifier.*;

public class JavaParserFilter {
    private JavaParser javaParser = new JavaParser();

    public CompilationUnit filter(String content) {
        CompilationUnit unit = javaParser.parse(content)
            .getResult().orElseThrow(() -> new RuntimeException("Error"));
        unit = makeFieldsPrivate(unit);
        return unit;
    }

    private CompilationUnit makeFieldsPrivate(CompilationUnit unit) {
        unit.getTypes().forEach(type -> {
            type.getFields().stream()
                .filter(field -> field.isAnnotationPresent("WireField"))
                .forEach(field -> {
                    field.setModifiers(Keyword.PRIVATE, Keyword.FINAL);
                });
        });
        return unit;
    }
}
