package pl.chunky.nats.kv;

import java.util.List;
import java.util.Optional;
import pl.chunky.nats.NatsException;

public interface KvStore {

  void init(final String bucket) throws NatsException;

  <T> long put(final String bucket, final String key, final T value) throws NatsException;

  <T> long create(final String bucket, final String key, final T value) throws NatsException;

  <T> long update(final String bucket, final String key, final T value, final long expectedRevision)
      throws NatsException;

  <T> Optional<T> get(final String bucket, final String key, final Class<T> type)
      throws NatsException;

  void delete(final String bucket, final String key) throws NatsException;

  void purge(final String bucket, final String key) throws NatsException;

  List<String> keys(final String bucket) throws NatsException;

  <T> void watch(final String bucket, final String key, final Class<T> type,
      final KvWatcher<T> watcher) throws NatsException;

  <T> void watchAll(final String bucket, final Class<T> type, final KvWatcher<T> watcher)
      throws NatsException;
}