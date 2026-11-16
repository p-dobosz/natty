package pl.pdobosz.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;
import java.time.Duration;
import pl.pdobosz.nats.codec.Codec;
import pl.pdobosz.nats.codec.FuryCodec;

public class NatsClient implements AutoCloseable {

  final Connection natsConnection;
  final NatsPublisher publisher;
  final NatsSubscriber subscriber;

  public NatsClient(final NatsConfiguration natsConfiguration) {
    this(natsConfiguration, new FuryCodec());
  }

  public NatsClient(final NatsConfiguration natsConfiguration, final Codec codec) {
    final Options.Builder builder = new Options.Builder()
        .server(natsConfiguration.host())
        .userInfo(natsConfiguration.username(), natsConfiguration.password())
        .connectionName(natsConfiguration.clientName())
        .connectionTimeout(natsConfiguration.connectionTimeout())
        .pingInterval(Duration.ofSeconds(10))
        .maxReconnects(-1)
        .reconnectWait(Duration.ofSeconds(2));

    this.natsConnection = connect(builder.build());
    this.publisher = new NatsPublisherImpl(this.natsConnection, codec);
    this.subscriber = new NatsSubscriberImpl(this.natsConnection, codec);
  }

  private static Connection connect(final Options options) {
    try {
      return Nats.connect(options);
    } catch (final IOException e) {
      throw new NatsException("Could not connect to NATS", e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new NatsException("Interrupted while connecting to NATS", e);
    }
  }

  public Connection connection() {
    return this.natsConnection;
  }

  public NatsPublisher publisher() {
    return this.publisher;
  }

  public NatsSubscriber subscriber() {
    return this.subscriber;
  }

  @Override
  public void close() {
    this.subscriber.close();
    try {
      this.natsConnection.close();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
