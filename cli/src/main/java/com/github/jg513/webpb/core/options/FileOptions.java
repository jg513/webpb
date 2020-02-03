package com.github.jg513.webpb.core.options;

import com.squareup.wire.schema.ProtoMember;

import static com.squareup.wire.schema.Options.FILE_OPTIONS;

public class FileOptions {
    public static final ProtoMember JAVA_IMPORT = ProtoMember.get(FILE_OPTIONS, "java_import");

    public static final ProtoMember JAVA_COMMON_ANNO = ProtoMember.get(FILE_OPTIONS, "java_common_anno");

    public static final ProtoMember JAVA_GETTER = ProtoMember.get(FILE_OPTIONS, "java_getter");

    public static final ProtoMember JAVA_SETTER = ProtoMember.get(FILE_OPTIONS, "java_setter");

    public static final ProtoMember TS_LONG = ProtoMember.get(FILE_OPTIONS, "ts_long");

    public static final ProtoMember TS_JSON = ProtoMember.get(FILE_OPTIONS, "ts_json");

    public static final ProtoMember TS_STREAM = ProtoMember.get(FILE_OPTIONS, "ts_stream");
}
