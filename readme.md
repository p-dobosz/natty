# natty

A small typed Java client for [NATS](https://nats.io) built on top of [jnats](https://github.com/nats-io/nats.java).

## Features

- publish/subscribe with `@NatsSubscribe`
- request/reply
- Key-Value store with watchers
- pluggable codecs: Fury (default) and Gson (JSON)
- Java 21

## Run NATS

```bash
docker run --rm -p 4222:4222 nats:latest -js
```

## Usage

```java
NatsConfiguration config =
    NatsConfiguration.defaultConfig("shop", "nats://localhost:4222");

try (NatsClient client = new NatsClient(config)) {
  client.publisher().publish("orders.created", new OrderCreated("ORD-1"));
}
```

Subscriber:

```java
public class OrderListener {

  @NatsSubscribe(value = "orders.created", packet = OrderCreated.class)
  public void onOrderCreated(OrderCreated event, Message message) {
    System.out.println("new order: " + event.orderId());
  }
}
```

```java
client.subscriber().registerSubscriber(new OrderListener());
```

Key-Value:

```java
KvStore kv = new KvStoreImpl(client, new GsonCodec(), executor);
kv.init("users");
kv.put("users", "marek", new User("marek"));
Optional<User> user = kv.get("users", "marek", User.class);
```

## Build

```bash
./gradlew build
```
