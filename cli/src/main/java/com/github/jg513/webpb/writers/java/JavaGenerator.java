package com.github.jg513.webpb.writers.java;

import com.github.jg513.webpb.WebpbMessage;
import com.github.jg513.webpb.common.Const;
import com.github.jg513.webpb.common.profile.Profile;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.NameAllocator;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.wire.schema.EnclosingType;
import com.squareup.wire.schema.EnumConstant;
import com.squareup.wire.schema.EnumType;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.ProtoMember;
import com.squareup.wire.schema.ProtoType;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.Service;
import com.squareup.wire.schema.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import okio.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.squareup.wire.schema.Options.ENUM_OPTIONS;
import static com.squareup.wire.schema.Options.FIELD_OPTIONS;
import static com.squareup.wire.schema.Options.MESSAGE_OPTIONS;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class JavaGenerator {
    private static final ClassName BYTE_STRING = ClassName.get(ByteString.class);

    private static final ClassName STRING = ClassName.get(String.class);

    private static final ClassName LIST = ClassName.get(List.class);

    private static final Map<ProtoType, ClassName> BUILT_IN_TYPES_MAP =
        ImmutableMap.<ProtoType, ClassName>builder()
            .put(ProtoType.BOOL, (ClassName) TypeName.BOOLEAN.box())
            .put(ProtoType.BYTES, ClassName.get(ByteString.class))
            .put(ProtoType.DOUBLE, (ClassName) TypeName.DOUBLE.box())
            .put(ProtoType.FLOAT, (ClassName) TypeName.FLOAT.box())
            .put(ProtoType.FIXED32, (ClassName) TypeName.INT.box())
            .put(ProtoType.FIXED64, (ClassName) TypeName.LONG.box())
            .put(ProtoType.INT32, (ClassName) TypeName.INT.box())
            .put(ProtoType.INT64, (ClassName) TypeName.LONG.box())
            .put(ProtoType.SFIXED32, (ClassName) TypeName.INT.box())
            .put(ProtoType.SFIXED64, (ClassName) TypeName.LONG.box())
            .put(ProtoType.SINT32, (ClassName) TypeName.INT.box())
            .put(ProtoType.SINT64, (ClassName) TypeName.LONG.box())
            .put(ProtoType.STRING, ClassName.get(String.class))
            .put(ProtoType.UINT32, (ClassName) TypeName.INT.box())
            .put(ProtoType.UINT64, (ClassName) TypeName.LONG.box())
            .put(FIELD_OPTIONS, ClassName.get("com.google.protobuf", "MessageOptions"))
            .put(ENUM_OPTIONS, ClassName.get("com.google.protobuf", "FieldOptions"))
            .put(MESSAGE_OPTIONS, ClassName.get("com.google.protobuf", "EnumOptions"))
            .build();

    private static final String URL_CHARS = "[-!#$%&'()*+,./0-9:;=?@A-Z\\[\\]_a-z~]";

    private final LoadingCache<Type, NameAllocator> nameAllocators
        = CacheBuilder.newBuilder().build(new CacheLoader<Type, NameAllocator>() {
        @Override
        public NameAllocator load(@NotNull Type type) {
            NameAllocator nameAllocator = new NameAllocator();

            if (type instanceof MessageType) {
                nameAllocator.newName("ADAPTER", "ADAPTER");

                ImmutableList<Field> fieldsAndOneOfFields = ((MessageType) type).fieldsAndOneOfFields();
                Set<String> collidingNames = collidingFieldNames(fieldsAndOneOfFields);
                for (Field field : fieldsAndOneOfFields) {
                    String suggestion = collidingNames.contains(field.name())
                        ? field.qualifiedName()
                        : field.name();
                    nameAllocator.newName(suggestion, field);
                }

            } else if (type instanceof EnumType) {
                nameAllocator.newName("value", "value");
                nameAllocator.newName("i", "i");
                nameAllocator.newName("reader", "reader");
                nameAllocator.newName("writer", "writer");

                for (EnumConstant constant : ((EnumType) type).constants()) {
                    nameAllocator.newName(constant.getName(), constant);
                }
            }

            return nameAllocator;
        }
    });

    private final Schema schema;

    private final ImmutableMap<ProtoType, ClassName> nameToJavaName;

    private final Profile profile;

    private JavaGenerator(Schema schema, Map<ProtoType, ClassName> nameToJavaName, Profile profile) {
        this.schema = schema;
        this.nameToJavaName = ImmutableMap.copyOf(nameToJavaName);
        this.profile = profile;
    }

    public static JavaGenerator create(Schema schema) {
        Map<ProtoType, ClassName> nameToJavaName = new LinkedHashMap<>(BUILT_IN_TYPES_MAP);

        for (ProtoFile protoFile : schema.protoFiles()) {
            String javaPackage = javaPackage(protoFile);
            putAll(nameToJavaName, javaPackage, null, protoFile.types());

            for (Service service : protoFile.services()) {
                ClassName className = ClassName.get(javaPackage, service.type().simpleName());
                nameToJavaName.put(service.type(), className);
            }
        }

        return new JavaGenerator(schema, nameToJavaName, new Profile());
    }

    private static void putAll(Map<ProtoType, ClassName> wireToJava, String javaPackage,
                               ClassName enclosingClassName, List<Type> types) {
        for (Type type : types) {
            ClassName className = enclosingClassName != null
                ? enclosingClassName.nestedClass(type.type().simpleName())
                : ClassName.get(javaPackage, type.type().simpleName());
            wireToJava.put(type.type(), className);
            putAll(wireToJava, javaPackage, className, type.nestedTypes());
        }
    }

    public Schema schema() {
        return schema;
    }

    private TypeName typeName(ProtoType protoType) {
        String name = profile.getTargetName(protoType);
        TypeName profileJavaName = name == null ? null : ClassName.bestGuess(name);
        if (profileJavaName != null) {
            return profileJavaName;
        }
        TypeName candidate = nameToJavaName.get(protoType);
        checkArgument(candidate != null, "unexpected type %s", protoType);
        return candidate;
    }

    @Nullable
    private ClassName abstractAdapterName(ProtoType protoType) {
        String name = profile.getTargetName(protoType);
        TypeName profileJavaName = name == null ? null : ClassName.bestGuess(name);
        if (profileJavaName == null) {
            return null;
        }

        ClassName javaName = nameToJavaName.get(protoType);
        Type type = schema.getType(protoType);
        return type instanceof EnumType
            ? javaName.peerClass(javaName.simpleName() + "Adapter")
            : javaName.peerClass("Abstract" + javaName.simpleName() + "Adapter");
    }

    private static String javaPackage(ProtoFile protoFile) {
        String javaPackage = protoFile.javaPackage();
        if (javaPackage != null) {
            return javaPackage;
        } else if (protoFile.packageName() != null) {
            return protoFile.packageName();
        } else {
            return "";
        }
    }

    private boolean isEnum(ProtoType type) {
        return schema.getType(type) instanceof EnumType;
    }

    private EnumConstant enumDefault(ProtoType type) {
        EnumType wireEnum = (EnumType) schema.getType(type);
        return wireEnum.constants().get(0);
    }

    private static TypeName listOf(TypeName type) {
        return ParameterizedTypeName.get(LIST, type);
    }

    private static String sanitizeJavadoc(String documentation) {
        documentation = documentation.replaceAll("[^\\S\n]+\n", "\n");
        documentation = documentation.replaceAll("\\s+$", "");
        documentation = documentation.replaceAll("\\*/", "&#42;/");
        documentation = documentation.replaceAll(
            "@see (http:" + URL_CHARS + "+)", "@see <a href=\"$1\">$1</a>");
        return documentation;
    }

    public ClassName generatedTypeName(Type type) {
        ClassName abstractAdapterName = abstractAdapterName(type.type());
        return abstractAdapterName != null ? abstractAdapterName : (ClassName) typeName(type.type());
    }

    public TypeSpec generateType(Type type) {
        if (type instanceof MessageType) {
            return generateMessage((MessageType) type);
        }
        if (type instanceof EnumType) {
            return generateEnum((EnumType) type);
        }
        if (type instanceof EnclosingType) {
            return generateEnclosingType((EnclosingType) type);
        }
        throw new IllegalStateException("Unknown type: " + type);
    }

    private TypeSpec generateEnum(EnumType type) {
        NameAllocator nameAllocator = nameAllocators.getUnchecked(type);
        String value = nameAllocator.get("value");
        ClassName javaType = (ClassName) typeName(type.type());

        TypeSpec.Builder builder = TypeSpec.enumBuilder(javaType.simpleName()).addModifiers(PUBLIC);

        if (!type.documentation().isEmpty()) {
            builder.addJavadoc("$L\n", sanitizeJavadoc(type.documentation()));
        }

        // Output Private tag field
        builder.addField(TypeName.INT, value, PRIVATE, FINAL);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
        constructorBuilder.addStatement("this.$1N = $1N", value);
        constructorBuilder.addParameter(TypeName.INT, value);

        Set<ProtoMember> allOptionFieldsBuilder = new LinkedHashSet<>();
        for (EnumConstant constant : type.constants()) {
            for (ProtoMember protoMember : constant.getOptions().map().keySet()) {
                Field optionField = schema.getField(protoMember);
                if (allOptionFieldsBuilder.add(protoMember)) {
                    TypeName optionJavaType = typeName(optionField.type());
                    builder.addField(optionJavaType, optionField.name(), PUBLIC, FINAL);
                    constructorBuilder.addParameter(optionJavaType, optionField.name());
                    constructorBuilder.addStatement("this.$L = $L", optionField.name(), optionField.name());
                }
            }
        }
        ImmutableList<ProtoMember> allOptionMembers = ImmutableList.copyOf(allOptionFieldsBuilder);
        String enumArgsFormat = "$L" + Strings.repeat(", $L", allOptionMembers.size());
        builder.addMethod(constructorBuilder.build());

        MethodSpec.Builder fromValueBuilder = MethodSpec.methodBuilder("fromValue")
            .addJavadoc("Return the constant for {@code $N} or null.\n", value)
            .addModifiers(PUBLIC, STATIC)
            .returns(javaType)
            .addParameter(int.class, value)
            .beginControlFlow("switch ($N)", value);

        Set<Integer> seenTags = new LinkedHashSet<>();
        for (EnumConstant constant : type.constants()) {
            Object[] enumArgs = new Object[allOptionMembers.size() + 1];
            enumArgs[0] = constant.getTag();
            for (int i = 0; i < allOptionMembers.size(); i++) {
                ProtoMember protoMember = allOptionMembers.get(i);
                Field field = schema.getField(protoMember);
                Object fieldValue = constant.getOptions().map().get(protoMember);
                enumArgs[i + 1] = fieldValue != null ? fieldInitializer(field.type(), fieldValue) : null;
            }

            TypeSpec.Builder constantBuilder = TypeSpec.anonymousClassBuilder(enumArgsFormat, enumArgs);
            if (!constant.getDocumentation().isEmpty()) {
                constantBuilder.addJavadoc("$L\n", sanitizeJavadoc(constant.getDocumentation()));
            }

            if ("true".equals(constant.getOptions().get(Const.ENUM_DEPRECATED))) {
                constantBuilder.addAnnotation(Deprecated.class);
            }

            builder.addEnumConstant(constant.getName(), constantBuilder.build());

            // Ensure constant case tags are unique, which might not be the case if allow_alias is true.
            if (seenTags.add(constant.getTag())) {
                fromValueBuilder.addStatement("case $L: return $L", constant.getTag(), constant.getName());
            }
        }

        builder.addMethod(fromValueBuilder.addStatement("default: return null")
            .endControlFlow()
            .build());

        // Public Getter
        builder.addMethod(MethodSpec.methodBuilder("getValue")
            .addModifiers(PUBLIC)
            .returns(TypeName.INT)
            .addStatement("return $N", value)
            .build());

        return builder.build();
    }

    private TypeSpec generateMessage(MessageType type) {
        NameAllocator nameAllocator = nameAllocators.getUnchecked(type);

        ClassName javaType = (ClassName) typeName(type.type());

        TypeSpec.Builder builder = TypeSpec.classBuilder(javaType.simpleName());
        builder.addModifiers(PUBLIC);
        builder.addSuperinterface(WebpbMessage.class);

        if (javaType.enclosingClassName() != null) {
            builder.addModifiers(STATIC);
        }

        if (!type.documentation().isEmpty()) {
            builder.addJavadoc("$L\n", sanitizeJavadoc(type.documentation()));
        }

        generatePathOption(type, builder);
        generateMethodOption(type, builder);
        generateMessageFields(type, nameAllocator, builder);

        for (Type nestedType : type.nestedTypes()) {
            builder.addType(generateType(nestedType));
        }

        builder.addAnnotation(Getter.class);
        builder.addAnnotation(Setter.class);
        builder.addAnnotation(AnnotationSpec.builder(Accessors.class)
            .addMember("chain", "true")
            .build()
        );
        return builder.build();
    }

    private void generateMessageFields(MessageType type, NameAllocator nameAllocator, TypeSpec.Builder builder) {
        for (Field field : type.fieldsAndOneOfFields()) {
            if ("true".equals(field.options().get(Const.OMITTED))) {
                continue;
            }
            TypeName fieldJavaType = fieldType(field);

            String fieldName = nameAllocator.get(field);
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldJavaType, fieldName, PRIVATE);
            if (!field.documentation().isEmpty()) {
                fieldBuilder.addJavadoc("$L\n", sanitizeJavadoc(field.documentation()));
            }
            if (field.isExtension()) {
                fieldBuilder.addJavadoc("Extension source: $L\n", field.location().withPathOnly());
            }
            if (field.isDeprecated()) {
                fieldBuilder.addAnnotation(Deprecated.class);
            }
            if ((field.type().isScalar() || isEnum(field.type()))
                && !field.isRepeated()
                && !field.isPacked()
                && field.getDefault() != null) {
                fieldBuilder.initializer(defaultValue(field));
            }
            builder.addField(fieldBuilder.build());
        }
    }

    private TypeSpec generateEnclosingType(EnclosingType type) {
        ClassName javaType = (ClassName) typeName(type.type());

        TypeSpec.Builder builder = TypeSpec.classBuilder(javaType.simpleName())
            .addModifiers(PUBLIC, FINAL);
        if (javaType.enclosingClassName() != null) {
            builder.addModifiers(STATIC);
        }

        String documentation = type.documentation();
        if (!documentation.isEmpty()) {
            documentation += "\n\n<p>";
        }
        documentation += "<b>NOTE:</b> This type only exists to maintain class structure"
            + " for its nested types and is not an actual message.";
        builder.addJavadoc("$L\n", documentation);

        builder.addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .addStatement("throw new $T()", AssertionError.class)
            .build());

        for (Type nestedType : type.nestedTypes()) {
            builder.addType(generateType(nestedType));
        }

        return builder.build();
    }

    private Set<String> collidingFieldNames(ImmutableList<Field> fields) {
        Set<String> fieldNames = new LinkedHashSet<>();
        Set<String> collidingNames = new LinkedHashSet<>();
        for (Field field : fields) {
            if (!fieldNames.add(field.name())) {
                collidingNames.add(field.name());
            }
        }
        return collidingNames;
    }

    private void generateMethodOption(MessageType type, TypeSpec.Builder builder) {
        Field field = schema.getField(Const.METHOD);
        String method = (String) type.options().get(Const.METHOD);
        builder.addField(FieldSpec.builder(fieldType(field), "METHOD")
            .addModifiers(PUBLIC, STATIC, FINAL)
            .initializer(fieldInitializer(field.type(), StringUtils.isEmpty(method) ? "" : method))
            .build());
    }

    private void generatePathOption(MessageType type, TypeSpec.Builder builder) {
        String path = (String) type.options().get(Const.PATH);
        String uri = StringUtils.isEmpty(path) ? "" : path.split("\\?")[0];
        Field field = schema.getField(Const.PATH);
        builder.addField(FieldSpec.builder(fieldType(field), "PATH")
            .addModifiers(PUBLIC, STATIC, FINAL)
            .initializer(fieldInitializer(field.type(), uri))
            .build());
    }

    private String fieldName(ProtoType type, Field field) {
        MessageType messageType = (MessageType) schema.getType(type);
        NameAllocator names = nameAllocators.getUnchecked(messageType);
        return names.get(field);
    }

    private TypeName fieldType(Field field) {
        ProtoType type = field.type();
        if (type.isMap()) {
            return ParameterizedTypeName.get(ClassName.get(Map.class),
                typeName(type.keyType()),
                typeName(type.valueType()));
        }
        TypeName messageType = typeName(type);
        return field.isRepeated() ? listOf(messageType) : messageType;
    }

    private CodeBlock defaultValue(Field field) {
        Object defaultValue = field.getDefault();

        if (defaultValue == null && isEnum(field.type())) {
            defaultValue = enumDefault(field.type()).getName();
        }

        if (field.type().isScalar() || defaultValue != null) {
            return fieldInitializer(field.type(), defaultValue);
        }

        throw new IllegalStateException("Field " + field + " cannot have default value");
    }

    private CodeBlock fieldInitializer(ProtoType type, @Nullable Object value) {
        TypeName javaType = typeName(type);

        if (value instanceof List) {
            CodeBlock.Builder builder = CodeBlock.builder();
            builder.add("$T.asList(", Arrays.class);
            boolean first = true;
            for (Object o : (List<?>) value) {
                if (!first) {
                    builder.add(",");
                }
                first = false;
                builder.add("\n$>$>$L$<$<", fieldInitializer(type, o));
            }
            builder.add(")");
            return builder.build();

        } else if (value instanceof Map) {
            CodeBlock.Builder builder = CodeBlock.builder();
            builder.add("new $T.Builder()", javaType);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                ProtoMember protoMember = (ProtoMember) entry.getKey();
                Field field = schema.getField(protoMember);
                CodeBlock valueInitializer = fieldInitializer(field.type(), entry.getValue());
                builder.add("\n$>$>.$L($L)$<$<", fieldName(type, field), valueInitializer);
            }
            builder.add("\n$>$>.build()$<$<");
            return builder.build();

        } else if (javaType.equals(TypeName.BOOLEAN.box())) {
            return CodeBlock.of("$L", value != null ? value : false);

        } else if (javaType.equals(TypeName.INT.box())) {
            return CodeBlock.of("$L", valueToInt(value));

        } else if (javaType.equals(TypeName.LONG.box())) {
            return CodeBlock.of("$LL", Long.toString(valueToLong(value)));

        } else if (javaType.equals(TypeName.FLOAT.box())) {
            return CodeBlock.of("$Lf", value != null ? String.valueOf(value) : 0f);

        } else if (javaType.equals(TypeName.DOUBLE.box())) {
            return CodeBlock.of("$Ld", value != null ? String.valueOf(value) : 0d);

        } else if (javaType.equals(STRING)) {
            return CodeBlock.of("$S", value != null ? (String) value : "");

        } else if (javaType.equals(BYTE_STRING)) {
            if (value == null) {
                return CodeBlock.of("$T.EMPTY", ByteString.class);
            } else {
                return CodeBlock.of("$T.decodeBase64($S)", ByteString.class,
                    ByteString.encodeString(String.valueOf(value), Charsets.ISO_8859_1).base64());
            }

        } else if (isEnum(type) && value != null) {
            return CodeBlock.of("$T.$L", javaType, value);
        } else {
            throw new IllegalStateException(type + " is not an allowed scalar type");
        }
    }

    private static int valueToInt(@Nullable Object value) {
        if (value == null) {
            return 0;
        }

        String string = String.valueOf(value);
        if (string.startsWith("0x") || string.startsWith("0X")) {
            return Integer.valueOf(string.substring("0x".length()), 16); // Hexadecimal.
        } else if (string.startsWith("0") && !string.equals("0")) {
            throw new IllegalStateException("Octal literal unsupported: " + value); // Octal.
        } else {
            return new BigInteger(string).intValue(); // Decimal.
        }
    }

    private static long valueToLong(@Nullable Object value) {
        if (value == null) {
            return 0L;
        }

        String string = String.valueOf(value);
        if (string.startsWith("0x") || string.startsWith("0X")) {
            return Long.valueOf(string.substring("0x".length()), 16); // Hexadecimal.
        } else if (string.startsWith("0") && !string.equals("0")) {
            throw new IllegalStateException("Octal literal unsupported: " + value); // Octal.
        } else {
            return new BigInteger(string).longValue(); // Decimal.
        }
    }
}
