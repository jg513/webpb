package com.github.jg513.webpb.writers.typescript;

import com.squareup.wire.schema.ProtoType;

import java.util.HashMap;
import java.util.Map;

public class Types {
    static final Map<ProtoType, String> types = new HashMap<ProtoType, String>() {{
        put(ProtoType.BOOL, "boolean");
        put(ProtoType.BYTES, "Uint8Array");
        put(ProtoType.DOUBLE, "number");
        put(ProtoType.FLOAT, "number");
        put(ProtoType.FIXED32, "number");
        put(ProtoType.FIXED64, "number");
        put(ProtoType.INT32, "number");
        put(ProtoType.INT64, "number");
        put(ProtoType.SFIXED32, "number");
        put(ProtoType.SFIXED64, "number");
        put(ProtoType.SINT32, "number");
        put(ProtoType.SINT64, "number");
        put(ProtoType.STRING, "string");
        put(ProtoType.UINT32, "number");
        put(ProtoType.UINT64, "number");
    }};

    static Map<ProtoType, Integer> mapKeyTypes = new HashMap<ProtoType, Integer>() {{
        put(ProtoType.INT32, 0);
        put(ProtoType.UINT32, 0);
        put(ProtoType.SINT32, 0);
        put(ProtoType.FIXED32, 5);
        put(ProtoType.SFIXED32, 5);
        put(ProtoType.INT64, 0);
        put(ProtoType.UINT64, 0);
        put(ProtoType.SINT64, 0);
        put(ProtoType.FIXED64, 1);
        put(ProtoType.SFIXED64, 1);
        put(ProtoType.BOOL, 0);
        put(ProtoType.STRING, 2);
    }};

    static Map<ProtoType, Integer> basicTypes = new HashMap<ProtoType, Integer>() {{
        put(ProtoType.DOUBLE, 1);
        put(ProtoType.FLOAT, 5);
        put(ProtoType.INT32, 0);
        put(ProtoType.UINT32, 0);
        put(ProtoType.SINT32, 0);
        put(ProtoType.FIXED32, 5);
        put(ProtoType.SFIXED32, 5);
        put(ProtoType.INT64, 0);
        put(ProtoType.UINT64, 0);
        put(ProtoType.SINT64, 0);
        put(ProtoType.FIXED64, 1);
        put(ProtoType.SFIXED64, 1);
        put(ProtoType.BOOL, 0);
        put(ProtoType.STRING, 2);
        put(ProtoType.BYTES, 2);
    }};

    static Map<ProtoType, Integer> packedTypes = new HashMap<ProtoType, Integer>() {{
        put(ProtoType.DOUBLE, 1);
        put(ProtoType.FLOAT, 5);
        put(ProtoType.INT32, 0);
        put(ProtoType.UINT32, 0);
        put(ProtoType.SINT32, 0);
        put(ProtoType.FIXED32, 5);
        put(ProtoType.SFIXED32, 5);
        put(ProtoType.INT64, 0);
        put(ProtoType.UINT64, 0);
        put(ProtoType.SINT64, 0);
        put(ProtoType.FIXED64, 1);
        put(ProtoType.SFIXED64, 1);
        put(ProtoType.BOOL, 0);
    }};

    static Map<ProtoType, Integer> longTypes = new HashMap<ProtoType, Integer>() {{
        put(ProtoType.INT64, 0);
        put(ProtoType.UINT64, 0);
        put(ProtoType.SINT64, 0);
        put(ProtoType.FIXED64, 1);
        put(ProtoType.SFIXED64, 1);
    }};
}
