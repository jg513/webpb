package com.github.jg513.webpb.writers.wire;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.jg513.webpb.core.context.TypeContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.github.javaparser.ast.Modifier.Keyword;

public class JavaParserFilter {
    private JavaParser javaParser = new JavaParser();

    public CompilationUnit filter(TypeContext typeContext, String content) {
        CompilationUnit unit = javaParser.parse(content)
            .getResult().orElseThrow(() -> new RuntimeException("Error"));

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
        return unit;
    }

    private void addGetters(TypeContext typeContext,
                            TypeDeclaration<?> declaration,
                            FieldDeclaration filed) {
        String type = filed.getElementType().asString();
        boolean isBool = StringUtils.equalsAny(type, "Boolean", "boolean");
        filed.getVariables().forEach(v -> {
            String name = StringUtils.capitalize(v.getNameAsString());
            if (typeContext.isGetter()) {
                declaration
                    .addMethod((isBool ? "is" : "get") + name, Keyword.PUBLIC)
                    .setType(filed.getElementType())
                    .setBody(new BlockStmt(NodeList.nodeList(
                        new ReturnStmt(new FieldAccessExpr(new ThisExpr(), v.getNameAsString()))
                    )));
            }
            if (typeContext.isSetter()) {
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
}
