package com.github.jg513.webpb.writers.typescript;

import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoType;

public class Encoder extends AbstractGenerator {
    public Encoder(TypescriptGenerator generator, StringBuilder builder) {
        super(generator, builder);
    }

    void generateEncode(MessageType type, String className) {
        String interfaceName = generator.interfaceName(className);
        indent().append("static encode(message: ")
            .append(interfaceName)
            .append(", writer?: $protobuf.Writer")
            .append("): $protobuf.Writer").append(" {\n");
        level(() -> {
            indent().append("writer || (writer = $Writer.create());\n");
            type.fields().forEach(field -> generateEncodeField(field, field.isOptional()));
            type.getOneOfs().forEach(oneOf ->
                oneOf.getFields().forEach(field -> generateEncodeField(field, true))
            );
            indent().append("return writer;\n");
        });
        closeBracket();
    }

    void generateEncodeDelimited(MessageType type, String className) {
        String interfaceName = generator.interfaceName(className);
        indent().append("static encodeDelimited(message: ")
            .append(interfaceName)
            .append(", writer?: $protobuf.Writer")
            .append("): $protobuf.Writer").append(" {\n");
        level(() -> indent().append("return this.encode(message, writer).ldelim();\n"));
        closeBracket();
    }

    private void generateEncodeField(Field field, boolean optional) {
        ProtoType protoType = field.getType();
        if (protoType != null && protoType.isMap()) {
            generateEncodeMapField(field);
        } else if (field.isRepeated()) {
            generateEncodeRepeatedField(field);
        } else {
            if (optional) {
                indent().append("if (message.").append(field.getName())
                    .append(" != null && message.hasOwnProperty(\"")
                    .append(field.getName()).append("\")) {\n");
                level(() -> generateEncodeDefaultField(field));
                indent().append("}\n");
            } else {
                generateEncodeDefaultField(field);
            }
        }
    }

    private void generateEncodeDefaultField(Field field) {
        ProtoType protoType = field.getType();
        assert protoType != null;
        protoType = generator.isEnum(protoType) ? ProtoType.INT32 : protoType;
        Integer wireType = Types.basicTypes.get(protoType);
        if (wireType == null) {
            genTypePartial(field, field.getName());
        } else {
            indent().append("writer.uint32(").append(field.getTag() << 3 | wireType)
                .append(").").append(protoType.getSimpleName())
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
                    .append(8 | Types.mapKeyTypes.get(protoType.getKeyType()))
                    .append(").").append(keyType.toLowerCase())
                    .append("(keys[i])");
                ProtoType valueType = protoType.getValueType();
                valueType = generator.isEnum(valueType) ? ProtoType.INT32 : valueType;
                Integer wireType = Types.basicTypes.get(valueType);
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
            indent().append("}\n");
        });
        indent().append("}\n");
    }

    private void generateEncodeRepeatedField(Field field) {
        indent().append("if (message.").append(field.getName())
            .append(" != null && message.").append(field.getName()).append(".length) {\n");
        level(() -> {
            ProtoType tempType = field.getType();
            assert tempType != null;
            ProtoType protoType = generator.isEnum(tempType) ? ProtoType.INT32 : tempType;
            indent().append("for (let i = 0; i < message.")
                .append(field.getName()).append(".length; ++i) {\n");
            if (field.isPacked() && Types.packedTypes.containsKey(field.getType())) {
                level(() -> {
                    indent().append("writer.uint32(")
                        .append((field.getTag() << 3 | 2)).append(").fork();\n");
                    indent().append("for (let i = 0; i < message.")
                        .append(field.getName()).append(".length; ++i) {\n");
                    level(() -> {
                        indent().append("writer.")
                            .append(protoType.getSimpleName().toLowerCase())
                            .append("(message.").append(field.getName()).append("[i]);\n");
                    });
                    indent().append("}\n");
                    indent().append("writer.ldelim();\n");
                });
            } else {
                Integer wireType = Types.basicTypes.get(protoType);
                level(() -> {
                    if (wireType == null) {
                        genTypePartial(field, field.getName() + "[i]");
                    } else {
                        indent().append("writer.uint32(").append(field.getTag() << 3 | wireType)
                            .append(").").append(protoType.getSimpleName().toLowerCase())
                            .append("(message.").append(field.getName()).append("[i]);\n");
                    }
                });
            }
            indent().append("}\n");
        });
        indent().append("}\n");
    }

    private void genTypePartial(Field field, String ref) {
        indent().append(field.getType()).append(".encode(message.")
            .append(ref).append(", writer.uint32(")
            .append(field.getTag() << 3 | 2).append(").fork()).ldelim();\n");
    }
}
