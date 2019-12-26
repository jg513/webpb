package com.github.jg513.webpb.writers.wire.specs;

import com.squareup.wire.schema.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PendingServiceFileSpec implements PendingFileSpec {
    private final Service service;
}
