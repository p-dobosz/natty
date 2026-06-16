package pl.chunky.nats.kv;

import io.nats.client.KeyValue;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.KeyValueWatcher;
import io.nats.client.api.StorageType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import pl.chunky.nats.NatsClient;
import pl.chunky.nats.NatsException;
import pl.chunky.nats.codec.Codec;

public class KvStoreImpl implements KvStore {

  final NatsClient natsClient;
  final Codec codec;
  final ExecutorService executorService;

  public KvStoreImpl(final NatsClient natsClient, final Codec codec,
      final ExecutorService executorService) {
    this.natsClient = natsClient;
    this.codec = codec;
    this.executorService = executorService;
  }

  @Override
  public void init(final String bucket) throws NatsException {
    try {
      final KeyValueConfiguration config = KeyValueConfiguration.builder()
          .name(bucket)
          .storageType(StorageType.Memory)
          .build();
      natsClient.connection().keyValueManagement().create(config);
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> long put(final String bucket, final String key, final T value) throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      return kv.put(key, codec.encode(value));
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> long create(final String bucket, final String key, final T value)
      throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      return kv.create(key, codec.encode(value));
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> long update(final String bucket, final String key, final T value,
      final long expectedRevision) throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      return kv.update(key, codec.encode(value), expectedRevision);
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> Optional<T> get(final String bucket, final String key, final Class<T> type)
      throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      final KeyValueEntry entry = kv.get(key);
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
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      kv.delete(key);
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public void purge(final String bucket, final String key) throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      kv.purge(key);
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public List<String> keys(final String bucket) throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      return kv.keys();
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> void watch(final String bucket, final String key, final Class<T> type,
      final KvWatcher<T> watcher) throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      kv.watch(key, new KeyValueWatcher() {
        @Override
        public void watch(final KeyValueEntry entry) {
          executorService.execute(() -> {
            final KvEventType kvEventType = switch (entry.getOperation()) {
              case PUT -> KvEventType.UPSERT;
              case DELETE -> KvEventType.DELETE;
              case PURGE -> KvEventType.PURGE;
            };

            final T decoded =
                (entry.getValue() != null && entry.getValue().length > 0) ? codec.decode(
                    entry.getValue(), type) : null;
            watcher.onUpdate(entry.getKey(), decoded, kvEventType);
          });
        }

        @Override
        public void endOfData() {
        }
      });
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }

  @Override
  public <T> void watchAll(final String bucket, final Class<T> type, final KvWatcher<T> watcher)
      throws NatsException {
    try {
      final KeyValue kv = natsClient.connection().keyValue(bucket);
      kv.watchAll(new KeyValueWatcher() {
        @Override
        public void watch(final KeyValueEntry entry) {
          executorService.execute(() -> {
            final KvEventType kvType = switch (entry.getOperation()) {
              case PUT -> KvEventType.UPSERT;
              case DELETE -> KvEventType.DELETE;
              case PURGE -> KvEventType.PURGE;
            };

            final T decoded =
                (entry.getValue() != null && entry.getValue().length > 0) ? codec.decode(
                    entry.getValue(), type) : null;
            watcher.onUpdate(entry.getKey(), decoded, kvType);
          });
        }

        @Override
        public void endOfData() {
        }
      });
    } catch (final Exception e) {
      throw new NatsException(e);
    }
  }
}