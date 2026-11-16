package pl.pdobosz.nats.kv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nats.client.api.KeyValueOperation;
import org.junit.jupiter.api.Test;

class KvStoreImplTest {

  @Test
  void mapsPutToUpsert() {
    assertEquals(KvEventType.UPSERT, KvStoreImpl.toEventType(KeyValueOperation.PUT));
  }

  @Test
  void mapsDeleteToDelete() {
    assertEquals(KvEventType.DELETE, KvStoreImpl.toEventType(KeyValueOperation.DELETE));
  }

  @Test
  void mapsPurgeToPurge() {
    assertEquals(KvEventType.PURGE, KvStoreImpl.toEventType(KeyValueOperation.PURGE));
  }
}
