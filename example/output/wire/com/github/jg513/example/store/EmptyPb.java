// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Store.proto
package com.github.jg513.example.store;

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

@Getter
@Setter
@Accessors(chain = true)
public final class EmptyPb extends Message<EmptyPb, EmptyPb.Builder> {

    public static final ProtoAdapter<EmptyPb> ADAPTER = new ProtoAdapter_EmptyPb();

    private static final long serialVersionUID = 0L;

    public EmptyPb() {
        this(ByteString.EMPTY);
    }

    public EmptyPb(ByteString unknownFields) {
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
        if (!(other instanceof EmptyPb)) return false;
        EmptyPb o = (EmptyPb) other;
        return unknownFields().equals(o.unknownFields());
    }

    @Override
    public int hashCode() {
        return unknownFields().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.replace(0, 2, "EmptyPb{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<EmptyPb, Builder> {

        public Builder() {}

        @Override
        public EmptyPb build() {
            return new EmptyPb(super.buildUnknownFields());
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class EnclosingPb extends Message<EnclosingPb, EnclosingPb.Builder> {

        public static final ProtoAdapter<EnclosingPb> ADAPTER = new ProtoAdapter_EnclosingPb();

        private static final long serialVersionUID = 0L;

        public EnclosingPb() {
            this(ByteString.EMPTY);
        }

        public EnclosingPb(ByteString unknownFields) {
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
            if (!(other instanceof EnclosingPb)) return false;
            EnclosingPb o = (EnclosingPb) other;
            return unknownFields().equals(o.unknownFields());
        }

        @Override
        public int hashCode() {
            return unknownFields().hashCode();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            return builder.replace(0, 2, "EnclosingPb{").append('}').toString();
        }

        public static final class Builder extends Message.Builder<EnclosingPb, Builder> {

            public Builder() {}

            @Override
            public EnclosingPb build() {
                return new EnclosingPb(super.buildUnknownFields());
            }
        }

        private static final class ProtoAdapter_EnclosingPb extends ProtoAdapter<EnclosingPb> {

            public ProtoAdapter_EnclosingPb() {
                super(FieldEncoding.LENGTH_DELIMITED, EnclosingPb.class);
            }

            @Override
            public int encodedSize(EnclosingPb value) {
                return value.unknownFields().size();
            }

            @Override
            public void encode(ProtoWriter writer, EnclosingPb value) throws IOException {
                writer.writeBytes(value.unknownFields());
            }

            @Override
            public EnclosingPb decode(ProtoReader reader) throws IOException {
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
            public EnclosingPb redact(EnclosingPb value) {
                Builder builder = value.newBuilder();
                builder.clearUnknownFields();
                return builder.build();
            }
        }
    }

    private static final class ProtoAdapter_EmptyPb extends ProtoAdapter<EmptyPb> {

        public ProtoAdapter_EmptyPb() {
            super(FieldEncoding.LENGTH_DELIMITED, EmptyPb.class);
        }

        @Override
        public int encodedSize(EmptyPb value) {
            return value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, EmptyPb value) throws IOException {
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public EmptyPb decode(ProtoReader reader) throws IOException {
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
        public EmptyPb redact(EmptyPb value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
