package pl.chunky.nats.codec;

public interface Codec {

  <T> byte[] encode(final T object);

  <T> T decode(final byte[] bytes, final Class<T> type);
}