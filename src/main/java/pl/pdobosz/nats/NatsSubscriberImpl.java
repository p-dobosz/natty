package pl.pdobosz.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import pl.pdobosz.nats.codec.Codec;

public class NatsSubscriberImpl implements NatsSubscriber {

  final Codec codec;
  final Connection connection;
  final List<Dispatcher> dispatchers = new CopyOnWriteArrayList<>();
  final ExecutorService executor = Executors.newFixedThreadPool(4, threadFactory());

  public NatsSubscriberImpl(final Connection connection, final Codec codec) {
    this.connection = connection;
    this.codec = codec;
  }

  private static ThreadFactory threadFactory() {
    final AtomicInteger counter = new AtomicInteger();
    return runnable -> new Thread(runnable, "nats-subscriber-" + counter.incrementAndGet());
  }

  @Override
  public void close() {
    dispatchers.forEach(connection::closeDispatcher);
    dispatchers.clear();
    if (!executor.isShutdown()) {
      executor.shutdownNow();
    }
  }

  @Override
  public <T> void subscribe(final String topic, final Class<T> type, final Object target,
      final Method method, final Executor executor) {
    final Dispatcher dispatcher = connection.createDispatcher(message -> {
      final T decoded = codec.decode(message.getData(), type);
      executor.execute(() -> invoke(method, target, decoded, message));
    });

    dispatcher.subscribe(topic);
    dispatchers.add(dispatcher);
  }

  @Override
  public void registerSubscriber(final Object subscriber) {
    for (final Method method : annotatedMethods(subscriber)) {
      final NatsSubscribe annotation = method.getAnnotation(NatsSubscribe.class);
      @SuppressWarnings("unchecked")
      final Class<Object> packetType = (Class<Object>) annotation.packet();
      subscribe(annotation.value(), packetType, subscriber, method, executor);
    }
  }

  static List<Method> annotatedMethods(final Object subscriber) {
    return Stream.of(subscriber.getClass().getDeclaredMethods())
        .filter(method -> method.isAnnotationPresent(NatsSubscribe.class))
        .toList();
  }

  private static void invoke(final Method method, final Object target, final Object... args) {
    try {
      method.invoke(target, args);
    } catch (final InvocationTargetException e) {
      throw new NatsException(e.getCause());
    } catch (final IllegalAccessException e) {
      throw new NatsException(e);
    }
  }
}
