package io.webpb.cli.common;

import com.squareup.wire.schema.Schema;
import io.webpb.cli.log.Logger;
import lombok.Builder;
import lombok.Getter;

import java.util.AbstractQueue;

@Getter
@Builder
public class CodeWriterContext {
    private Logger log;

    private String out;

    private Schema schema;

    private AbstractQueue<PendingSpec> specs;
}
