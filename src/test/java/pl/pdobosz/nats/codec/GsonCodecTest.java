package pl.pdobosz.nats.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class GsonCodecTest {

  record Sample(int id, String name) {
  }

  final GsonCodec codec = new GsonCodec();

  @Test
  void roundTrip() {
    final Sample sample = new Sample(1, "test");

    final byte[] bytes = codec.encode(sample);

    assertEquals(sample, codec.decode(bytes, Sample.class));
  }

  @Test
  void decodesJson() {
    final byte[] bytes = "{\"id\":2,\"name\":\"other\"}".getBytes(StandardCharsets.UTF_8);

    assertEquals(new Sample(2, "other"), codec.decode(bytes, Sample.class));
  }
}
