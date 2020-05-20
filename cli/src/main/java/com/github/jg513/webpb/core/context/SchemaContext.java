package com.github.jg513.webpb.core.context;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class SchemaContext {

    private static final String OPTIONS_FILE = "WebpbOptions";

    private Map<ProtoFile, FileContext> fileContexts = new HashMap<>();

    private Schema schema;

    private FileContext extendContext;

    private QualifiedNames qualifiedNames = new QualifiedNames();

    private JavaParser javaParser = new JavaParser();

    public SchemaContext(Schema schema) {
        this.schema = schema;
        schema
            .getProtoFiles().stream()
            .filter(f -> f.name().equals(OPTIONS_FILE))
            .findFirst()
            .ifPresent(protoFile -> {
                extendContext = new FileContext(SchemaContext.this, protoFile);
                fileContexts.put(protoFile, extendContext);
            });
        schema.getProtoFiles().stream()
            .filter(f -> !f.name().equals(OPTIONS_FILE))
            .forEach(protoFile ->
                fileContexts.put(protoFile, new FileContext(SchemaContext.this, protoFile))
            );
    }

    public void putQualifiedNames(Map<String, Name> names) {
        this.qualifiedNames.putAll(names);
    }

    public Map<AnnotationExpr, Name> parseAnnotations(List<String> annotations) {
        Map<AnnotationExpr, Name> annotationMap = new LinkedHashMap<>();
        if (annotations != null && !annotations.isEmpty()) {
            for (String annotation : annotations) {
                javaParser
                    .parseAnnotation(annotation)
                    .ifSuccessful(expr -> qualifiedNames
                        .get(expr.getName())
                        .ifPresent(name -> {
                            expr.getName().setQualifier(null);
                            annotationMap.put(expr, name);
                        })
                    );
            }
        }
        return annotationMap;
    }

    public Optional<FileContext> fileContext(ProtoFile file) {
        return Optional.ofNullable(fileContexts.get(file));
    }

    public List<Name> parseImports(Node node) {
        List<Name> imports = new LinkedList<>();
        parseImports(node, imports);
        return imports;
    }

    private void parseImports(Node node, List<Name> imports) {
        if (node instanceof FieldAccessExpr) {
            qualifiedNames
                .get(new Name(null, ((FieldAccessExpr) node).getScope().toString()))
                .ifPresent(imports::add);
        } else if (node instanceof AnnotationExpr) {
            qualifiedNames
                .get(((AnnotationExpr) node).getName())
                .ifPresent(imports::add);
        } else if (node instanceof ClassOrInterfaceType) {
            ((ClassOrInterfaceType) node).getTypeArguments().ifPresent(list -> {
                for (Type t : list) {
                    if (t instanceof ClassOrInterfaceType) {
                        parseParameterizedImports((ClassOrInterfaceType) t, imports);
                    }
                }
            });
            parseParameterizedImports((ClassOrInterfaceType) node, imports);
        } else {
            qualifiedNames.get(node.toString()).ifPresent(imports::add);
        }
        for (Node childNode : node.getChildNodes()) {
            parseImports(childNode, imports);
        }
    }

    private void parseParameterizedImports(ClassOrInterfaceType type, List<Name> imports) {
        qualifiedNames.get(type.getNameAsString()).ifPresent(imports::add);
        type.getTypeArguments().ifPresent(list -> {
            for (Type t : list) {
                if (t instanceof ClassOrInterfaceType) {
                    parseParameterizedImports((ClassOrInterfaceType) t, imports);
                }
            }
        });
    }
}
