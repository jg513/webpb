// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Store.proto
package com.github.jg513.example.store;

import com.google.protobuf.MessageOptions;
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

public final class StoreCurrentRequest extends Message<StoreCurrentRequest, StoreCurrentRequest.Builder> {
  public static final ProtoAdapter<StoreCurrentRequest> ADAPTER = new ProtoAdapter_StoreCurrentRequest();

  private static final long serialVersionUID = 0L;

  public static final MessageOptions MESSAGE_OPTIONS = new MessageOptions.Builder()
      .method("GET")
      .path("/stores/current")
      .build();

  public StoreCurrentRequest() {
    this(ByteString.EMPTY);
  }

  public StoreCurrentRequest(ByteString unknownFields) {
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
    if (!(other instanceof StoreCurrentRequest)) return false;
    StoreCurrentRequest o = (StoreCurrentRequest) other;
    return unknownFields().equals(o.unknownFields());
  }

  @Override
  public int hashCode() {
    return unknownFields().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    return builder.replace(0, 2, "StoreCurrentRequest{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<StoreCurrentRequest, Builder> {
    public Builder() {
    }

    @Override
    public StoreCurrentRequest build() {
      return new StoreCurrentRequest(super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_StoreCurrentRequest extends ProtoAdapter<StoreCurrentRequest> {
    public ProtoAdapter_StoreCurrentRequest() {
      super(FieldEncoding.LENGTH_DELIMITED, StoreCurrentRequest.class);
    }

    @Override
    public int encodedSize(StoreCurrentRequest value) {
      return value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, StoreCurrentRequest value) throws IOException {
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public StoreCurrentRequest decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          default: {
            reader.readUnknownField(tag);
          }
        }
      }
      builder.addUnknownFields(reader.endMessageAndGetUnknownFields(token));
      return builder.build();
    }

    @Override
    public StoreCurrentRequest redact(StoreCurrentRequest value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
