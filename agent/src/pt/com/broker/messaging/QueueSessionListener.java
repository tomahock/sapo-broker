package pt.com.broker.messaging;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.net.IoSessionHelper;

/**
 * QueueSessionListener represents a local (agent connected) clients who subscribed to a specific topic.
 * 
 */
public class QueueSessionListener extends BrokerListener
{
	private int currentQEP = 0;

	private static final Logger log = LoggerFactory.getLogger(QueueSessionListener.class);

	private final List<IoSession> _sessions = new ArrayList<IoSession>();

	private final String _dname;

	private final Object mutex = new Object();

	public QueueSessionListener(String destinationName)
	{
		_dname = destinationName;
	}

	@Override
	public DestinationType getSourceDestinationType()
	{
		return DestinationType.QUEUE;
	}

	@Override
	public DestinationType getTargetDestinationType()
	{
		return DestinationType.QUEUE;
	}

	public long onMessage(final InternalMessage msg)
	{
		if (msg == null)
			return -1;

		final IoSession ioSession = pick();

		try
		{
			if (ioSession != null)
			{
				if (ioSession.isConnected() && !ioSession.isClosing())
				{
					if (ioSession.getScheduledWriteBytes() > MAX_SESSION_BUFFER_SIZE)
					{
						if (log.isDebugEnabled())
						{
							log.debug("MAX_SESSION_BUFFER_SIZE reached in session '{}'", ioSession.toString());
						}
						return -1;
					}
					final NetMessage response = BrokerListener.buildNotification(msg, _dname, pt.com.broker.types.NetAction.DestinationType.QUEUE);
					ioSession.write(response);

					return 2 * 60 * 1000; // reserve for 2mn
				}
			}
		}
		catch (Throwable e)
		{
			if (e instanceof org.jibx.runtime.JiBXException)
			{
				Gcs.ackMessage(_dname, msg.getMessageId());
				log.warn("Undeliverable message was deleted. Id: '{}'", msg.getMessageId());
			}

			try
			{
				(ioSession.getHandler()).exceptionCaught(ioSession, e);
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);
			}
		}

		return -1;
	}

	private IoSession pick()
	{
		synchronized (mutex)
		{
			int n = _sessions.size();
			if (n == 0)
				return null;

			if (currentQEP == (n - 1))
			{
				currentQEP = 0;
			}
			else
			{
				++currentQEP;
			}

			try
			{
				IoSession session = _sessions.get(currentQEP);

				return session;
			}
			catch (Exception e)
			{
				try
				{
					currentQEP = 0;
					return _sessions.get(currentQEP);
				}
				catch (Exception e2)
				{
					return null;
				}
			}
		}
	}

	public int addConsumer(IoSession iosession)
	{
		synchronized (mutex)
		{
			if (!_sessions.contains(iosession))
			{
				_sessions.add(iosession);

				log.info(String.format("Create message consumer for queue: '%s', address: '%s', Total sessions: '%s'", _dname, IoSessionHelper.getRemoteAddress(iosession), _sessions.size()));
			}
			return _sessions.size();
		}
	}

	public int removeSessionConsumer(IoSession iosession)
	{
		synchronized (mutex)
		{
			if (_sessions.remove(iosession))
			{
				log.info(String.format("Remove message consumer for queue: '%s', address: '%s', Remaining sessions: '%s'", _dname, IoSessionHelper.getRemoteAddress(iosession), _sessions.size()));
			}

			if (_sessions.isEmpty())
			{
				QueueSessionListenerList.remove(_dname);
				Gcs.removeAsyncConsumer(this);
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
		synchronized (mutex)
		{
			return _sessions.size();
		}
	}

	@Override
	public boolean ready()
	{
		return true;
	}
}
