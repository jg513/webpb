package com.github.jg513.webpb.core.options;

import com.squareup.wire.schema.ProtoMember;

import static com.squareup.wire.schema.Options.MESSAGE_OPTIONS;

public class MessageOptions {

    public static final ProtoMember METHOD = ProtoMember.get(MESSAGE_OPTIONS, "method");

    public static final ProtoMember PATH = ProtoMember.get(MESSAGE_OPTIONS, "path");

    public static final ProtoMember JAVA_ANNO = ProtoMember.get(MESSAGE_OPTIONS, "java_anno");

    public static final ProtoMember TAG = ProtoMember.get(MESSAGE_OPTIONS, "tag");

    public static final ProtoMember GETTER = ProtoMember.get(MESSAGE_OPTIONS, "getter");

    public static final ProtoMember SETTER = ProtoMember.get(MESSAGE_OPTIONS, "setter");
}
