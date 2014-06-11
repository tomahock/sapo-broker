package pt.com.broker.client.nio.handlers.timeout;

/**
 * Created by luissantos on 26-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class TimeoutException extends Exception {


    private static final long serialVersionUID = -2755469978068211161L;

    /**
     * <p>Constructor for TimeoutException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public TimeoutException(String message) {
        super(message);
    }
}
