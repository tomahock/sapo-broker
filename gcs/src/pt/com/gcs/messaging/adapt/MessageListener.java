package pt.com.gcs.messaging.adapt;

public interface MessageListener
{
	public boolean onMessage(Message message);

	public String getDestinationName();

	public DestinationType getDestinationType();

}
