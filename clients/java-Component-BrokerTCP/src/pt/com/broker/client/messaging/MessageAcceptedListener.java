package pt.com.broker.client.messaging;

import pt.com.broker.types.NetFault;

public interface MessageAcceptedListener
{
	void messageAccepted(String actionId);

	void messageTimedout(String actionId);

	void messageFailed(NetFault fault);
}
