package pt.com.broker.client.nio.handlers.timeout;

/**
 * Created by luissantos on 26-05-2014.
 */
public class TimeoutException extends Throwable {

    public TimeoutException(String message) {
        super(message);
    }
}
