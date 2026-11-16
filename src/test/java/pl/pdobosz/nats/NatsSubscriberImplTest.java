package pl.pdobosz.nats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nats.client.Message;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class NatsSubscriberImplTest {

  record Order(String id) {
  }

  static class SampleSubscriber {

    @NatsSubscribe(value = "orders.created", packet = Order.class)
    void onOrder(final Order order, final Message message) {
    }

    @NatsSubscribe(value = "orders.deleted", packet = String.class)
    void onDelete(final String id, final Message message) {
    }

    void notAnnotated() {
    }
  }

  @Test
  void findsOnlyAnnotatedMethods() {
    final List<Method> methods = NatsSubscriberImpl.annotatedMethods(new SampleSubscriber());

    assertEquals(2, methods.size());
    assertTrue(methods.stream().noneMatch(method -> method.getName().equals("notAnnotated")));
  }

  @Test
  void readsTopicAndPacketFromAnnotation() {
    final Method method = NatsSubscriberImpl.annotatedMethods(new SampleSubscriber()).stream()
        .filter(candidate -> candidate.getName().equals("onOrder"))
        .findFirst()
        .orElseThrow();

    final NatsSubscribe annotation = method.getAnnotation(NatsSubscribe.class);
    assertEquals("orders.created", annotation.value());
    assertEquals(Order.class, annotation.packet());
  }
}
