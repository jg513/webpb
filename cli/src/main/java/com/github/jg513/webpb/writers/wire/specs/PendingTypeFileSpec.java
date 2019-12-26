package com.github.jg513.webpb.writers.wire.specs;

import com.squareup.wire.schema.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PendingTypeFileSpec implements PendingFileSpec {
    private final Type type;
}
