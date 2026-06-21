package pl.chunky.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import pl.chunky.nats.codec.Codec;
import pl.chunky.nats.codec.FuryCodec;

public class NatsSubscriberImpl implements NatsSubscriber {

  final Codec codec = new FuryCodec();
  final Connection connection;
  final List<Dispatcher> dispatchers = new ArrayList<>();
  final ExecutorService executor = Executors.newFixedThreadPool(4,
      r -> new Thread(r, "nats-subscriber"));

  public NatsSubscriberImpl(final Connection connection) {
    this.connection = connection;
  }

  @Override
  public void close() {
    if (!this.executor.isShutdown()) {
      this.executor.shutdown();
    }
  }

  @Override
  public <T> void subscribe(final String topic, final Class<T> type, final Object target,
      final Method method, final Executor executor) {
    final Dispatcher dispatcher = connection.createDispatcher(msg -> {
      final T decoded = codec.decode(msg.getData(), type);
      executor.execute(() -> {
        try {
          method.invoke(target, decoded, msg);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      });
    });

    dispatcher.subscribe(topic);
    dispatchers.add(dispatcher);
  }

  @Override
  public void registerSubscriber(final Object subscriber) {
    Stream.of(subscriber.getClass().getDeclaredMethods())
        .filter(m -> m.isAnnotationPresent(NatsSubscribe.class))
        .forEach(method -> {
          final NatsSubscribe ann = method.getAnnotation(NatsSubscribe.class);
          @SuppressWarnings("unchecked") final Class<Object> packetType = (Class<Object>) ann.packet();
          subscribe(ann.value(), packetType, subscriber, method, this.executor);
        });
  }
}