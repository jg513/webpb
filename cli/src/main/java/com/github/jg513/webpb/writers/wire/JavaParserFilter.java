package com.github.jg513.webpb.writers.wire;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.jg513.webpb.core.context.TypeContext;
import com.github.jg513.webpb.options.FieldOptions;
import com.github.jg513.webpb.options.FileOptions;
import com.github.jg513.webpb.options.MessageOptions;
import com.squareup.wire.schema.EnumType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.javaparser.ast.Modifier.Keyword;

public class JavaParserFilter {
    private static Map<String, String> methodsMapping = new HashMap<String, String>() {{
        put("java_import", "javaImports");
        put("java_message_anno", "javaMessageAnnotations");
        put("java_setter", "javaSetter");
        put("java_getter", "javaGetter");


        put("omitted", "omitted");
        put("method", "method");
        put("path", "path");
        put("tag", "tags");
        put("java_anno", "javaAnnotations");
    }};

    private JavaParser javaParser = new JavaParser();

    public CompilationUnit filter(TypeContext typeContext, String content) {
        CompilationUnit unit = javaParser.parse(content)
            .getResult().orElseThrow(() -> new RuntimeException("Error"));
        if (typeContext.getType() instanceof EnumType) {
            return unit;
        }

        visitConstructors(unit);
        visitWireFields(unit, typeContext);
        visitImports(unit);
        visitOptionsBuilder(unit);
        return unit;
    }

    private void visitConstructors(CompilationUnit unit) {
        unit.getTypes().stream()
            .filter(type -> type.getConstructors().stream()
                .noneMatch(ctor -> ctor.getParameters().isEmpty())
            )
            .forEach(type -> type.getConstructors().stream()
                .filter(ctor -> ctor.getParameters().stream().noneMatch(
                    parameter -> "unknownFields".equals(parameter.getNameAsString())
                ))
                .findFirst()
                .ifPresent(ctor -> ctor.getBody().getStatements().stream()
                    .filter(Statement::isExplicitConstructorInvocationStmt)
                    .findFirst()
                    .map(Statement::asExplicitConstructorInvocationStmt)
                    .ifPresent(stmt -> {
                        Expression expression = stmt.getArguments().get(stmt.getArguments().size() - 1);
                        type.getMembers().addBefore(
                            new ConstructorDeclaration()
                                .addModifier(Keyword.PUBLIC)
                                .setName(ctor.getName())
                                .setBody(new BlockStmt(new NodeList<>(
                                    new ExplicitConstructorInvocationStmt(false, null, new NodeList<>(
                                        new NameExpr("ADAPTER"), expression
                                    ))
                                ))),
                            ctor);
                    })
                )
            );
    }

    private void visitWireFields(CompilationUnit unit, TypeContext typeContext) {
        List<Name> imports = new LinkedList<>();
        unit.getTypes().stream()
            .peek(typeDeclaration ->
                typeContext.getAnnotations().keySet().forEach(node -> {
                    typeDeclaration.addAnnotation(node);
                    imports.addAll(typeContext.getContext().parseImports(node));
                })
            )
            .forEach(type -> type.getFields().stream()
                .filter(field -> field.isAnnotationPresent("WireField"))
                .forEach(field -> {
                    field.setModifiers(Keyword.PRIVATE);
                    addGetters(typeContext, type, field);
                    addFieldAnnotations(typeContext, field, imports);
                }));
        imports.stream()
            .sorted(Comparator.comparing(Name::asString))
            .forEach(i -> unit.addImport(i.asString()));
    }

