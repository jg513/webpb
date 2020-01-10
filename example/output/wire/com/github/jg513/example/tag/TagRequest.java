// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Tag.proto
package com.github.jg513.example.tag;

import com.github.jg513.webpb.options.MessageOptions;
import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.util.Arrays;

@Getter
@Setter
@Accessors(chain = true)
public final class TagRequest extends Message<TagRequest, TagRequest.Builder> {

    public static final ProtoAdapter<TagRequest> ADAPTER = new ProtoAdapter_TagRequest();

    private static final long serialVersionUID = 0L;

    public static final MessageOptions MESSAGE_OPTIONS =
            new MessageOptions.Builder()
                    .method("GET")
                    .path("/tag")
                    .tags(Arrays.asList("java", "other"))
                    .build();

    public TagRequest() {
        this(ByteString.EMPTY);
    }

    public TagRequest(ByteString unknownFields) {
        super(ADAPTER, unknownFields);
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof TagRequest)) return false;
        TagRequest o = (TagRequest) other;
        return unknownFields().equals(o.unknownFields());
    }

    @Override
    public int hashCode() {
        return unknownFields().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.replace(0, 2, "TagRequest{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<TagRequest, Builder> {

        public Builder() {}

        @Override
        public TagRequest build() {
            return new TagRequest(super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_TagRequest extends ProtoAdapter<TagRequest> {

        public ProtoAdapter_TagRequest() {
            super(FieldEncoding.LENGTH_DELIMITED, TagRequest.class);
        }

        @Override
        public int encodedSize(TagRequest value) {
            return value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, TagRequest value) throws IOException {
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public TagRequest decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    default:
                        {
                            reader.readUnknownField(tag);
                        }
                }
            }
            builder.addUnknownFields(reader.endMessageAndGetUnknownFields(token));
            return builder.build();
        }

        @Override
        public TagRequest redact(TagRequest value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
