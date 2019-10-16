package com.github.jg513.webpb.writers.typescript;

import com.google.common.collect.ImmutableMap;
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
import com.github.jg513.webpb.common.Const;
import com.github.jg513.webpb.common.Handler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(staticName = "of")
final class TypescriptGenerator {
    private static final String INDENT = "    ";

    private final Schema schema;

    private final StringBuilder builder;

    private Set<String> imports = new HashSet<>();

    private int level = 0;

    private static final Map<ProtoType, String> typesMap =
        ImmutableMap.<ProtoType, String>builder()
            .put(ProtoType.BOOL, "boolean")
            .put(ProtoType.BYTES, "Uint8Array")
            .put(ProtoType.DOUBLE, "number")
            .put(ProtoType.FLOAT, "number")
            .put(ProtoType.FIXED32, "number")
            .put(ProtoType.FIXED64, "number")
            .put(ProtoType.INT32, "number")
            .put(ProtoType.INT64, "number")
            .put(ProtoType.SFIXED32, "number")
            .put(ProtoType.SFIXED64, "number")
            .put(ProtoType.SINT32, "number")
            .put(ProtoType.SINT64, "number")
            .put(ProtoType.STRING, "string")
            .put(ProtoType.UINT32, "number")
            .put(ProtoType.UINT64, "number")
            .build();

    public void generate(ProtoFile protoFile) {
        String packageName = protoFile.packageName();
        generateTypes(packageName, protoFile.types());
        for (String type : imports) {
            if (!packageName.equals(type)) {
                builder.insert(0, "import { " + type + " } from './" + type + "';\n\n");
            }
        }
        builder.insert(0, "import { Webpb } from 'Webpb';\n\n");
        builder.insert(0, "// " + Const.GIT_URL + "\n\n");
        builder.insert(0, "// " + Const.HEADER + "\n");
    }

    private void generateTypes(String namespace, List<Type> types) {
        indent().append("export namespace ").append(namespace).append(" {\n");
        for (Type type : types) {
            generateType(type);
            if (!type.nestedTypes().isEmpty()) {
                level(() -> generateTypes(type.type().simpleName(), type.nestedTypes()));
            }
        }
        closeBracket();
        if (level == 0) {
            trimDuplicatedNewline();
        }
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
        level(() -> {
            indent().append("export enum ").append(type.type().simpleName()).append(" {\n");

            for (EnumConstant constant : type.constants()) {
                level(() -> indent().append(constant.getName())
                    .append(" = ").append(constant.getTag()).append(",\n")
                );
            }
            closeBracket();
        });
    }

    private void generateMessage(MessageType type) {
        String className = type.type().simpleName();

        level(() -> {
            indent().append("export interface ").append(interfaceName(className)).append(" {\n");
            level(() -> generateMessageFields(type, true));
            closeBracket();
        });

        level(() -> {
            indent()
                .append("export class ").append(className)
                .append(" implements ").append(interfaceName(className));
            if (type.options().get(Const.PATH) != null) {
                builder.append(", Webpb.WebpbMessage {\n");
            } else {
                builder.append(" {\n");
            }

            level(() -> {
                generateMessageFields(type, false);
                indent().append("META: () => Webpb.WebpbMeta;\n\n");
                generateConstructor(type, className);
            });
            closeBracket();
        });
    }

    private void generateMessageFields(MessageType type, boolean isInterface) {
        for (Field field : type.fieldsAndOneOfFields()) {
            if (!field.type().isScalar()) {
                imports.add(field.type().enclosingTypeOrPackage());
            }
            String typeName = toTypeName(field.type());
            indent().append(field.name());
            if (field.isOptional()) {
                builder.append('?');
            } else if (!isInterface) {
                builder.append('!');
            }
            builder.append(": ").append(typeName);
            if (field.isRepeated()) {
                builder.append("[]");
            }
            builder.append(";\n");
        }
    }

