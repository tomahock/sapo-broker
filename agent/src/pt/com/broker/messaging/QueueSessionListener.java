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

	private final static double MAX_SUSPENSION_TIME = 1000;
	
	private final static double LOW_WATER_MARK = (double) MAX_SESSION_BUFFER_SIZE; 
	private final static double HIGH_WATER_MARK = LOW_WATER_MARK * 2;
	
	private final static double DELTA =  HIGH_WATER_MARK - LOW_WATER_MARK;
	
	private static class SessionInfo
	{
		IoSession session;
		long time;

		SessionInfo(IoSession session)
		{
			this.session = session;
			time = 0;
		}

		boolean isReady()
		{
			return time < System.currentTimeMillis();
		}
	}

	private volatile int currentQEP = 0;

	private static final Logger log = LoggerFactory.getLogger(QueueSessionListener.class);

	private final List<SessionInfo> _sessions = new ArrayList<SessionInfo>();

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

		final SessionInfo sessionInfo = pick();
		
		try
		{
			if ( (sessionInfo != null) && (sessionInfo.session != null) )
			{
				IoSession ioSession = sessionInfo.session;
				if (ioSession.isConnected() && !ioSession.isClosing())
				{
					long scheduledWriteBytes = ioSession.getScheduledWriteBytes();
					
					if( (scheduledWriteBytes> LOW_WATER_MARK) && (scheduledWriteBytes < HIGH_WATER_MARK) )
					{
						long time = (long) ((scheduledWriteBytes - LOW_WATER_MARK ) * (MAX_SUSPENSION_TIME / DELTA));
						sessionInfo.time = System.currentTimeMillis() + time;
					}
					else if(scheduledWriteBytes >= HIGH_WATER_MARK)
					{
						sessionInfo.time = (long) (System.currentTimeMillis() + MAX_SUSPENSION_TIME);
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
				if((sessionInfo != null) && (sessionInfo.session != null) )
				{
					(sessionInfo.session.getHandler()).exceptionCaught(sessionInfo.session , e);
				}
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);
			}
		}

		return -1;
	}

	private SessionInfo pick()
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
				for (int i = 0; i != n; ++i)
				{
					SessionInfo sessionInfo = _sessions.get(currentQEP);
					if(sessionInfo.isReady())
					{
						return sessionInfo;
					}
				}
			}
			catch (Exception e)
			{
				try
				{
					currentQEP = 0;
					do
					{
						SessionInfo sessionInfo = _sessions.get(currentQEP);
						if (sessionInfo.isReady())
						{
							return sessionInfo;
						}
					}
					while ((++currentQEP) != (n - 1));
				}
				catch (Exception e2)
				{
					return null;
				}
			}

		}
		return null;
	}

	public int addConsumer(IoSession iosession)
	{
		synchronized (mutex)
		{
			if (!_sessions.contains(iosession))
			{
				_sessions.add(new SessionInfo(iosession));

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
		synchronized (_sessions)
		{
			for(SessionInfo sessionInfo : _sessions)
			{
				if(sessionInfo.isReady())
					return true;
			}
		}
		return false;
	}
}
