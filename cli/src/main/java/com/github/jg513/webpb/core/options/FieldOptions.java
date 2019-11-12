package com.github.jg513.webpb.core.options;

import com.squareup.wire.schema.ProtoMember;

import static com.squareup.wire.schema.Options.FIELD_OPTIONS;

public class FieldOptions {
    public static final ProtoMember OMITTED = ProtoMember.get(FIELD_OPTIONS, "omitted");

    public static final ProtoMember JAVA_ANNO = ProtoMember.get(FIELD_OPTIONS, "java_anno");
}
