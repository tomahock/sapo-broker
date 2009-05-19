package pt.com.broker.messaging;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalMessage;

public class BrokerSyncConsumer
{
	private static final Logger log = LoggerFactory.getLogger(BrokerSyncConsumer.class);

	public static void poll(NetPoll poll, IoSession ios)
	{
		String pollDest = poll.getDestination();
		if (log.isDebugEnabled())
		{
			log.debug("Poll message from Queue '{}'", pollDest);
		}

		try
		{
			InternalMessage m = Gcs.poll(pollDest);
			if (m == null)
			{
				BrokerExecutor.schedule(new QueuePoller(poll, ios), 1000, TimeUnit.MILLISECONDS);
				return;
			}

			if (!m.getDestination().equals(pollDest))
			{
				throw new IllegalStateException("Poll.destinationName != Message.getDestination()");
			}

			if ((ios != null) && ios.isConnected() && !ios.isClosing())
			{
				final NetMessage response = BrokerListener.buildNotification(m, pollDest, DestinationType.QUEUE);
				ios.write(response);
			}
			else
			{
				Gcs.removeSyncConsumer(pollDest);
			}
		}
		catch (Throwable e)
		{
			try
			{
				(ios.getHandler()).exceptionCaught(ios, e);
			}
			catch (Throwable t)
			{
				throw new RuntimeException(t);
			}
		}
	}
}
