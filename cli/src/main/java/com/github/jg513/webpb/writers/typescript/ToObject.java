package com.github.jg513.webpb.writers.typescript;

import com.squareup.wire.schema.EnumConstant;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.OneOf;
import com.squareup.wire.schema.ProtoType;

import java.util.List;
import java.util.stream.Collectors;

public class ToObject extends AbstractGenerator {
    public ToObject(TypescriptGenerator generator, StringBuilder builder) {
        super(generator, builder);
    }

    void generateToObject(MessageType type, String className) {
        indent().append("static toObject(message: ")
            .append(generator.interfaceName(className))
            .append(", options?: $protobuf.IConversionOptions): { [k: string]: any } {\n");
        if (type.fields().isEmpty()) {
            level(() -> indent().append("return {};\n"));
        } else {
            level(() -> {
                indent().append("options || (options = {});\n");
                indent().append("let obj : { [k: string]: any } = {};\n");
                initArrays(type);
                initObjects(type);
                initDefaults(type);
                generateFields(type);
                type.getOneOfs().forEach(oneOf -> generateOneOf(oneOf));
                indent().append("return obj;\n");
            });
        }
        closeBracket();
    }

    void generateToJSON(MessageType type, String className) {
        indent().append("toJSON(): { [k: string]: any } {\n");
        level(() -> indent().append("return ").append(type.getType())
            .append(".toObject(this, $protobuf.util.toJSONOptions);\n"));
        closeBracket();
    }

    private void initArrays(MessageType type) {
        List<Field> fields = type.fields().stream()
            .filter(Field::isRepeated)
            .collect(Collectors.toList());
        if (fields.isEmpty()) {
            return;
        }
        indent().append("if (options.arrays || options.defaults) {\n");
        level(() -> fields.forEach(field ->
            indent().append("obj.").append(field.getName()).append(" = [];\n")
        ));
        indent().append("}\n");
    }

    private void initObjects(MessageType type) {
        List<Field> fields = type.fields().stream()
            .filter(field -> {
                ProtoType protoType = field.getType();
                return protoType != null && protoType.isMap();
            })
            .collect(Collectors.toList());
        if (fields.isEmpty()) {
            return;
        }
        indent().append("if (options.objects || options.defaults) {\n");
        level(() -> fields.forEach(field ->
            indent().append("obj.").append(field.getName()).append(" = {};\n")
        ));
        indent().append("}\n");
    }

    private void initDefaults(MessageType type) {
        List<Field> fields = type.fields().stream()
            .filter(field -> {
                ProtoType protoType = field.getType();
                return !(field.isRepeated() || protoType == null || protoType.isMap());
            })
            .collect(Collectors.toList());
        if (fields.isEmpty()) {
            return;
        }
        indent().append("if (options.defaults) {\n");
        level(() -> fields.forEach(this::getTypeDefault));
        indent().append("}\n");
    }

    private void getTypeDefault(Field field) {
        String defaultValue = field.getDefault();
        ProtoType protoType = field.getType();
        assert protoType != null;
        if (Types.longTypes.containsKey(protoType)) {
            indent().append("if ($util.Long) {\n");
            long value = defaultValue == null ? 0 : Long.decode(defaultValue);
            level(() -> {
                indent().append("const long = new $util.Long(")
                    .append((int) value).append(", ")
                    .append(value >> 32).append(", ")
                    .append(protoType == ProtoType.UINT64).append(");\n");
                indent().append("obj.max = options.longs === String ? ")
                    .append("long.toString() : options.longs === Number ")
                    .append("? long.toNumber() : long;\n");
            });
            indent().append("} else {\n");
            level(() ->
                indent().append("obj.max = options.longs === String ? \"")
                    .append(value).append("\" : ").append(value).append(";\n")
            );
            indent().append("}\n");
            return;
        }
        String value = "null";
        if (generator.isEnum(protoType)) {
            EnumConstant constant = generator.enumDefault(protoType);
            if (constant == null) {
                value = "undefined";
            } else {
                value = "options.enums === String ? \"" + constant.getName() + "\" : 0";
            }
        } else if (ProtoType.BOOL.equals(protoType)) {
            value = "false";
        } else if (ProtoType.BYTES.equals(protoType)) {
            value = "options.bytes === String ? \"\" : options.bytes !== Array " +
                "? $util.newBuffer(obj." + field.getName() + ") : []";
        } else if (ProtoType.STRING.equals(protoType)) {
            value = "\"\"";
        } else if (protoType.isScalar()) {
            value = "0";
        }
        indent().append("obj.").append(field.getName())
            .append(" = ").append(value).append(";\n");
    }

