package pt.com.broker.client.messaging;

public interface MessageAcceptedListener
{
	void messageAccepted(String actionId);

	void messageTimedout(String actionId);
}
