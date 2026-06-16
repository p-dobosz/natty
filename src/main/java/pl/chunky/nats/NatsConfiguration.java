package pl.chunky.nats;

import java.time.Duration;

public record NatsConfiguration(
    String clientName,
    String host,
    String username,
    String password,
    Duration connectionTimeout
) {

  public static NatsConfiguration defaultConfig(final String clientName, final String host) {
    return new NatsConfiguration(clientName, host, "", "", Duration.ofSeconds(5));
  }
}