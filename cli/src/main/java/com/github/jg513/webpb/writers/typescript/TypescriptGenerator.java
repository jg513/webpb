package com.github.jg513.webpb.writers.typescript;

import com.github.jg513.webpb.core.Const;
import com.github.jg513.webpb.core.Handler;
import com.github.jg513.webpb.core.ParamGroup;
import com.github.jg513.webpb.core.Utils;
import com.github.jg513.webpb.core.context.FieldContext;
import com.github.jg513.webpb.core.context.FileContext;
import com.github.jg513.webpb.core.context.SchemaContext;
import com.github.jg513.webpb.core.context.TypeContext;
import com.github.jg513.webpb.core.options.FieldOptions;
import com.github.jg513.webpb.core.options.MessageOptions;
import com.squareup.wire.schema.EnclosingType;
import com.squareup.wire.schema.EnumConstant;
import com.squareup.wire.schema.EnumType;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.Options;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.ProtoType;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.Type;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor(staticName = "of")
final class TypescriptGenerator {

    private static final String INDENT = "    ";

    private final SchemaContext schemaContext;

    private final Schema schema;

    private final List<String> tags;

    private final StringBuilder builder;

    private final Set<String> imports = new HashSet<>();

    private int level = 0;

    private boolean withLong = false;

    private FileContext fileContext;

