package com.github.jg513.webpb.core.context;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.jg513.webpb.core.options.FileOptions;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Type;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
public class FileContext {

    private static final String TRUE = "true";

    private final Map<Type, TypeContext> typeContexts = new HashMap<>();

    private final SchemaContext context;

    private final ProtoFile protoFile;

    private final Map<AnnotationExpr, Name> messageAnnotations = new LinkedHashMap<>();

    private boolean javaGetter = true;

    private boolean javaSetter = true;

    private boolean tsLong = true;

    private boolean tsJson = true;

    private boolean tsStream = true;

    private boolean tsLongAsString = true;

    private boolean javaToStringMethod = true;

    public FileContext(SchemaContext context, ProtoFile protoFile) {
        this.context = context;
        this.protoFile = protoFile;
        context.putQualifiedNames(parseImports(context.getJavaParser(), protoFile));
        Optional
            .ofNullable(context.getExtendContext())
            .ifPresent(extend -> {
                extend.messageAnnotations.keySet().forEach(expr ->
                    this.messageAnnotations.put(expr.clone(), expr.getName())
                );
                this.javaGetter = extend.javaGetter;
                this.javaSetter = extend.javaSetter;
                this.tsLong = extend.tsLong;
                this.tsJson = extend.tsJson;
                this.tsStream = extend.tsStream;
                this.tsLongAsString = extend.tsLongAsString;
                this.javaToStringMethod = extend.javaToStringMethod;
            });
        ParserUtils
            .getList(protoFile.getOptions(), FileOptions.JAVA_COMMON_ANNO)
            .ifPresent(v -> this.messageAnnotations.putAll(context.parseAnnotations(v)));
        ParserUtils
            .get(protoFile.getOptions(), FileOptions.JAVA_GETTER)
            .ifPresent(v -> this.javaGetter = TRUE.equals(v));
        ParserUtils
            .get(protoFile.getOptions(), FileOptions.JAVA_SETTER)
            .ifPresent(v -> this.javaSetter = TRUE.equals(v));
        ParserUtils
            .get(protoFile.getOptions(), FileOptions.TS_LONG)
            .ifPresent(v -> this.tsLong = TRUE.equals(v));
        ParserUtils
            .get(protoFile.getOptions(), FileOptions.TS_JSON)
            .ifPresent(v -> this.tsJson = TRUE.equals(v));
        ParserUtils
            .get(protoFile.getOptions(), FileOptions.TS_STREAM)
            .ifPresent(v -> this.tsStream = TRUE.equals(v));
        ParserUtils
            .get(protoFile.getOptions(), FileOptions.TS_LONG_AS_STRING)
            .ifPresent(v -> this.tsLongAsString = TRUE.equals(v));
        ParserUtils
            .get(protoFile.getOptions(), FileOptions.JAVA_TO_STRING_METHOD)
            .ifPresent(v -> this.javaToStringMethod = TRUE.equals(v));
        protoFile.getTypes().forEach(this::genTypeContexts);
    }

    private void genTypeContexts(Type type) {
        typeContexts.put(type, new TypeContext(FileContext.this, type));
        type.getNestedTypes().forEach(this::genTypeContexts);
    }

    private Map<String, Name> parseImports(JavaParser javaParser, ProtoFile protoFile) {
        Map<String, Name> importMap = new LinkedHashMap<>();
        Arrays
            .asList(List.class, Map.class)
            .forEach(c -> javaParser
                .parseName(c.getName())
                .ifSuccessful(name -> importMap.put(name.getIdentifier(), name))
            );
        for (Type type : protoFile.getTypes()) {
            String identifier = Objects.requireNonNull(type.getType()).getSimpleName();
            if (StringUtils.isEmpty(protoFile.javaPackage())) {
                throw new IllegalArgumentException("java_package option is required in " + protoFile.name());
            }
            importMap.put(identifier, new Name(new Name(null, protoFile.javaPackage()), identifier));
        }
        ParserUtils
            .getList(protoFile.getOptions(), FileOptions.JAVA_IMPORT)
            .ifPresent(strings -> strings.forEach(v ->
                javaParser.parseName(v).ifSuccessful(name ->
                    importMap.put(name.getIdentifier(), name)
                )));
        return importMap;
    }

    public Optional<TypeContext> typeContext(Type type) {
        return Optional.ofNullable(typeContexts.get(type));
    }
}
