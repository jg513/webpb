package com.github.jg513.webpb.core.specs;

import com.squareup.wire.schema.ProtoFile;
import com.github.jg513.webpb.core.PendingSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PendingFileSpec extends PendingSpec {
    private final ProtoFile file;
}
