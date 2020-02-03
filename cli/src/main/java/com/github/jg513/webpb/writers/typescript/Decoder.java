package com.github.jg513.webpb.writers.typescript;

import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoType;

public class Decoder extends AbstractGenerator {
    public Decoder(TypescriptGenerator generator, StringBuilder builder) {
        super(generator, builder);
    }

    void generateDecode(MessageType type) {
        indent().append("static decode(reader: ($protobuf.Reader | Uint8Array), length?: number): ")
            .append(type.getType())
            .append(" {\n");
        level(() -> {
            indent().append("(reader instanceof $Reader) || (reader = $Reader.create(reader));\n");
            indent().append("let end = length === undefined ? reader.len : reader.pos + length;\n");
            indent().append("let message = new ").append(type.getType()).append("();\n");
            indent().append("while (reader.pos < end) {\n");
            level(() -> {
                indent().append("const tag = reader.uint32();\n");
                if (type.fields().isEmpty()) {
                    indent().append("reader.skipType(tag & 7);\n");
                } else {
                    indent().append("switch (tag >>> 3) {\n");
                    type.fields().forEach(field -> level(() -> generateDecodeField(field)));
                    type.getOneOfs().forEach(oneOf ->
                        oneOf.getFields().forEach(field -> level(() -> generateDecodeField(field)))
                    );
                    level(() -> {
                        indent().append("default:\n");
                        level(() -> {
                            indent().append("reader.skipType(tag & 7);\n");
                            indent().append("break;\n");
                        });
                    });
                    indent().append("}\n");
                }
            });
            indent().append("}\n");
            for (Field field : type.fields()) {
                if (!field.isRequired()) {
                    continue;
                }
                indent().append("if (!message.hasOwnProperty(\"")
                    .append(field.getName()).append("\")) {\n");
                level(() -> indent().append("throw new $util.ProtocolError(\"missing required '")
                    .append(field.getName()).append("'\", { instance: message });\n")
                );
                indent().append("}\n");
            }
            indent().append("return message;\n");
        });
        closeBracket();
    }

    void generateDecodeDelimited(MessageType type) {
        indent().append("static decodeDelimited(reader: ($protobuf.Reader | Uint8Array)): ")
            .append(type.getType()).append(" {\n");
        level(() -> {
            indent().append("(reader instanceof $Reader) || (reader = new $Reader(reader));\n");
            indent().append("return this.decode(reader, reader.uint32());\n");
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
        ProtoType protoType = field.getType();
        assert protoType != null;
        protoType = generator.isEnum(protoType) ? ProtoType.INT32 : protoType;
        Integer wireType = Types.basicTypes.get(protoType);
        if (wireType == null) {
            indent().append("message.").append(field.getName())
                .append(" = ").append(protoType)
                .append(".decode(reader, reader.uint32());\n");
        } else {
            indent().append("message.").append(field.getName())
                .append(" = reader.").append(protoType).append("();\n");
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
        valueType = generator.isEnum(valueType) ? ProtoType.INT32 : valueType;
        assert keyType != null && valueType != null;
        indent().append("const key = reader.").append(keyType.getSimpleName()).append("();\n");
        indent().append("reader.pos++;\n");
        String key = "key";
        if (Types.longTypes.containsKey(keyType)) {
            key = "typeof key === \"object\" ? $util.longToHash(key) : key";
        }
        indent().append("message.").append(field.getName())
            .append("[").append(key).append("] = ");
        Integer wireType = Types.basicTypes.get(valueType);
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
        ProtoType tempType = field.getType();
        ProtoType protoType = generator.isEnum(tempType) ? ProtoType.INT32 : tempType;
        if (Types.packedTypes.containsKey(protoType)) {
            indent().append("if ((tag & 7) === 2) {\n");
            level(() -> {
                indent().append("const end = reader.uint32() + reader.pos;\n");
                indent().append("while (reader.pos < end) {\n");
                level(() -> indent().append("message.")
                    .append(field.getName()).append(".push(reader.")
                    .append(protoType).append("());\n")
                );
                indent().append("}\n");
            });
            indent().append("} else {\n");
            level(() -> {
                Integer wireType = Types.basicTypes.get(protoType);
                if (wireType == null) {
                    indent().append("message.")
                        .append(field.getName()).append(".push(")
                        .append(protoType).append(".decode(reader, reader.uint32()));\n");
                } else {
                    indent().append("message.")
                        .append(field.getName()).append(".push(reader.")
                        .append(protoType).append("());\n");
                }
            });
            indent().append("}\n");
        }
    }
}
