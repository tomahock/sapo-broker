package pt.com.broker.messaging;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.mina.core.session.IoSession;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.net.IoSessionHelper;

/**
 * TopicSubscriber represents a local (agent connected) clients who subscribed to a specific topic.
 * 
 */

public class TopicSubscriber extends BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(TopicSubscriber.class);

	private final Set<IoSession> _sessions = new CopyOnWriteArraySet<IoSession>();

	private final String _dname;

	private Object slock = new Object();

	public TopicSubscriber(String destinationName)
	{
		_dname = destinationName;
	}

	@Override
	public DestinationType getSourceDestinationType()
	{
		return DestinationType.TOPIC;
	}
	
	@Override
	public DestinationType getTargetDestinationType()
	{
		return DestinationType.TOPIC;
	}

	public long onMessage(InternalMessage amsg)
	{
		if (amsg == null)
			return -1;

		try
		{
			for (IoSession ios : _sessions)
			{
				try
				{
					if (ios.isConnected() && !ios.isClosing())
					{
						if (ios.getScheduledWriteBytes() > (MAX_SESSION_BUFFER_SIZE))
						{
							if (log.isWarnEnabled())
							{
								String message = String.format("Slow client for '%s'. Message with id '%s' will be discarded. Client Address: '%s'", _dname, amsg.getMessageId(), IoSessionHelper.getRemoteAddress(ios));
								log.warn(message);
							}
						}
						else
						{
							final NetMessage response = BrokerListener.buildNotification(amsg, _dname, DestinationType.TOPIC);
							ios.write(response);
						}
					}
				}
				catch (Throwable t)
				{
					try
					{
						(ios.getHandler()).exceptionCaught(ios, t);
					}
					catch (Throwable t1)
					{
						log.error("Could not propagate error to the client session! Message: {}", t1.getMessage());
					}
				}
			}
		}
		catch (Throwable e)
		{
			log.error("Error on message listener for '{}': {}", e.getMessage(), _dname);
		}
		return 0;
	}

	public int removeSessionConsumer(IoSession iosession)
	{
		synchronized (slock)
		{
			if (_sessions.remove(iosession))
			{
				int subscriberCount = _sessions.size();
				log.info("Remove local 'Topic' consumer for subscription: '{}', address: '{}'", _dname, IoSessionHelper.getRemoteAddress(iosession));
				log.info("Local subscriber count for '{}': {}", _dname, subscriberCount);
				return subscriberCount;
			}
			return _sessions.size();
		}
	}

	public int addConsumer(IoSession iosession)
	{
		synchronized (slock)
		{
			if (_sessions.add(iosession))
			{
				int subscriberCount = _sessions.size();
				log.info("Create local 'Topic' consumer for subscription: '{}', address: '{}'", _dname, IoSessionHelper.getRemoteAddress(iosession));
				log.info("Local subscriber count for '{}': {}", _dname, subscriberCount);
				return subscriberCount;
			}
			return _sessions.size();
		}
	}

	public String getDestinationName()
	{
		return _dname;
	}

	public int count()
	{
		return _sessions.size();
	}

	@Override
	public boolean ready()
	{
		return true;
	}
}
