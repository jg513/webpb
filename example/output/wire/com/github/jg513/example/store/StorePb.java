// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Store.proto
package com.github.jg513.example.store;

import com.github.jg513.webpb.core.WebpbMessage;
import com.github.jg513.webpb.options.MessageOptions;
import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import okio.ByteString;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public final class StorePb extends Message<StorePb, StorePb.Builder> implements WebpbMessage {

    public static final MessageOptions MESSAGE_OPTIONS =
            new MessageOptions.Builder().javaAnnotations("@JsonInclude(Include.NON_NULL)").build();

    public static final ProtoAdapter<StorePb> ADAPTER = new ProtoAdapter_StorePb();

    private static final long serialVersionUID = 0L;

    public static final Integer DEFAULT_ID = 0;

    public static final String DEFAULT_NAME = "store";

    public static final Integer DEFAULT_CITY = 100;

    @WireField(
            tag = 1,
            adapter = "com.squareup.wire.ProtoAdapter#INT32",
            label = WireField.Label.REQUIRED)
    private Integer id;

    @WireField(
            tag = 2,
            adapter = "com.squareup.wire.ProtoAdapter#STRING",
            label = WireField.Label.REQUIRED)
    private String name;

    @WireField(
            tag = 3,
            adapter = "com.squareup.wire.ProtoAdapter#INT32",
            label = WireField.Label.REQUIRED)
    private Integer city;

    public StorePb() {
        super(ADAPTER, ByteString.EMPTY);
    }

    public StorePb(Integer id, String name, Integer city) {
        this(id, name, city, ByteString.EMPTY);
    }

    public StorePb(Integer id, String name, Integer city, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.id = id;
        this.name = name;
        this.city = city;
    }

    @Override
    public MessageOptions messageOptions() {
        return MESSAGE_OPTIONS;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.id = id;
        builder.name = name;
        builder.city = city;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof StorePb)) return false;
        StorePb o = (StorePb) other;
        return unknownFields().equals(o.unknownFields())
                && id.equals(o.id)
                && name.equals(o.name)
                && city.equals(o.city);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + id.hashCode();
            result = result * 37 + name.hashCode();
            result = result * 37 + city.hashCode();
            super.hashCode = result;
        }
        return result;
    }

    public static final class Builder extends Message.Builder<StorePb, Builder> {

        public Integer id;

        public String name;

        public Integer city;

        public Builder() {}

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder city(Integer city) {
            this.city = city;
            return this;
        }

        @Override
        public StorePb build() {
            if (id == null || name == null || city == null) {
                throw Internal.missingRequiredFields(id, "id", name, "name", city, "city");
            }
            return new StorePb(id, name, city, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_StorePb extends ProtoAdapter<StorePb> {

        public ProtoAdapter_StorePb() {
            super(
                    FieldEncoding.LENGTH_DELIMITED,
                    StorePb.class,
                    "type.googleapis.com/StoreProto.StorePb");
        }

        @Override
        public int encodedSize(StorePb value) {
            return ProtoAdapter.INT32.encodedSizeWithTag(1, value.id)
                    + ProtoAdapter.STRING.encodedSizeWithTag(2, value.name)
                    + ProtoAdapter.INT32.encodedSizeWithTag(3, value.city)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, StorePb value) throws IOException {
            ProtoAdapter.INT32.encodeWithTag(writer, 1, value.id);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.name);
            ProtoAdapter.INT32.encodeWithTag(writer, 3, value.city);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public StorePb decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.id(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 2:
                        builder.name(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.city(ProtoAdapter.INT32.decode(reader));
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
        public StorePb redact(StorePb value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
