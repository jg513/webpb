package com.github.jg513.webpb.common.specs;

import com.squareup.wire.schema.Type;
import com.github.jg513.webpb.common.PendingSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PendingTypeSpec extends PendingSpec {
    private final Type type;
}
