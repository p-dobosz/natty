package pl.chunky.nats.codec;

import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;

public class FuryCodec implements Codec {

  final ThreadSafeFury fury;

  public FuryCodec() {
    this.fury = Fury.builder()
        .withLanguage(Language.JAVA)
        .requireClassRegistration(false)
        .buildThreadSafeFury();
  }

  @Override
  public <T> byte[] encode(final T object) {
    return fury.serialize(object);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T decode(final byte[] bytes, final Class<T> type) {
    return (T) fury.deserialize(bytes);
  }
}