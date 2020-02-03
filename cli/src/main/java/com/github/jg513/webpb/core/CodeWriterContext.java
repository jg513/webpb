package com.github.jg513.webpb.core;

import com.github.jg513.webpb.core.context.SchemaContext;
import com.github.jg513.webpb.core.specs.PendingSpec;
import com.github.jg513.webpb.log.Logger;
import com.squareup.wire.schema.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.AbstractQueue;
import java.util.List;

@Getter
@Builder
public class CodeWriterContext {
    private Logger log;

    private String out;

    private Schema schema;

    private SchemaContext schemaContext;

    private AbstractQueue<PendingSpec> specs;

    private List<String> tags;
}