    private void generateConstructor(MessageType type, String className) {
        if (type.fieldsAndOneOfFields().isEmpty()) {
            indent().append("private constructor() {\n");
            level(() -> initializeMeta(type));
            closeBracket();
            indent().append("static create(): ").append(className).append(" {\n");
            level(() -> indent().append("return new ").append(className).append("();\n"));
            closeBracket();
        } else {
            String interfaceName = interfaceName(className);
            indent().append("private constructor(p: ").append(interfaceName).append(") {\n");
            level(() -> {
                indent().append("Webpb.assign(p, this, ").append(generateOmitted(type)).append(");\n");
                initializeMeta(type);
            });
            closeBracket();
            indent().append("static create(properties: ")
                .append(interfaceName).append("): ").append(className).append(" {\n");
            level(() -> indent().append("return new ").append(type.type()
                .simpleName()).append("(properties").append(");\n"));
            closeBracket();
        }
    }

    private void initializeMeta(MessageType type) {
        indent().append("this.META = () => ({\n");
        level(() -> {
            generateMetaField("class", "'" + type.type().simpleName() + "'");
            Options options = type.options();
            String method = (String) options.get(Const.METHOD);
            generateMetaField("method", method == null ? "''" : "'" + method + "'");
            String path = (String) options.get(Const.PATH);
            generateMetaPath(path);
        });
        trimDuplicatedNewline();
        indent().append("});\n\n");
    }

    private void generateMetaPath(String path) {
        indent().append("path").append(": ");
        if (StringUtils.isEmpty(path)) {
            builder.append("''\n");
            return;
        }
        Pattern pattern = Pattern.compile("((?<key>\\w+)=)?\\{(?<path>[\\w.]+)}&?");
        Matcher matcher = pattern.matcher(path);

        AtomicInteger index = new AtomicInteger();
        builder.append('`');
        boolean hasQuery = false;
        while (matcher.find()) {
            builder.append(path, index.get(), matcher.start());
            String key = matcher.group("key");
            String value = matcher.group("path");
            if (StringUtils.isNotEmpty(key)) {
                if (builder.charAt(builder.length() - 1) == '?') {
                    builder.deleteCharAt(builder.length() - 1);
                }
                builder.append("${Webpb.query({\n");
                level(() -> {
                    do {
                        String levelKey = matcher.group("key");
                        String levelValue = matcher.group("path");
                        indent().append(levelKey).append(": ").append(getter(levelValue)).append(",\n");
                        index.set(matcher.end());
                    } while (matcher.find());
                });
                indent().append("})}`\n");
                hasQuery = true;
                break;
            }
            builder.append("${").append(getter(value)).append("}");
            index.set(matcher.end());
        }
        if (index.get() < path.length() - 1) {
            builder.append(path, index.get(), path.length());
        }
        if (!hasQuery) {
            builder.append("`\n");
        }
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
        for (Field field : type.fieldsAndOneOfFields()) {
            if ("true".equals(field.options().get(Const.OMITTED))) {
                if (separator != null) {
                    builder.append(separator);
                }
                builder.append('"').append(field.name()).append('"');
                separator = ", ";
            }
        }
        return builder.append("]").toString();
    }

    private void generateEnclosingType(EnclosingType type) {
        level(() -> generateTypes(type.type().simpleName(), type.nestedTypes()));
    }

    private void generateMetaField(String key, String value) {
        indent().append(key).append(": ").append(value == null ? "" : value).append(",\n");
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

    private void closeBracket() {
        trimDuplicatedNewline();
        indent().append("}\n\n");
    }

    private String toTypeName(ProtoType protoType) {
        if (typesMap.containsKey(protoType)) {
            return typesMap.get(protoType);
        }

        Type type = this.schema.getType(protoType);
        if (type instanceof MessageType) {
            return protoType.enclosingTypeOrPackage() + ".I" + protoType.simpleName();
        } else {
            return protoType.toString();
        }
    }

    private void level(Handler handler) {
        this.level++;
        handler.handle();
        this.level--;
    }

    private StringBuilder indent() {
        for (int i = 0; i < level; i++) {
            builder.append(INDENT);
        }
        return builder;
    }

    private String interfaceName(String className) {
        return "I" + className;
    }
}
