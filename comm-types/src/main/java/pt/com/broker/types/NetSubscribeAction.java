package pt.com.broker.types;

/**
 * Created by luissantos on 07-05-2014.
 */
public interface NetSubscribeAction {

    public String getDestination();

    public NetAction.DestinationType getDestinationType();

}
