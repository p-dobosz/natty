package pl.pdobosz.nats.codec;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

public class GsonCodec implements Codec {

  final Gson gson;

  public GsonCodec() {
    this(new Gson());
  }

  public GsonCodec(final Gson gson) {
    this.gson = gson;
  }

  @Override
  public <T> byte[] encode(final T object) {
    return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public <T> T decode(final byte[] bytes, final Class<T> type) {
    return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), type);
  }
}
