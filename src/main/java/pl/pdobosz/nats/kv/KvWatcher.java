package pl.pdobosz.nats.kv;

@FunctionalInterface
public interface KvWatcher<T> {

  void onUpdate(final String key, final T value, final KvEventType type);
}