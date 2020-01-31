package com.github.jg513.webpb.writers.typescript;

import com.github.jg513.webpb.core.Handler;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Decoder {
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
        indent().append("static decode(reader: ($protobuf.Reader | Uint8Array), length?: number): ")
            .append(type.getType())
            .append(" {\n");
        level(() -> {
            indent().append("if (!(reader instanceof $Reader)) {\n");
            level(() -> indent().append("reader = $Reader.create(reader);\n"));
            indent().append("}\n");
            indent().append("let end = length === undefined ? reader.len : reader.pos + length;\n");
            indent().append("let message = new ").append(type.getType()).append("();\n");
            indent().append("while (reader.pos < end) {\n");
            level(() -> {
                indent().append("const tag = reader.uint32();\n");
                indent().append("switch (tag >>> 3) {\n");
                type.fields().forEach(field -> level(() -> generateDecodeField(field)));
                level(() -> {
                    indent().append("default:\n");
                    level(() -> {
                        indent().append("reader.skipType(tag & 7);\n");
                        indent().append("break;\n");
                    });
                });
                indent().append("}\n");
                for (Field field : type.fields()) {
                    if (!field.isRequired()) {
                        continue;
                    }
                    indent().append("if (!message.hasOwnProperty(\"")
                        .append(field.getName()).append("\")) {\n");
                    level(() -> indent().append("throw $util.ProtocolError(\"missing required '")
                        .append(field.getName()).append("'\", { instance: message });\n")
                    );
                    indent().append("}\n");
                }
            });
            indent().append("}\n");
            indent().append("return message;\n");
        });
        closeBracket();
    }

    private void generateDecodeField(Field field) {
        indent().append("case ").append(field.getTag()).append(": {\n");
        level(() -> {
            ProtoType protoType = field.getType();
            if (protoType != null && protoType.isMap()) {
                generateDecodeMapField(field);
            } else if (field.isRepeated()) {
                generateDecodeRepeatedField(field);
            } else {
                generateDecodeDefaultField(field);
            }
            indent().append("break;\n");
        });
        indent().append("}\n");
    }

    private void generateDecodeDefaultField(Field field) {
        assert field.getType() != null;
        Integer wireType = TypescriptGenerator.basicTypes.get(field.getType());
        if (wireType == null) {
            indent().append("message.").append(field.getName())
                .append(" = ").append(field.getType())
                .append(".decode(reader, reader.uint32());\n");
        } else {
            indent().append("message.").append(field.getName())
                .append(" = reader.").append(field.getType()).append("();\n");
        }
    }

    private void generateDecodeMapField(Field field) {
        ProtoType protoType = field.getType();
        assert protoType != null;
        indent().append("reader.skip().pos++;\n");
        indent().append("if (message.")
            .append(field.getName()).append(" === $util.emptyObject) {\n");
        level(() -> indent().append("message.").append(field.getName()).append(" = {};\n"));
        indent().append("}\n");
        ProtoType keyType = protoType.getKeyType();
        ProtoType valueType = protoType.getValueType();
        assert keyType != null && valueType != null;
        indent().append("const key = reader.").append(keyType.getSimpleName()).append("();\n");
        indent().append("reader.pos++;\n");
        String key = "key";
        if (TypescriptGenerator.longTypes.containsKey(keyType)) {
            key = "typeof key === \"object\" ? $util.longToHash(key) : key";
        }
        indent().append("message.").append(field.getName())
            .append("[").append(key).append("] = ");
        Integer wireType = TypescriptGenerator.basicTypes.get(valueType);
        if (wireType == null) {
            builder.append(valueType).append(".decode(reader, reader.uint32());\n");
        } else {
            builder.append("reader.").append(valueType.getSimpleName()).append("();\n");
        }
    }

    private void generateDecodeRepeatedField(Field field) {
        indent().append("if (!(message.").append(field.getName())
            .append(" && message.").append(field.getName()).append(".length)) {\n");
        level(() -> indent().append("message.").append(field.getName()).append(" = [];\n"));
        indent().append("}\n");
        if (TypescriptGenerator.packedTypes.containsKey(field.getType())) {
            indent().append("if ((tag & 7) === 2) {\n");
            level(() -> {
                indent().append("const end = reader.uint32() + reader.pos;\n");
                indent().append("while (reader.pos < end) {\n");
                level(() -> indent().append("message.")
                    .append(field.getName()).append(".push(reader.")
                    .append(field.getType()).append("());\n")
                );
                indent().append("}\n");
            });
            indent().append("} else {\n");
            level(() -> {
                Integer wireType = TypescriptGenerator.basicTypes.get(field.getType());
                if (wireType == null) {
                    indent().append("message.")
                        .append(field.getName()).append(".push(")
                        .append(field.getType()).append(".decode(reader, reader.uint32()));\n");
                } else {
                    indent().append("message.")
                        .append(field.getName()).append(".push(reader.")
                        .append(field.getType()).append("());\n");
                }
            });
            indent().append("}\n");
        }
    }
}