    public boolean generate(ProtoFile protoFile) {
        String packageName = protoFile.getPackageName();
        this.schemaContext.fileContext(protoFile)
            .ifPresent(fileContext -> this.fileContext = fileContext);
        assert fileContext != null;
        if (generateTypes(packageName, protoFile.getTypes())) {
            StringBuilder builder = new StringBuilder();
            builder.append("// " + Const.HEADER + "\n");
            builder.append("// " + Const.GIT_URL + "\n\n");
            builder.append("import * as Webpb from 'webpb';\n\n");
            if (fileContext.isTsJson() || fileContext.isTsStream()) {
                builder.append("import * as $protobuf from \"protobufjs\";\n");
                if (fileContext.isTsLong() && withLong) {
                    builder.append("import * as $long from \"long\";\n");
                }
                builder.append("const $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;\n\n");
            }
            for (String type : imports) {
                if (!StringUtils.startsWith(type, packageName)) {
                    builder.append("import * as ").append(type)
                        .append(" from './").append(type).append("';\n\n");
                }
            }
            this.builder.insert(0, builder);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean generateTypes(String namespace, List<Type> types) {
        boolean hasContent = false;
        for (Type type : types) {
            if (type instanceof MessageType && !tags.isEmpty()) {
                List<String> tags = (List<String>) type.getOptions().get(MessageOptions.TAG);
                if (tags != null && !Utils.containsAny(this.tags, tags)) {
                    continue;
                }
            }
            hasContent = true;
            generateType(type);
            if (!type.getNestedTypes().isEmpty()) {
                level(() -> generateTypes(
                    Objects.requireNonNull(type.getType()).getSimpleName(),
                    type.getNestedTypes()
                ));
            }
        }
        if (level == 0) {
            trimDuplicatedNewline();
        }
        return hasContent;
    }

    private void generateType(Type type) {
        if (type instanceof MessageType) {
            generateMessage((MessageType) type);
        } else if (type instanceof EnumType) {
            generateEnum((EnumType) type);
        } else if (type instanceof EnclosingType) {
            generateEnclosingType((EnclosingType) type);
        } else {
            throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private void generateEnum(EnumType type) {
        indent().append("export enum ").append(type.getType().getSimpleName()).append(" {\n");

        for (EnumConstant constant : type.getConstants()) {
            level(() -> indent().append(constant.getName())
                .append(" = ").append(constant.getTag()).append(",\n")
            );
        }
        closeBracket();
    }

    private void generateMessage(MessageType type) {
        String className = type.getType().getSimpleName();

        indent().append("export interface ").append(interfaceName(className)).append(" {\n");
        level(() -> generateMessageFields(type, true));
        closeBracket();

        indent()
            .append("export class ").append(className)
            .append(" implements ").append(interfaceName(className));
        if (type.getOptions().get(MessageOptions.PATH) != null) {
            builder.append(", Webpb.WebpbMessage {\n");
        } else {
            builder.append(" {\n");
        }

        level(() -> {
            generateMessageFields(type, false);
            indent().append("META: () => Webpb.WebpbMeta;\n\n");
            generateConstructor(type, className);

            if (fileContext.isTsStream()) {
                Encoder encoder = new Encoder(this, builder);
                encoder.generateEncode(type, className);
                encoder.generateEncodeDelimited(type, className);

                Decoder decoder = new Decoder(this, builder);
                decoder.generateDecode(type);
                decoder.generateDecodeDelimited(type);
            }

            if (fileContext.isTsJson()) {
                ToObject toObject = new ToObject(this, builder);
                toObject.generateToObject(type, className);
                toObject.generateToJSON(type, className);
            }
        });
        closeBracket();
    }

    private void generateMessageFields(MessageType type, boolean isInterface) {
        TypeContext typeContext = fileContext.typeContext(type).orElse(null);
        for (Field field : type.getFieldsAndOneOfFields()) {
            ProtoType protoType = field.getType();
            assert protoType != null;
            if (!protoType.isScalar()) {
                addImport(field.getType());
            }
            indent().append(field.getName());
            if (field.isOptional()) {
                builder.append('?');
            } else if (!isInterface & field.getDefault() == null) {
                builder.append('!');
            }
            FieldContext fieldContext = typeContext == null
                ? null : typeContext.fieldContext(field.getName()).orElse(null);
            if (protoType.isMap()) {
                ProtoType keyType = protoType.getKeyType();
                ProtoType valueType = protoType.getValueType();
                assert keyType != null && valueType != null;
                builder.append(": ").append("{ [k: string]: ")
                    .append(toTypeName(type, valueType, fieldContext)).append(" }");
            } else {
                String typeName = toTypeName(type, protoType, fieldContext);
                builder.append(": ").append(typeName);
                if (field.isRepeated()) {
                    builder.append("[]");
                }
                if (!isInterface && field.getDefault() != null) {
                    builder.append(" = ");
                    if ("string".equals(typeName)) {
                        builder.append('"').append(field.getDefault()).append('"');
                    } else {
                        builder.append(field.getDefault());
                    }
                }
            }
            builder.append(";\n");
        }
    }

    private void generateConstructor(MessageType type, String className) {
        if (type.getFieldsAndOneOfFields().isEmpty()) {
            indent().append("private constructor() {\n");
            level(() -> {
                indent().append("this.META = () => ({\n");
                initializeMeta(type);
            });
            closeBracket();
            indent().append("static create(): ").append(className).append(" {\n");
            level(() -> indent().append("return new ").append(className).append("();\n"));
        } else {
            String interfaceName = interfaceName(className);
            indent().append("private constructor(p?: ").append(interfaceName).append(") {\n");
            level(() -> {
                indent().append("Webpb.assign(p, this, ")
                    .append(generateOmitted(type)).append(");\n");
                indent().append("this.META = () => (p && {\n");
                initializeMeta(type);
            });
            closeBracket();
            indent().append("static create(properties: ")
                .append(interfaceName).append("): ").append(className).append(" {\n");
            level(() -> indent().append("return new ")
                .append(type.getType().getSimpleName())
                .append("(properties").append(");\n"));
        }
        closeBracket();
    }

    private void initializeMeta(MessageType type) {
        level(() -> {
            generateMetaField("class", "'" + type.getType().getSimpleName() + "'");
            Options options = type.getOptions();
            String method = (String) options.get(MessageOptions.METHOD);
            generateMetaField("method", method == null ? "''" : "'" + method + "'");
            String path = (String) options.get(MessageOptions.PATH);
            generateMetaPath(type, path);
        });
        trimDuplicatedNewline();
        indent().append("}) as Webpb.WebpbMeta;\n\n");
    }

    private void generateMetaPath(MessageType type, String path) {
        indent().append("path").append(": ");
        if (StringUtils.isEmpty(path)) {
            builder.append("''\n");
            return;
        }

        builder.append('`');
        ParamGroup group = ParamGroup.of(path).validation(schema, type);
        Iterator<ParamGroup.Param> iterator = group.getParams().iterator();
        while (iterator.hasNext()) {
            ParamGroup.Param param = iterator.next();
            builder.append(param.getPrefix());
            if (StringUtils.isNotEmpty(param.getKey())) {
                if (builder.charAt(builder.length() - 1) == '?') {
                    builder.deleteCharAt(builder.length() - 1);
                }
                builder.append("${Webpb.query({\n");
                level(() -> {
                    indent().append(param.getKey()).append(": ")
                        .append(getter(param.getAccessor())).append(",\n");
                    while (iterator.hasNext()) {
                        ParamGroup.Param p = iterator.next();
                        indent().append(p.getKey()).append(": ")
                            .append(getter(p.getAccessor())).append(",\n");
                    }
                });
                indent().append("})}`\n").append(group.getSuffix());
                return;
            }
            builder.append("${").append(getter(param.getAccessor())).append("}");
        }
        builder.append(group.getSuffix()).append("`\n");
    }

    private String getter(String value) {
        StringBuilder builder = new StringBuilder();
        if (value.contains(".")) {
            builder.append("Webpb.getter(p, '").append(value).append("')");
        } else {
            builder.append("p.").append(value);
        }
        return builder.toString();
    }

    private String generateOmitted(MessageType type) {
        StringBuilder builder = new StringBuilder("[");
        String separator = null;
        for (Field field : type.getFieldsAndOneOfFields()) {
            if ("true".equals(field.getOptions().get(FieldOptions.OMITTED))) {
                if (separator != null) {
                    builder.append(separator);
                }
                builder.append('"').append(field.getName()).append('"');
                separator = ", ";
            }
        }
        return builder.append("]").toString();
    }

    private void generateEnclosingType(EnclosingType type) {
        generateTypes(type.getType().getSimpleName(), type.getNestedTypes());
    }

    private void generateMetaField(String key, String value) {
        indent().append(key).append(": ").append(value == null ? "" : value).append(",\n");
    }

    private String toTypeName(MessageType messageType, ProtoType protoType, FieldContext fieldContext) {
        if (Types.longTypes.containsKey(protoType)) {
            if (fileContext.isTsLong()) {
                return "(number | $protobuf.Long)";
            } else if (fileContext.isTsLongAsString()) {
                return "string";
            } else if (fieldContext != null && fieldContext.isTsString()) {
                return "string";
            }
        }
        if (Types.types.containsKey(protoType)) {
            return Types.types.get(protoType);
        }

        String packageName = fileContext.getProtoFile().getPackageName();
        Type type = this.schema.getType(protoType);
        if (StringUtils.startsWith(protoType.getEnclosingTypeOrPackage(), packageName)) {
            if (type instanceof MessageType) {
                return "I" + protoType.getSimpleName();
            }
            return protoType.getSimpleName();
        }
        if (type instanceof MessageType) {
            return protoType.getEnclosingTypeOrPackage() + ".I" + protoType.getSimpleName();
        } else {
            return protoType.toString();
        }
    }

    private void trimDuplicatedNewline() {
        while (builder.length() > 1) {
            if (builder.charAt(builder.length() - 1) != '\n') {
                break;
            }
            if (builder.charAt(builder.length() - 2) != '\n') {
                break;
            }
            builder.deleteCharAt(builder.length() - 1);
        }
    }

    boolean isEnum(ProtoType type) {
        return schema.getType(type) instanceof EnumType;
    }

    EnumConstant enumDefault(ProtoType type) {
        EnumType wireEnum = (EnumType) schema.getType(type);
        if (wireEnum.getConstants().size() == 0) {
            return null;
        }
        return wireEnum.getConstants().get(0);
    }

    void closeBracket() {
        trimDuplicatedNewline();
        indent().append("}\n\n");
    }

    void level(Handler handler) {
        this.level++;
        handler.handle();
        this.level--;
    }

    StringBuilder indent() {
        for (int i = 0; i < level; i++) {
            builder.append(INDENT);
        }
        return builder;
    }

    String interfaceName(String className) {
        return "I" + className;
    }

    void addImport(ProtoType protoType) {
        if (protoType.isMap()) {
            return;
        }
        imports.add(protoType.getEnclosingTypeOrPackage());
    }

    void setWithLong(boolean withLong) {
        this.withLong = withLong;
    }
}
