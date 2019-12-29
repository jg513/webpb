package com.github.jg513.webpb.core.context;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.jg513.webpb.core.options.FileOptions;
import com.github.jg513.webpb.core.options.MessageOptions;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.Type;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class TypeContext {
    private Type type;

    private Map<String, FieldContext> fieldContexts = new HashMap<>();

    private SchemaContext context;

    private FileContext fileContext;

    private Map<AnnotationExpr, Name> annotations = new LinkedHashMap<>();

    private String path;

    private String method;

    private boolean getter;

    private boolean setter;

    public TypeContext(FileContext fileContext, Type type) {
        this.type = type;
        this.context = fileContext.getContext();
        this.fileContext = fileContext;
        this.getter = fileContext.isGetter();
        this.setter = fileContext.isSetter();
        fileContext.getMessageAnnotations().keySet().forEach(expr ->
            this.annotations.put(expr.clone(), expr.getName())
        );
        ParserUtils
            .getList(type.getOptions(), MessageOptions.JAVA_ANNO)
            .ifPresent(v -> this.annotations.putAll(this.context.parseAnnotations(v)));
        ParserUtils
            .get(type.getOptions(), MessageOptions.PATH)
            .ifPresent(v -> this.path = v);
        ParserUtils
            .get(type.getOptions(), MessageOptions.METHOD)
            .ifPresent(v -> this.method = v);
        if (type instanceof MessageType) {
            ((MessageType) type).getFieldsAndOneOfFields().forEach(field ->
                fieldContexts.put(field.getName(), new FieldContext(TypeContext.this, field))
            );
        }
        ParserUtils
            .get(type.getOptions(), FileOptions.JAVA_GETTER)
            .ifPresent(v -> this.getter = "true".equals(v));
        ParserUtils
            .get(type.getOptions(), FileOptions.JAVA_SETTER)
            .ifPresent(v -> this.setter = "true".equals(v));
    }

    public Optional<FieldContext> fieldContext(String field) {
        return Optional.ofNullable(fieldContexts.get(field));
    }
}
