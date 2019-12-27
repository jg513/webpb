package com.github.jg513.webpb.core.context;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.jg513.webpb.core.options.FieldOptions;
import com.squareup.wire.schema.Field;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class FieldContext {
    private SchemaContext context;

    private TypeContext typeContext;

    private Map<AnnotationExpr, Name> annotations = new LinkedHashMap<>();

    private boolean omitted = false;

    public FieldContext(TypeContext typeContext, Field field) {
        this.context = typeContext.getContext();
        this.typeContext = typeContext;
        ParserUtils
            .getList(field.getOptions(), FieldOptions.JAVA_ANNO)
            .ifPresent(v -> this.annotations.putAll(this.context.parseAnnotations(v)));
        ParserUtils
            .get(field.getOptions(), FieldOptions.OMITTED)
            .ifPresent(v -> this.omitted = "true".equals(v));
    }
}
