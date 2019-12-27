package com.github.jg513.webpb.core.specs;

import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PendingServiceSpec implements PendingSpec {
    private final ProtoFile file;

    private final Service service;
}
