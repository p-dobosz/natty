package pl.chunky.nats;

public class NatsException extends RuntimeException {

  public NatsException(final String message) {
    super(message);
  }

  public NatsException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public NatsException(final Throwable cause) {
    super(cause);
  }
}