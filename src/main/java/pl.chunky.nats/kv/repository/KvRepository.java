package pl.chunky.nats.kv.repository;

import java.util.Optional;
import pl.chunky.nats.NatsException;
import pl.chunky.nats.kv.KvStore;

public abstract class KvRepository<T> {

  final KvStore kvStore;
  final String bucket;
  final Class<T> type;

  protected KvRepository(final KvStore kvStore, final String bucket, final Class<T> type) {
    this.kvStore = kvStore;
    this.bucket = bucket;
    this.type = type;
  }

  public void put(final String key, final T value) throws NatsException {
    kvStore.put(bucket, key, value);
  }

  public Optional<T> get(final String key) throws NatsException {
    return kvStore.get(bucket, key, type);
  }

  public void delete(final String key) throws NatsException {
    kvStore.delete(bucket, key);
  }
}