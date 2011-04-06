package pt.com.broker.types;

import pt.com.broker.types.MessageListener.MessageListenerState;

public interface MessageListenerEventChangeHandler
{
	void stateChanged(MessageListener messageListener, MessageListenerState state);
}
