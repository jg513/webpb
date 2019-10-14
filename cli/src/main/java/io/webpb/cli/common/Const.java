package io.webpb.cli.common;

import com.squareup.wire.schema.ProtoMember;

import static com.squareup.wire.schema.Options.ENUM_VALUE_OPTIONS;
import static com.squareup.wire.schema.Options.FIELD_OPTIONS;
import static com.squareup.wire.schema.Options.MESSAGE_OPTIONS;

public class Const {
    public static final ProtoMember ENUM_DEPRECATED = ProtoMember.get(ENUM_VALUE_OPTIONS, "deprecated");

    public static final ProtoMember METHOD = ProtoMember.get(MESSAGE_OPTIONS, "method");

    public static final ProtoMember PATH = ProtoMember.get(MESSAGE_OPTIONS, "path");

    public static final ProtoMember OMITTED = ProtoMember.get(FIELD_OPTIONS, "omitted");
}
