package io.webpb.cli.common.specs;

import com.squareup.wire.schema.Service;
import io.webpb.cli.common.PendingSpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PendingServiceSpec extends PendingSpec {
    private final Service service;
}
