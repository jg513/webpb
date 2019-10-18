package com.github.jg513.webpb.common.options;

import com.squareup.wire.schema.ProtoMember;

import static com.squareup.wire.schema.Options.FILE_OPTIONS;

public class FileOptions {
    public static final ProtoMember JAVA_IMPORT = ProtoMember.get(FILE_OPTIONS, "java_import");

    public static final ProtoMember JAVA_MESSAGE_ANNO = ProtoMember.get(FILE_OPTIONS, "java_message_anno");

    public static final ProtoMember JAVA_GETTER = ProtoMember.get(FILE_OPTIONS, "java_getter");

    public static final ProtoMember JAVA_SETTER = ProtoMember.get(FILE_OPTIONS, "java_setter");
}
