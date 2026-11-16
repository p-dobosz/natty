package pl.pdobosz.nats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class NatsConfigurationTest {

  @Test
  void defaultConfigFillsDefaults() {
    final NatsConfiguration config =
        NatsConfiguration.defaultConfig("client", "nats://localhost:4222");

    assertEquals("client", config.clientName());
    assertEquals("nats://localhost:4222", config.host());
    assertEquals("", config.username());
    assertEquals("", config.password());
    assertEquals(Duration.ofSeconds(5), config.connectionTimeout());
  }
}
