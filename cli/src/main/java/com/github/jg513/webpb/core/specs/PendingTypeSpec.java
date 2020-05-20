package com.github.jg513.webpb.core.specs;

import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PendingTypeSpec implements PendingSpec {

    private final ProtoFile protoFile;

    private final Type type;
}
