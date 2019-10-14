package io.webpb.cli.common.specs;

import com.squareup.wire.schema.ProtoFile;
import io.webpb.cli.common.PendingSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PendingFileSpec extends PendingSpec {
    private final ProtoFile file;
}
