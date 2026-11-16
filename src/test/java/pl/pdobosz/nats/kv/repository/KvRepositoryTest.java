package pl.pdobosz.nats.kv.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pl.pdobosz.nats.kv.KvStore;
import pl.pdobosz.nats.kv.KvWatcher;

class KvRepositoryTest {

  static class FakeKvStore implements KvStore {

    final Map<String, Object> values = new HashMap<>();
    String lastBucket;

    @Override
    public void init(final String bucket) {
    }

    @Override
    public <T> long put(final String bucket, final String key, final T value) {
      lastBucket = bucket;
      values.put(key, value);
      return values.size();
    }

    @Override
    public <T> long create(final String bucket, final String key, final T value) {
      return put(bucket, key, value);
    }

    @Override
    public <T> long update(final String bucket, final String key, final T value,
        final long expectedRevision) {
      return put(bucket, key, value);
    }

    @Override
    public <T> Optional<T> get(final String bucket, final String key, final Class<T> type) {
      lastBucket = bucket;
      return Optional.ofNullable(type.cast(values.get(key)));
    }

    @Override
    public void delete(final String bucket, final String key) {
      lastBucket = bucket;
      values.remove(key);
    }

    @Override
    public void purge(final String bucket, final String key) {
    }

    @Override
    public List<String> keys(final String bucket) {
      return List.copyOf(values.keySet());
    }

    @Override
    public <T> void watch(final String bucket, final String key, final Class<T> type,
        final KvWatcher<T> watcher) {
    }

    @Override
    public <T> void watchAll(final String bucket, final Class<T> type,
        final KvWatcher<T> watcher) {
    }
  }

  static class UserRepository extends KvRepository<String> {

    UserRepository(final KvStore kvStore) {
      super(kvStore, "users", String.class);
    }
  }

  @Test
  void putThenGet() {
    final UserRepository repository = new UserRepository(new FakeKvStore());

    repository.put("alice", "Alice");

    assertEquals(Optional.of("Alice"), repository.get("alice"));
  }

  @Test
  void deleteRemovesValue() {
    final UserRepository repository = new UserRepository(new FakeKvStore());
    repository.put("alice", "Alice");

    repository.delete("alice");

    assertTrue(repository.get("alice").isEmpty());
  }

  @Test
  void usesConfiguredBucket() {
    final FakeKvStore kvStore = new FakeKvStore();
    final UserRepository repository = new UserRepository(kvStore);

    repository.put("alice", "Alice");

    assertEquals("users", kvStore.lastBucket);
  }
}
