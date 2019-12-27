package com.github.jg513.webpb.core.specs;

import com.squareup.wire.schema.ProtoFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PendingFileSpec implements PendingSpec {
    private final ProtoFile file;
}
