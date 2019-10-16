package com.github.jg513.webpb.common;

import com.github.jg513.webpb.log.Logger;
import com.squareup.wire.schema.Schema;
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
