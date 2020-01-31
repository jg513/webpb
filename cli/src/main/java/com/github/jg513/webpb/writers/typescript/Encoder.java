package com.github.jg513.webpb.writers.typescript;

import com.github.jg513.webpb.core.Handler;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Encoder {
    private final TypescriptGenerator generator;

    private final StringBuilder builder;

    private void level(Handler handler) {
        generator.level(handler);
    }

    private StringBuilder indent() {
        return generator.indent();
    }

    private void closeBracket() {
        generator.closeBracket();
    }

    void generateEncode(MessageType type, String className) {
        String interfaceName = generator.interfaceName(className);
        indent().append("static encode(message: ")
            .append(interfaceName)
            .append(", writer?: $protobuf.Writer")
            .append("): $protobuf.Writer").append(" {\n");
        level(() -> {
            indent().append("if (!writer) {\n");
            level(() -> indent().append("writer = $Writer.create();\n"));
            indent().append("}\n");
            type.fields().forEach(this::generateEncodeField);
            indent().append("return writer;\n");
        });
        closeBracket();
    }

    private void generateEncodeField(Field field) {
        ProtoType protoType = field.getType();
        if (protoType != null && protoType.isMap()) {
            generateEncodeMapField(field);
        } else if (field.isRepeated()) {
            generateEncodeRepeatedField(field);
        } else {
            if (field.isOptional()) {
                indent().append("if (message.").append(field.getName())
                    .append(" != null && message.hasOwnProperty(\"")
                    .append(field.getName()).append("\")) {\n");
                level(() -> generateEncodeDefaultField(field));
                closeBracket();
            } else {
                generateEncodeDefaultField(field);
            }
        }
    }

    private void generateEncodeDefaultField(Field field) {
        assert field.getType() != null;
        Integer wireType = TypescriptGenerator.basicTypes.get(field.getType());
        if (wireType == null) {
            genTypePartial(field, field.getName());
        } else {
            indent().append("writer.uint32(").append(field.getTag() << 3 | wireType)
                .append(").").append(field.getType().getSimpleName())
                .append("(message.").append(field.getName()).append(");\n");
        }
    }

    private void generateEncodeMapField(Field field) {
        ProtoType protoType = field.getType();
        assert protoType != null;
        indent().append("if (message.").append(field.getName())
            .append(" != null && message.hasOwnProperty(\"").append(field.getName())
            .append("\")) {\n");
        level(() -> {
            indent().append("for (let keys = Object.keys(message.")
                .append(field.getName()).append("), i = 0; i < keys.length; ++i) {\n");
            level(() -> {
                String keyType = "";
                if (protoType.getKeyType() != null) {
                    keyType = protoType.getKeyType().getSimpleName();
                }
                indent().append("writer.uint32(").append((field.getTag() << 3 | 2))
                    .append(").fork().uint32(")
                    .append(8 | TypescriptGenerator.mapKeyTypes.get(protoType.getKeyType()))
                    .append(").").append(keyType.toLowerCase())
                    .append("(keys[i])");
                ProtoType valueType = protoType.getValueType();
                Integer wireType = TypescriptGenerator.basicTypes.get(valueType);
                assert valueType != null;
                if (wireType == null) {
                    builder.append(";\n");
                    indent().append(valueType)
                        .append(".encode(message.").append(field.getName()).append("[keys[i]], ")
                        .append("writer.").append(valueType.getSimpleName().toLowerCase())
                        .append("(18).fork()).ldelim().ldelim();\n");
                } else {
                    builder.append(".uint32(").append(16 | wireType).append(").")
                        .append(valueType.getSimpleName().toLowerCase())
                        .append("(message.").append(field.getName()).append("[keys[i]]).ldelim();\n");
                }
            });
            closeBracket();
        });
        closeBracket();
    }

    private void generateEncodeRepeatedField(Field field) {
        indent().append("if (message.").append(field.getName())
            .append(" != null && message.").append(field.getName()).append(".length) {\n");
        level(() -> {
            indent().append("for (let i = 0; i < message.")
                .append(field.getName()).append(".length; ++i) {\n");
            if (field.isPacked() && TypescriptGenerator.packedTypes.containsKey(field.getType())) {
                level(() -> {
                    indent().append("writer.uint32(")
                        .append((field.getTag() << 3 | 2)).append(").fork();\n");
                    indent().append("for (let i = 0; i < message.")
                        .append(field.getName()).append(".length; ++i) {\n");
                    level(() -> {
                        assert field.getType() != null;
                        indent().append("writer.")
                            .append(field.getType().getSimpleName().toLowerCase())
                            .append("(message.").append(field.getName()).append("[i]);\n");
                    });
                    closeBracket();
                    indent().append("writer.ldelim();\n");
                });
                closeBracket();
            } else {
                assert field.getType() != null;
                Integer wireType = TypescriptGenerator.basicTypes.get(field.getType());
                level(() -> {
                    if (wireType == null) {
                        genTypePartial(field, field.getName() + "[i]");
                    } else {
                        indent().append("writer.uint32(").append(field.getTag() << 3 | wireType)
                            .append(").").append(field.getType().getSimpleName().toLowerCase())
                            .append("(message.").append(field.getName()).append("[i]);\n");
                    }
                });
                closeBracket();
            }
        });
        closeBracket();
    }

    private void genTypePartial(Field field, String ref) {
        indent().append(field.getType()).append(".encode(message.")
            .append(ref).append(", writer.uint32(")
            .append(field.getTag() << 3 | 2).append(").fork()).ldelim();\n");
    }
}
