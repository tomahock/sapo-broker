package pt.com.broker.types;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageListenerBase implements MessageListener
{
	private static final Logger log = LoggerFactory.getLogger(MessageListenerBase.class);

	private CopyOnWriteArrayList<MessageListenerEventChangeHandler> handlers = new CopyOnWriteArrayList<MessageListenerEventChangeHandler>();

	@Override
	public void addStateChangeListener(MessageListenerEventChangeHandler handler)
	{
		handlers.add(handler);
	}

	@Override
	public void removeStateChangeListener(MessageListenerEventChangeHandler handler)
	{
		handlers.remove(handler);
	}

	protected void onEventChange(MessageListenerState state)
	{
		for (MessageListenerEventChangeHandler eventHandler : handlers)
		{
			try
			{
				eventHandler.stateChanged(this, state);
			}
			catch (Throwable t)
			{

				log.error(String.format("Failed to process event handler. New State: '%s'", state.toString()));
			}
		}
	}
}
