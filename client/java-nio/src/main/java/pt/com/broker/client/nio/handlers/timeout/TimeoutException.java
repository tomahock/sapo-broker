package pt.com.broker.client.nio.handlers.timeout;

/**
 * Created by luissantos on 26-05-2014.
 */
public class TimeoutException extends Throwable {


    private static final long serialVersionUID = -2755469978068211161L;

    public TimeoutException(String message) {
        super(message);
    }
}
