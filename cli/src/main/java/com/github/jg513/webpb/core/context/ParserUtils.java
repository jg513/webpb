package com.github.jg513.webpb.core.context;

import com.squareup.wire.schema.Options;
import com.squareup.wire.schema.ProtoMember;

import java.util.List;
import java.util.Optional;

public class ParserUtils {
    public static Optional<String> get(Options options, ProtoMember member) {
        return Optional.ofNullable((String) options.get(member));
    }

    @SuppressWarnings("unchecked")
    public static Optional<List<String>> getList(Options options, ProtoMember member) {
        return Optional.ofNullable((List<String>) options.get(member));
    }
}
