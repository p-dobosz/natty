package pl.chunky.nats;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public interface NatsSubscriber {

  <T> void subscribe(final String topic, final Class<T> type, final Object target,
      final Method method, final Executor executor);

  void registerSubscriber(final Object subscriber);

  void close();
}