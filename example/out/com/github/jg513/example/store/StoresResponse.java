// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Store.proto
package com.github.jg513.example.store;

import com.github.jg513.example.resource.PagingPb;
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

public final class StoresResponse extends Message<StoresResponse, StoresResponse.Builder> {
  public static final ProtoAdapter<StoresResponse> ADAPTER = new ProtoAdapter_StoresResponse();

  private static final long serialVersionUID = 0L;

  @WireField(
      tag = 1,
      adapter = "com.github.jg513.example.store.StorePb#ADAPTER",
      label = WireField.Label.REQUIRED
  )
  public final StorePb stores;

  @WireField(
      tag = 2,
      adapter = "com.github.jg513.example.resource.PagingPb#ADAPTER",
      label = WireField.Label.REQUIRED
  )
  public final PagingPb paging;

  public StoresResponse(StorePb stores, PagingPb paging) {
    this(stores, paging, ByteString.EMPTY);
  }

  public StoresResponse(StorePb stores, PagingPb paging, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.stores = stores;
    this.paging = paging;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.stores = stores;
    builder.paging = paging;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof StoresResponse)) return false;
    StoresResponse o = (StoresResponse) other;
    return unknownFields().equals(o.unknownFields())
        && stores.equals(o.stores)
        && paging.equals(o.paging);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + stores.hashCode();
      result = result * 37 + paging.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", stores=").append(stores);
    builder.append(", paging=").append(paging);
    return builder.replace(0, 2, "StoresResponse{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<StoresResponse, Builder> {
    public StorePb stores;

    public PagingPb paging;

    public Builder() {
    }

    public Builder stores(StorePb stores) {
      this.stores = stores;
      return this;
    }

    public Builder paging(PagingPb paging) {
      this.paging = paging;
      return this;
    }

    @Override
    public StoresResponse build() {
      if (stores == null
          || paging == null) {
        throw Internal.missingRequiredFields(stores, "stores",
            paging, "paging");
      }
      return new StoresResponse(stores, paging, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_StoresResponse extends ProtoAdapter<StoresResponse> {
    public ProtoAdapter_StoresResponse() {
      super(FieldEncoding.LENGTH_DELIMITED, StoresResponse.class);
    }

    @Override
    public int encodedSize(StoresResponse value) {
      return StorePb.ADAPTER.encodedSizeWithTag(1, value.stores)
          + PagingPb.ADAPTER.encodedSizeWithTag(2, value.paging)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, StoresResponse value) throws IOException {
      StorePb.ADAPTER.encodeWithTag(writer, 1, value.stores);
      PagingPb.ADAPTER.encodeWithTag(writer, 2, value.paging);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public StoresResponse decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.stores(StorePb.ADAPTER.decode(reader)); break;
          case 2: builder.paging(PagingPb.ADAPTER.decode(reader)); break;
          default: {
            reader.readUnknownField(tag);
          }
        }
      }
      builder.addUnknownFields(reader.endMessageAndGetUnknownFields(token));
      return builder.build();
    }

    @Override
    public StoresResponse redact(StoresResponse value) {
      Builder builder = value.newBuilder();
      builder.stores = StorePb.ADAPTER.redact(builder.stores);
      builder.paging = PagingPb.ADAPTER.redact(builder.paging);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
