package pl.chunky.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.time.Duration;

public class NatsClient {

  final Connection natsConnection;
  final NatsPublisher publisher;
  final NatsSubscriber subscriber;

  public NatsClient(final NatsConfiguration natsConfiguration) throws Exception {
    final Options.Builder builder = new Options.Builder()
        .server(natsConfiguration.host())
        .userInfo(natsConfiguration.username(), natsConfiguration.password())
        .connectionName(natsConfiguration.clientName())
        .connectionTimeout(natsConfiguration.connectionTimeout())
        .pingInterval(Duration.ofSeconds(10))
        .maxReconnects(-1)
        .reconnectWait(Duration.ofSeconds(2));

    this.natsConnection = Nats.connect(builder.build());
    this.publisher = new NatsPublisherImpl(this.natsConnection);
    this.subscriber = new NatsSubscriberImpl(this.natsConnection);
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

  public void close() throws Exception {
    this.subscriber.close();
    this.natsConnection.close();
  }
}