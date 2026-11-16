package pl.pdobosz.nats;

import io.nats.client.Connection;
import io.nats.client.Message;
import java.time.Duration;
import java.util.Optional;
import pl.pdobosz.nats.codec.Codec;

public class NatsPublisherImpl implements NatsPublisher {

  final Codec codec;
  final Connection connection;

  public NatsPublisherImpl(final Connection connection, final Codec codec) {
    this.connection = connection;
    this.codec = codec;
  }

  @Override
  public <T> void publish(final String topic, final T message) {
    connection.publish(topic, codec.encode(message));
  }

  @Override
  public <T, J> Optional<J> request(final String topic, final T message,
      final Class<J> responseType, final Duration timeout) {
    try {
      final Message response = connection.request(topic, codec.encode(message), timeout);
      return Optional.ofNullable(response)
          .map(message1 -> codec.decode(message1.getData(), responseType));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new NatsException(e);
    }
  }

  @Override
  public <T> void reply(final Message requestMessage, final T response) {
    final String replyTo = requestMessage.getReplyTo();
    if (replyTo != null && !replyTo.isEmpty()) {
      connection.publish(replyTo, codec.encode(response));
    }
  }
}
