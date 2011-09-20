package pt.com.broker.types;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.channels.ListenerChannel;

/**
 * A MessageListener object is used to receive asynchronously delivered messages.
 */

public interface MessageListener
{
	/**
	 * Passes a message to the listener.
	 * 
	 * @param message
	 *            the message passed to the listener
	 */

	public enum Type
	{
		LOCAL, INTERNAL, REMOTE
	};

	public enum MessageListenerState
	{
		Active, NotActive, Ready, NotReady, Writable, NotWritable
	};

	public ForwardResult onMessage(DeliverableMessage message);

	public ListenerChannel getChannel();

	public String getsubscriptionKey();

	public DestinationType getSourceDestinationType();

	public DestinationType getTargetDestinationType();

	public boolean isReady();

	public boolean isActive();

	public boolean isAckRequired();

	public Type getType();

	public void addStateChangeListener(MessageListenerEventChangeHandler handler);

	public void removeStateChangeListener(MessageListenerEventChangeHandler handler);
}