    private void addGetters(TypeContext typeContext,
                            TypeDeclaration<?> declaration,
                            FieldDeclaration filed) {
        String type = filed.getElementType().asString();
        boolean isBool = StringUtils.equalsAny(type, "Boolean", "boolean");
        filed.getVariables().forEach(v -> {
            String name = StringUtils.capitalize(v.getNameAsString());
            if (typeContext.isJavaGetter()) {
                declaration
                    .addMethod((isBool ? "is" : "get") + name, Keyword.PUBLIC)
                    .setType(filed.getElementType())
                    .setBody(new BlockStmt(NodeList.nodeList(
                        new ReturnStmt(new FieldAccessExpr(new ThisExpr(), v.getNameAsString()))
                    )));
            }
            if (typeContext.isJavaSetter()) {
                declaration
                    .addMethod("set" + name, Keyword.PUBLIC)
                    .setType(type)
                    .setParameters(NodeList.nodeList(new Parameter(filed.getElementType(), name)))
                    .setBody(new BlockStmt(NodeList.nodeList(
                        new ExpressionStmt(new AssignExpr(
                            new FieldAccessExpr(new ThisExpr(), name),
                            v.getNameAsExpression(),
                            AssignExpr.Operator.ASSIGN
                        )),
                        new ReturnStmt(new ThisExpr())
                    )));
            }
        });
    }

    private void addFieldAnnotations(TypeContext typeContext, FieldDeclaration filed, List<Name> imports) {
        filed.getVariables().stream()
            .findFirst()
            .flatMap(variable -> typeContext.fieldContext(variable.getNameAsString()))
            .ifPresent(fieldContext ->
                fieldContext.getAnnotations().keySet().forEach(node -> {
                    filed.addAnnotation(node);
                    imports.addAll(typeContext.getContext().parseImports(node));
                })
            );
    }

    private void visitImports(CompilationUnit unit) {
        unit.getImports().forEach(importDeclaration -> {
            Name importName = importDeclaration.getName();
            importName.getQualifier().ifPresent(name -> {
                if ("com.google.protobuf".equals(name.asString())) {
                    importDeclaration.setName(new Name(
                        new Name("com.github.jg513.webpb.options"),
                        importName.getIdentifier()
                    ));
                }
            });
        });
    }

    private void visitOptionsBuilder(CompilationUnit unit) {
        unit.getImports().removeIf(importDeclaration ->
            Arrays.class.getName().equals(importDeclaration.getNameAsString())
        );
        unit.getTypes().forEach(type ->
            type.getFields().forEach(field -> {
                Stream.of(FileOptions.class, FieldOptions.class, MessageOptions.class)
                    .map(Class::getSimpleName)
                    .filter(typeName -> typeName.equals(field.getElementType().asString()))
                    .forEach(s ->
                        field.getVariables().forEach(v -> this.visitOptionsVariable(unit, v))
                    );
            })
        );
    }

    private void visitOptionsVariable(CompilationUnit unit, VariableDeclarator variable) {
        variable.getInitializer().ifPresent(new Consumer<Expression>() {
            @Override
            public void accept(Expression expression) {
                if (!expression.isMethodCallExpr()) {
                    return;
                }
                MethodCallExpr expr = expression.asMethodCallExpr();
                if (methodsMapping.containsKey(expr.getNameAsString())) {
                    expr.setName(methodsMapping.get(expr.getNameAsString()));
                    visitOptionsVariableInitializer(unit, expr);
                }
                expr.getScope().ifPresent(this);
            }
        });
    }

    private void visitOptionsVariableInitializer(CompilationUnit unit, MethodCallExpr expr) {
        expr.setArguments(NodeList.nodeList(expr.getArguments().stream()
            .map(argument -> {
                if (!argument.isMethodCallExpr()) {
                    return argument;
                }
                MethodCallExpr call = argument.asMethodCallExpr();
                if (!"asList".equals(call.getNameAsString())) {
                    return argument;
                }
                if (call.getScope().isPresent()
                    && !"Arrays".equals(call.getScope().get().toString())) {
                    return argument;
                }
                if (call.getArguments().size() > 1) {
                    unit.addImport(Arrays.class);
                    return argument;
                }
                unit.addImport(Collections.class);
                return new MethodCallExpr(
                    new NameExpr(Collections.class.getSimpleName()),
                    "singletonList",
                    call.getArguments()
                );
            })
            .collect(Collectors.toList())
        ));
    }
}
