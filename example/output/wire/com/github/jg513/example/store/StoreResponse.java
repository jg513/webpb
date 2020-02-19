// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Store.proto
package com.github.jg513.example.store;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
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
public final class StoreResponse extends Message<StoreResponse, StoreResponse.Builder> {

    public static final ProtoAdapter<StoreResponse> ADAPTER = new ProtoAdapter_StoreResponse();

    private static final long serialVersionUID = 0L;

    @WireField(
            tag = 1,
            adapter = "com.github.jg513.example.store.StorePb#ADAPTER",
            label = WireField.Label.REQUIRED)
    private StorePb store;

    @WireField(
            tag = 2,
            adapter = "com.github.jg513.example.store.StoreResponse$StoreNestedPb#ADAPTER",
            label = WireField.Label.REQUIRED)
    private StoreNestedPb nested;

    public StoreResponse() {
        super(ADAPTER, ByteString.EMPTY);
    }

    public StoreResponse(StorePb store, StoreNestedPb nested) {
        this(store, nested, ByteString.EMPTY);
    }

    public StoreResponse(StorePb store, StoreNestedPb nested, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.store = store;
        this.nested = nested;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.store = store;
        builder.nested = nested;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof StoreResponse)) return false;
        StoreResponse o = (StoreResponse) other;
        return unknownFields().equals(o.unknownFields())
                && store.equals(o.store)
                && nested.equals(o.nested);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + store.hashCode();
            result = result * 37 + nested.hashCode();
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(", store=").append(store);
        builder.append(", nested=").append(nested);
        return builder.replace(0, 2, "StoreResponse{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<StoreResponse, Builder> {

        public StorePb store;

        public StoreNestedPb nested;

        public Builder() {}

        public Builder store(StorePb store) {
            this.store = store;
            return this;
        }

        public Builder nested(StoreNestedPb nested) {
            this.nested = nested;
            return this;
        }

        @Override
        public StoreResponse build() {
            if (store == null || nested == null) {
                throw Internal.missingRequiredFields(store, "store", nested, "nested");
            }
            return new StoreResponse(store, nested, super.buildUnknownFields());
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class StoreNestedPb extends Message<StoreNestedPb, StoreNestedPb.Builder> {

        public static final ProtoAdapter<StoreNestedPb> ADAPTER = new ProtoAdapter_StoreNestedPb();

        private static final long serialVersionUID = 0L;

        public static final String DEFAULT_EMPLOYEE = "";

        @WireField(
                tag = 1,
                adapter = "com.squareup.wire.ProtoAdapter#STRING",
                label = WireField.Label.REQUIRED)
        private String employee;

        public StoreNestedPb() {
            super(ADAPTER, ByteString.EMPTY);
        }

        public StoreNestedPb(String employee) {
            this(employee, ByteString.EMPTY);
        }

        public StoreNestedPb(String employee, ByteString unknownFields) {
            super(ADAPTER, unknownFields);
            this.employee = employee;
        }

        @Override
        public Builder newBuilder() {
            Builder builder = new Builder();
            builder.employee = employee;
            builder.addUnknownFields(unknownFields());
            return builder;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) return true;
            if (!(other instanceof StoreNestedPb)) return false;
            StoreNestedPb o = (StoreNestedPb) other;
            return unknownFields().equals(o.unknownFields()) && employee.equals(o.employee);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode;
            if (result == 0) {
                result = unknownFields().hashCode();
                result = result * 37 + employee.hashCode();
                super.hashCode = result;
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(", employee=").append(employee);
            return builder.replace(0, 2, "StoreNestedPb{").append('}').toString();
        }

        public static final class Builder extends Message.Builder<StoreNestedPb, Builder> {

            public String employee;

            public Builder() {}

            public Builder employee(String employee) {
                this.employee = employee;
                return this;
            }

            @Override
            public StoreNestedPb build() {
                if (employee == null) {
                    throw Internal.missingRequiredFields(employee, "employee");
                }
                return new StoreNestedPb(employee, super.buildUnknownFields());
            }
        }

        private static final class ProtoAdapter_StoreNestedPb extends ProtoAdapter<StoreNestedPb> {

            public ProtoAdapter_StoreNestedPb() {
                super(FieldEncoding.LENGTH_DELIMITED, StoreNestedPb.class);
            }

            @Override
            public int encodedSize(StoreNestedPb value) {
                return ProtoAdapter.STRING.encodedSizeWithTag(1, value.employee)
                        + value.unknownFields().size();
            }

            @Override
            public void encode(ProtoWriter writer, StoreNestedPb value) throws IOException {
                ProtoAdapter.STRING.encodeWithTag(writer, 1, value.employee);
                writer.writeBytes(value.unknownFields());
            }

            @Override
            public StoreNestedPb decode(ProtoReader reader) throws IOException {
                Builder builder = new Builder();
                long token = reader.beginMessage();
                for (int tag; (tag = reader.nextTag()) != -1; ) {
                    switch (tag) {
                        case 1:
                            builder.employee(ProtoAdapter.STRING.decode(reader));
                            break;
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
            public StoreNestedPb redact(StoreNestedPb value) {
                Builder builder = value.newBuilder();
                builder.clearUnknownFields();
                return builder.build();
            }
        }
    }

    private static final class ProtoAdapter_StoreResponse extends ProtoAdapter<StoreResponse> {

        public ProtoAdapter_StoreResponse() {
            super(FieldEncoding.LENGTH_DELIMITED, StoreResponse.class);
        }

        @Override
        public int encodedSize(StoreResponse value) {
            return StorePb.ADAPTER.encodedSizeWithTag(1, value.store)
                    + StoreNestedPb.ADAPTER.encodedSizeWithTag(2, value.nested)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, StoreResponse value) throws IOException {
            StorePb.ADAPTER.encodeWithTag(writer, 1, value.store);
            StoreNestedPb.ADAPTER.encodeWithTag(writer, 2, value.nested);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public StoreResponse decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.store(StorePb.ADAPTER.decode(reader));
                        break;
                    case 2:
                        builder.nested(StoreNestedPb.ADAPTER.decode(reader));
                        break;
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
        public StoreResponse redact(StoreResponse value) {
            Builder builder = value.newBuilder();
            builder.store = StorePb.ADAPTER.redact(builder.store);
            builder.nested = StoreNestedPb.ADAPTER.redact(builder.nested);
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