    private void generateFields(MessageType type) {
        List<Field> fields = type.fields().stream().filter(field -> {
            ProtoType protoType = field.getType();
            return protoType != null && protoType.isMap();
        }).collect(Collectors.toList());
        if (!fields.isEmpty()) {
            indent().append("let keys: string[];\n");
        }
        type.fields().forEach(this::generateField);
    }

    private void generateField(Field field) {
        ProtoType protoType = field.getType();
        assert protoType != null;
        if (protoType.isMap()) {
            indent().append("if (message.").append(field.getName())
                .append(" && (keys = Object.keys(message.")
                .append(field.getName()).append(")).length) {\n");
            level(() -> {
                indent().append("obj.").append(field.getName()).append(" = {};\n");
                indent().append("for (let i = 0; i < keys.length; ++i) {\n");
                ProtoType valueType = protoType.getValueType();
                assert valueType != null;
                level(() -> valueToObject(field, valueType, field.getName() + "[keys[i]]"));
                indent().append("}\n");
            });
            indent().append("}\n");
        } else if (field.isRepeated()) {
            indent().append("if (message.")
                .append(field.getName()).append(" && message.")
                .append(field.getName()).append(".length) {\n");
            level(() -> {
                indent().append("obj.").append(field.getName()).append(" = [];\n");
                indent().append("for (let i = 0; i < message.")
                    .append(field.getName()).append(".length; ++i) {\n");
                level(() -> valueToObject(field, protoType, field.getName() + "[i]"));
                indent().append("}\n");
            });
            indent().append("}\n");
        } else {
            indent().append("if (message.")
                .append(field.getName()).append(" != null && message.hasOwnProperty(\"")
                .append(field.getName()).append("\")) {\n");
            level(() -> valueToObject(field, protoType, field.getName()));
            indent().append("}\n");
        }
    }

    private void valueToObject(Field field, ProtoType valueType, String prop) {
        if (generator.isEnum(valueType)) {
            indent()
                .append("obj.").append(prop)
                .append(" = options.enums === String ? ")
                .append(valueType).append("[message.")
                .append(prop).append("] : message.")
                .append(prop).append(";\n");
        } else if (valueType.isScalar()) {
            if (ProtoType.DOUBLE.equals(valueType) || ProtoType.FLOAT.equals(valueType)) {
                indent().append("obj.").append(prop)
                    .append(" = options.json && !isFinite(message.")
                    .append(prop).append(") ? String(message.")
                    .append(prop).append(") : message.")
                    .append(prop).append(";\n");
            } else if (ProtoType.BYTES.equals(valueType)) {
                indent().append("obj.").append(prop)
                    .append(" = options.bytes === String ? $util.base64.encode(message.")
                    .append(prop).append(", 0, message.")
                    .append(prop).append(".length) : options.bytes === Array ? Array.prototype.slice.call(message.")
                    .append(prop).append(") : message.")
                    .append(prop).append(";\n");
            } else if (Types.longTypes.containsKey(valueType)) {
                indent().append("if (typeof message.")
                    .append(prop).append(" === \"number\") {\n");
                level(() -> indent().append("obj.").append(prop)
                    .append(" = options.longs === String ? String(message.")
                    .append(prop).append(") : message.")
                    .append(prop).append(";\n"));
                indent().append("} else {\n");
                level(() -> indent().append("obj.").append(prop)
                    .append(" = options.longs === String ? $util.Long.prototype.toString.call(message.")
                    .append(prop).append(") : options.longs === Number ? new $util.LongBits(message.")
                    .append(prop).append(".low >>> 0, message.")
                    .append(prop).append(".high >>> 0).toNumber() : message.")
                    .append(prop).append(";\n"));
                indent().append("}\n");
            } else {
                indent().append("obj.")
                    .append(prop).append(" = message.")
                    .append(prop).append(";\n");
            }
        } else {
            indent().append("obj.").append(prop)
                .append(" = ").append(valueType)
                .append(".toObject(message.")
                .append(prop).append(", options);\n");
        }
    }

    private void generateOneOf(OneOf oneOf) {
        oneOf.getFields().forEach(field -> {
            ProtoType protoType = field.getType();
            assert protoType != null;
            indent().append("if (message.")
                .append(field.getName()).append(" != null && message.hasOwnProperty(\"")
                .append(field.getName()).append("\")) {\n");
            level(() -> {
                valueToObject(field, protoType, field.getName());
                indent().append("options.oneofs && (obj.").append(oneOf.getName())
                    .append(" = \"").append(field.getName()).append("\");\n");
            });
            indent().append("}\n");
        });
    }
}
