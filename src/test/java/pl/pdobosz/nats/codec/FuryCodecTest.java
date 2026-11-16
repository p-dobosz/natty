package pl.pdobosz.nats.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FuryCodecTest {

  record Sample(int id, String name) {
  }

  final FuryCodec codec = new FuryCodec();

  @Test
  void roundTrip() {
    final Sample sample = new Sample(1, "test");

    final byte[] bytes = codec.encode(sample);

    assertEquals(sample, codec.decode(bytes, Sample.class));
  }

  @Test
  void decodeWithWrongTypeThrows() {
    final byte[] bytes = codec.encode(new Sample(1, "test"));

    assertThrows(IllegalArgumentException.class, () -> codec.decode(bytes, String.class));
  }
}
