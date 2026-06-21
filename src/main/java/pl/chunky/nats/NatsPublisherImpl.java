package pl.chunky.nats;

import io.nats.client.Connection;
import io.nats.client.Message;
import java.time.Duration;
import pl.chunky.nats.codec.Codec;
import pl.chunky.nats.codec.FuryCodec;

public class NatsPublisherImpl implements NatsPublisher {

  final Codec codec = new FuryCodec();
  final Connection connection;

  public NatsPublisherImpl(final Connection connection) {
    this.connection = connection;
  }

  @Override
  public <T> void publish(final String topic, final T message) {
    connection.publish(topic, codec.encode(message));
  }

  @Override
  public <T, J> J request(final String topic, final T message, final Class<J> responseType, final Duration timeout) {
    try {
      final Message response = connection.request(topic, codec.encode(message), timeout);
      if (response == null) {
        return null;
      }
      return codec.decode(response.getData(), responseType);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> void reply(final Message requestMessage, final T response) {
    try {
      final String replyTo = requestMessage.getReplyTo();
      if (replyTo != null && !replyTo.isEmpty()) {
        connection.publish(replyTo, codec.encode(response));
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}