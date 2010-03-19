package pt.com.gcs.messaging;

import pt.com.broker.types.NetAction.DestinationType;

/**
 * A MessageListener object is used to receive asynchronously delivered messages. 
 */

public interface MessageListener
{
	/**
	 * Passes a message to the listener.
	 * @param message the message passed to the listener
	 */
	
	public enum Type{LOCAL, INTERNAL, REMOTE};
	
	public ForwardResult onMessage(InternalMessage message);	
	
	public ListenerChannel getChannel();

	public String getsubscriptionKey();

	public DestinationType getSourceDestinationType();
	
	public DestinationType getTargetDestinationType();
	
	public boolean isReady();
	
	public boolean isActive();
	
	public boolean isAckRequired();
	
	public Type getType();
}
