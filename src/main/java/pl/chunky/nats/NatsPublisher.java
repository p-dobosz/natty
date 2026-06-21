package pl.chunky.nats;

import io.nats.client.Message;
import java.time.Duration;

public interface NatsPublisher {

  <T> void publish(final String topic, final T message);

  <T, J> J request(final String topic, final T message, final Class<J> responseType,
      final Duration timeout);

  <T> void reply(final Message requestMessage, final T response);
}