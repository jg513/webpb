package com.github.jg513.webpb.core.specs;

import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Service;
import com.github.jg513.webpb.core.PendingSpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PendingServiceSpec extends PendingSpec {
    private final ProtoFile file;

    private final Service service;
}
