package pl.pdobosz.nats.kv;

import io.nats.client.KeyValue;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.KeyValueOperation;
import io.nats.client.api.KeyValueWatcher;
import io.nats.client.api.StorageType;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import pl.pdobosz.nats.NatsClient;
import pl.pdobosz.nats.NatsException;
import pl.pdobosz.nats.codec.Codec;

public class KvStoreImpl implements KvStore {

  final NatsClient natsClient;
  final Codec codec;
  final ExecutorService executorService;
  final Map<String, KeyValue> buckets = new ConcurrentHashMap<>();

  public KvStoreImpl(final NatsClient natsClient, final Codec codec,
      final ExecutorService executorService) {
    this.natsClient = natsClient;
    this.codec = codec;
    this.executorService = executorService;
  }

  @Override
  public void init(final String bucket) throws NatsException {
    try {
      final var management = natsClient.connection().keyValueManagement();
      if (!management.getBucketNames().contains(bucket)) {
        final KeyValueConfiguration config = KeyValueConfiguration.builder()
            .name(bucket)
            .storageType(StorageType.Memory)
            .build();
        management.create(config);
      }
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> long put(final String bucket, final String key, final T value) throws NatsException {
    try {
      return keyValue(bucket).put(key, codec.encode(value));
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> long create(final String bucket, final String key, final T value)
      throws NatsException {
    try {
      return keyValue(bucket).create(key, codec.encode(value));
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> long update(final String bucket, final String key, final T value,
      final long expectedRevision) throws NatsException {
    try {
      return keyValue(bucket).update(key, codec.encode(value), expectedRevision);
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> Optional<T> get(final String bucket, final String key, final Class<T> type)
      throws NatsException {
    try {
      final KeyValueEntry entry = keyValue(bucket).get(key);
      if (entry == null || entry.getValue() == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(codec.decode(entry.getValue(), type));
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public void delete(final String bucket, final String key) throws NatsException {
    try {
      keyValue(bucket).delete(key);
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public void purge(final String bucket, final String key) throws NatsException {
    try {
      keyValue(bucket).purge(key);
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public List<String> keys(final String bucket) throws NatsException {
    try {
      return keyValue(bucket).keys();
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> void watch(final String bucket, final String key, final Class<T> type,
      final KvWatcher<T> watcher) throws NatsException {
    try {
      keyValue(bucket).watch(key, keyValueWatcher(type, watcher));
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> void watchAll(final String bucket, final Class<T> type, final KvWatcher<T> watcher)
      throws NatsException {
    try {
      keyValue(bucket).watchAll(keyValueWatcher(type, watcher));
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  private KeyValue keyValue(final String bucket) throws IOException {
    final KeyValue cached = buckets.get(bucket);
    if (cached != null) {
      return cached;
    }
    final KeyValue keyValue = natsClient.connection().keyValue(bucket);
    buckets.put(bucket, keyValue);
    return keyValue;
  }

  private <T> KeyValueWatcher keyValueWatcher(final Class<T> type, final KvWatcher<T> watcher) {
    return new KeyValueWatcher() {
      @Override
      public void watch(final KeyValueEntry entry) {
        executorService.execute(() -> {
          final T decoded = entry.getValue() == null || entry.getValue().length == 0
              ? null
              : codec.decode(entry.getValue(), type);
          watcher.onUpdate(entry.getKey(), decoded, toEventType(entry.getOperation()));
        });
      }

      @Override
      public void endOfData() {
      }
    };
  }

  static KvEventType toEventType(final KeyValueOperation operation) {
    return switch (operation) {
      case PUT -> KvEventType.UPSERT;
      case DELETE -> KvEventType.DELETE;
      case PURGE -> KvEventType.PURGE;
    };
  }
}
