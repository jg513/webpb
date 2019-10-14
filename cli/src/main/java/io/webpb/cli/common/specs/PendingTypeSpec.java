package io.webpb.cli.common.specs;

import com.squareup.wire.schema.Type;
import io.webpb.cli.common.PendingSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PendingTypeSpec extends PendingSpec {
    private final Type type;
}
