package com.github.jg513.webpb.writers.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.jg513.webpb.WebpbMessage;
import com.github.jg513.webpb.common.Const;
import com.github.jg513.webpb.common.ParamGroup;
import com.github.jg513.webpb.common.options.FieldOptions;
import com.github.jg513.webpb.common.options.MessageOptions;
import com.squareup.wire.schema.EnclosingType;
import com.squareup.wire.schema.EnumConstant;
import com.squareup.wire.schema.EnumType;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.ProtoType;
import com.squareup.wire.schema.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JavaGenerator {
    private static final String ENUM_VALUE = "code";

    private static final Map<ProtoType, Type> TYPES_MAP = new HashMap<ProtoType, Type>() {{
        put(ProtoType.BOOL, new ClassOrInterfaceType(null, Boolean.class.getSimpleName()));
        put(ProtoType.BYTES, new ArrayType(PrimitiveType.byteType()));
        put(ProtoType.DOUBLE, new ClassOrInterfaceType(null, Double.class.getSimpleName()));
        put(ProtoType.FLOAT, new ClassOrInterfaceType(null, Float.class.getSimpleName()));
        put(ProtoType.FIXED32, new ClassOrInterfaceType(null, Integer.class.getSimpleName()));
        put(ProtoType.FIXED64, new ClassOrInterfaceType(null, Long.class.getSimpleName()));
        put(ProtoType.INT32, new ClassOrInterfaceType(null, Integer.class.getSimpleName()));
        put(ProtoType.INT64, new ClassOrInterfaceType(null, Long.class.getSimpleName()));
        put(ProtoType.SFIXED32, new ClassOrInterfaceType(null, Integer.class.getSimpleName()));
        put(ProtoType.SFIXED64, new ClassOrInterfaceType(null, Long.class.getSimpleName()));
        put(ProtoType.SINT32, new ClassOrInterfaceType(null, Integer.class.getSimpleName()));
        put(ProtoType.SINT64, new ClassOrInterfaceType(null, Long.class.getSimpleName()));
        put(ProtoType.STRING, new ClassOrInterfaceType(null, String.class.getSimpleName()));
        put(ProtoType.UINT32, new ClassOrInterfaceType(null, Integer.class.getSimpleName()));
        put(ProtoType.UINT64, new ClassOrInterfaceType(null, Long.class.getSimpleName()));
    }};

    private JavaOptions options;

    private Schema schema;

    private JavaParser parser = new JavaParser();

    private JavaGenerator(Schema schema, JavaOptions options) {
        this.schema = schema;
        this.options = options;
    }

    public static JavaGenerator create(Schema schema, JavaOptions options) {
        return new JavaGenerator(schema, options);
    }

    public CompilationUnit generate(ProtoFile file, com.squareup.wire.schema.Type type) {
        CompilationUnit unit = new CompilationUnit();
        unit.addOrphanComment(new LineComment(Const.HEADER));
        unit.addOrphanComment(new LineComment(Const.GIT_URL));
        unit.setPackageDeclaration(getPackage(file, type));
        List<Name> imports = new ArrayList<>();
        TypeDeclaration declaration = generateType(type, imports);
        imports.sort(Comparator.comparing(Name::asString));
        imports.forEach(i -> unit.addImport(i.asString()));
        unit.addType(declaration);
        return unit;
    }

    private String getPackage(ProtoFile file, com.squareup.wire.schema.Type type) {
        if (StringUtils.isNotEmpty(file.javaPackage())) {
            return file.javaPackage();
        }
        return type.type().enclosingTypeOrPackage();
    }

    private TypeDeclaration generateType(com.squareup.wire.schema.Type type, List<Name> imports) {
        if (type instanceof MessageType) {
            return generateMessage((MessageType) type, imports);
        } else if (type instanceof EnumType) {
            return generateEnum((EnumType) type);
        } else if (type instanceof EnclosingType) {
            return generateEnclosingType((EnclosingType) type, imports);
        } else {
            throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private ClassOrInterfaceDeclaration generateMessage(MessageType type, List<Name> imports) {
        ClassOrInterfaceDeclaration declaration = new ClassOrInterfaceDeclaration();
        declaration.setName(type.type().simpleName());
        declaration.addModifier(Modifier.Keyword.PUBLIC);
        parser.parseName(WebpbMessage.class.getName()).ifSuccessful(imports::add);
        declaration.addImplementedType(WebpbMessage.class);

        generateTypeAnnotations(type, declaration, imports);

        addMethodOption(type, declaration);
        addPathOption(type, declaration);

        generateMessageFields(imports, type, declaration);

        for (com.squareup.wire.schema.Type nestedType : type.nestedTypes()) {
            TypeDeclaration typeDeclaration = generateType(nestedType, imports);
            typeDeclaration.addModifier(Modifier.Keyword.STATIC);
            declaration.addMember(typeDeclaration);
        }
        return declaration;
    }

    private EnumDeclaration generateEnum(EnumType type) {
        EnumDeclaration declaration = new EnumDeclaration();
        declaration.setName(type.type().simpleName());
        declaration.addModifier(Modifier.Keyword.PUBLIC);

        for (EnumConstant constant : type.constants()) {
            EnumConstantDeclaration enumConstant = declaration.addEnumConstant(constant.getName());
            enumConstant.addArgument(new IntegerLiteralExpr(constant.getTag()));
        }

        declaration.addField(PrimitiveType.intType(), ENUM_VALUE, Modifier.Keyword.PRIVATE);
        generateEnumConstructor(declaration);
        generateEnumOfMethod(declaration, type);
        generateEnumValueGetter(declaration);
        return declaration;
    }

    private TypeDeclaration generateEnclosingType(EnclosingType type, List<Name> imports) {
        ClassOrInterfaceDeclaration declaration = new ClassOrInterfaceDeclaration();
        declaration.addModifier(Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL);
        declaration.setName(type.type().simpleName());
        for (com.squareup.wire.schema.Type nestedType : type.nestedTypes()) {
            declaration.addMember(generateType(nestedType, imports));
        }
        return generateType(type, imports);
    }

    @SuppressWarnings("unchecked")
    private void generateTypeAnnotations(MessageType type, TypeDeclaration declaration, List<Name> imports) {
        for (Map.Entry<AnnotationExpr, Name> entry : options.getAnnotationMap().entrySet()) {
            declaration.addAnnotation(entry.getKey());
            imports.add(entry.getValue());
        }
        List<String> annotations = (List<String>) type.options().get(MessageOptions.JAVA_ANNO);
        addAnnotations(declaration, annotations, imports);
    }

    private void addAnnotations(BodyDeclaration declaration, List<String> annotations, List<Name> imports) {
        if (annotations != null && !annotations.isEmpty()) {
            for (String annotation : annotations) {
                parser.parseAnnotation(annotation).ifSuccessful(expr -> {
                    parseImports(expr, imports);
                    expr.getName().setQualifier(null);
                    declaration.addAnnotation(expr);
                });
            }
        }
    }

    private void generateEnumConstructor(EnumDeclaration declaration) {
        ConstructorDeclaration constructor = declaration.addConstructor();
        constructor.addParameter(new Parameter(PrimitiveType.intType(), ENUM_VALUE));
        constructor.getBody().addStatement(new AssignExpr(
            new FieldAccessExpr(new ThisExpr(), ENUM_VALUE),
            new NameExpr(ENUM_VALUE),
            AssignExpr.Operator.ASSIGN
        ));
    }

    private void generateEnumOfMethod(EnumDeclaration declaration, EnumType type) {
        MethodDeclaration method = declaration.addMethod("of", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        method.addParameter(new Parameter(PrimitiveType.intType(), ENUM_VALUE));
        method.setType(declaration.getName().asString());
        NodeList<SwitchEntry> entries = new NodeList<>();
        for (EnumConstant constant : type.constants()) {
            entries.add(new SwitchEntry(
                NodeList.nodeList(new IntegerLiteralExpr(constant.getTag())),
                SwitchEntry.Type.STATEMENT_GROUP,
                NodeList.nodeList(new ReturnStmt(new NameExpr(constant.getName())))
            ));
        }
        entries.add(new SwitchEntry().addStatement(new ReturnStmt("null")));
        method.setBody(new BlockStmt().addStatement(new SwitchStmt(new NameExpr(ENUM_VALUE), entries)));
    }

    private void generateEnumValueGetter(EnumDeclaration declaration) {
        MethodDeclaration method = declaration.addMethod("get" + StringUtils.capitalize(ENUM_VALUE), Modifier.Keyword.PUBLIC);
        method.setType(PrimitiveType.intType());
        method.setBody(new BlockStmt().addStatement(new ReturnStmt(
            new FieldAccessExpr(new ThisExpr(), ENUM_VALUE)
        )));
    }

    @SuppressWarnings("unchecked")
    private void generateMessageFields(List<Name> imports, MessageType type, ClassOrInterfaceDeclaration clazz) {
        for (Field field : type.fieldsAndOneOfFields()) {
            if ("true".equals(field.options().get(FieldOptions.OMITTED))) {
                continue;
            }
            Type fieldType = getType(field);
            parseImports(fieldType, imports);
            FieldDeclaration declaration = clazz.addField(fieldType, field.name(), Modifier.Keyword.PRIVATE);
            List<String> annotations = (List<String>) field.options().get(FieldOptions.JAVA_ANNO);
            addAnnotations(declaration, annotations, imports);
        }
    }

    private void addMethodOption(MessageType type, ClassOrInterfaceDeclaration declaration) {
        Field field = schema.getField(MessageOptions.METHOD);
        String value = (String) type.options().get(MessageOptions.METHOD);
        if (StringUtils.isNotEmpty(value)) {
            addStaticOption(declaration, field, "METHOD", value);
        }
    }

    private void addPathOption(MessageType type, ClassOrInterfaceDeclaration declaration) {
        Field field = schema.getField(MessageOptions.PATH);
        String path = (String) type.options().get(MessageOptions.PATH);
        if (StringUtils.isNotEmpty(path)) {
            ParamGroup.of(path).validation(schema, type);
            String value = StringUtils.isEmpty(path) ? "" : path.split("\\?")[0];
            addStaticOption(declaration, field, "PATH", value);
        }
    }

    private void addStaticOption(ClassOrInterfaceDeclaration declaration, Field field, String key, String value) {
        if (field != null && StringUtils.isNotEmpty(value)) {
            declaration.addFieldWithInitializer(getType(field), key, new StringLiteralExpr(value),
                Modifier.Keyword.PUBLIC,
                Modifier.Keyword.STATIC,
                Modifier.Keyword.FINAL
            );
        }
    }

    private Type getType(Field field) {
        ProtoType type = field.type();
        if (field.isRepeated()) {
            return new ClassOrInterfaceType(null, new SimpleName(List.class.getSimpleName()), NodeList.nodeList(toType(type)));
        } else {
            return toType(type);
        }
    }

    private Type toType(ProtoType protoType) {
        if (protoType.isMap()) {
            return new ClassOrInterfaceType(null, new SimpleName(Map.class.getSimpleName()), NodeList.nodeList(
                toType(protoType.keyType()),
                toType(protoType.valueType())
            ));
        }
        Type type = TYPES_MAP.get(protoType);
        if (type != null) {
            return type.clone();
        }
        return new ClassOrInterfaceType(null, protoType.simpleName());
    }

    private void parseImports(Node node, List<Name> imports) {
        if (node instanceof FieldAccessExpr) {
            imports.add(options.getFullName(new Name(null, ((FieldAccessExpr) node).getScope().toString())));
        } else if (node instanceof AnnotationExpr) {
            imports.add(options.getFullName(((AnnotationExpr) node).getName()));
        } else if (node instanceof ClassOrInterfaceType) {
            addImports((ClassOrInterfaceType) node, imports);
        } else {
            options.getName(node.toString()).ifPresent(imports::add);
        }
        for (Node childNode : node.getChildNodes()) {
            parseImports(childNode, imports);
        }
    }

    private void addImports(ClassOrInterfaceType type, List<Name> imports) {
        options.getName(type.getNameAsString()).ifPresent(imports::add);
        type.getTypeArguments().ifPresent(list -> {
            for (Type t : list) {
                if (t instanceof ClassOrInterfaceType) {
                    addImports((ClassOrInterfaceType) t, imports);
                }
            }
        });
    }
}
