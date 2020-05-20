package com.github.jg513.webpb.writers.wire;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
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
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.jg513.webpb.core.context.TypeContext;
import com.squareup.wire.schema.EnumType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        put("ts_string", "tsString");
    }};

    private JavaParser javaParser = new JavaParser();

    public CompilationUnit filter(TypeContext typeContext, String content) {
        CompilationUnit unit = javaParser.parse(content)
            .getResult().orElseThrow(() -> new RuntimeException("Error"));
        if (typeContext.getType() instanceof EnumType) {
            return unit;
        }

        unit
            .accept(new ModifierVisitor<TypeContext>() {

                ClassOrInterfaceDeclaration classDeclaration = null;

                @Override
                public Visitable visit(ClassOrInterfaceDeclaration n, TypeContext arg) {
                    if (!isMessage(n)) {
                        return super.visit(n, arg);
                    }
                    classDeclaration = n;
                    visitWireFields(n, unit, arg);
                    if (n.getConstructors().stream().anyMatch(ctor -> ctor.getParameters().isEmpty())) {
                        return super.visit(n, arg);
                    }
                    n.getConstructors().stream()
                        .findFirst()
                        .ifPresent(ctor -> ctor.getBody().getStatements().stream()
                            .filter(Statement::isExplicitConstructorInvocationStmt)
                            .findFirst()
                            .map(Statement::asExplicitConstructorInvocationStmt)
                            .ifPresent(stmt -> {
                                Expression expression = stmt.getArguments().get(stmt.getArguments().size() - 1);
                                n.getMembers().addBefore(
                                    new ConstructorDeclaration()
                                        .addModifier(Keyword.PUBLIC)
                                        .setName(ctor.getName())
                                        .setBody(new BlockStmt(new NodeList<>(
                                            new ExplicitConstructorInvocationStmt(false, null, new NodeList<>(
                                                new NameExpr("ADAPTER"),
                                                new FieldAccessExpr(new NameExpr("ByteString"), "EMPTY")
                                            ))
                                        ))),
                                    ctor);
                            })
                        );
                    return super.visit(n, arg);
                }

                @Override
                public Node visit(ImportDeclaration n, TypeContext arg) {
                    Name importName = n.getName();
                    importName.getQualifier().ifPresent(name -> {
                        if ("com.google.protobuf".equals(name.asString())) {
                            n.setName(new Name(
                                new Name("com.github.jg513.webpb.options"),
                                importName.getIdentifier()
                            ));
                        }
                    });
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(MethodCallExpr n, TypeContext arg) {
                    if (methodsMapping.containsKey(n.getNameAsString())) {
                        n.setName(methodsMapping.get(n.getNameAsString()));
                        visitOptionsVariableInitializer(unit, n);
                    }
                    return super.visit(n, arg);
                }

                @Override
                public Visitable visit(FieldDeclaration n, TypeContext arg) {
                    if (n.isStatic() && n.getVariable(0).getNameAsString().equals("MESSAGE_OPTIONS")) {
                        List<ConstructorDeclaration> constructors = classDeclaration.getConstructors();
                        classDeclaration.getMembers().addAfter(
                            new MethodDeclaration()
                                .setModifiers(Keyword.PUBLIC)
                                .setType("MessageOptions")
                                .setName("messageOptions")
                                .setBody(new BlockStmt(
                                    NodeList.nodeList(new ReturnStmt("MESSAGE_OPTIONS"))
                                )),
                            constructors.get(constructors.size() - 1)
                        );
                    }
                    return super.visit(n, arg);
                }
            }, typeContext);
        return unit;
    }

    private boolean isMessage(ClassOrInterfaceDeclaration declaration) {
        return declaration.getExtendedTypes().stream()
            .anyMatch(type -> {
                if (!type.getNameAsString().equals("Message") || !type.getTypeArguments().isPresent()) {
                    return false;
                }
                NodeList<Type> types = type.getTypeArguments().get();
                if (types.size() != 2) {
                    return false;
                }
                Type type1 = types.get(0);
                Type type2 = types.get(1);
                return type1.asString().equals(declaration.getNameAsString())
                    && type2.asString().equals(declaration.getNameAsString() + ".Builder");
            });
    }

    private void visitWireFields(TypeDeclaration<?> typeDeclaration, CompilationUnit unit, TypeContext typeContext) {
        List<Name> imports = new LinkedList<>();
        typeContext.getAnnotations().keySet().forEach(node -> {
            typeDeclaration.addAnnotation(node);
            imports.addAll(typeContext.getContext().parseImports(node));
        });
        typeDeclaration.getFields().stream()
            .filter(field -> field.isAnnotationPresent("WireField"))
            .forEach(field -> {
                field.setModifiers(Keyword.PRIVATE);
                typeContext.fieldContext(field.getVariable(0).getNameAsString())
                    .ifPresent(fieldContext -> {
                        if (fieldContext.isOmitted()) {
                            field.addModifier(Keyword.TRANSIENT);
                        }
                    });
                addGetters(typeContext, typeDeclaration, field);
                addFieldAnnotations(typeContext, field, imports);
            });
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
