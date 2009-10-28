package pt.com.gcs.messaging;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.net.IoSessionHelper;

/**
 * RemoteQueueConsumers maintains current remote queue consumers (other agents).
 * 
 */
class RemoteQueueConsumers
{
	private static final RemoteQueueConsumers instance = new RemoteQueueConsumers();

	private static Logger log = LoggerFactory.getLogger(RemoteQueueConsumers.class);

	private static final int WRITE_BUFFER_SIZE = 1024 * 1024;

	protected synchronized static void add(String queueName, IoSession iosession)
	{
		CopyOnWriteArrayList<IoSession> sessions = instance.remoteQueueConsumers.get(queueName);
		if (sessions == null)
		{
			sessions = new CopyOnWriteArrayList<IoSession>();
		}

		if (!sessions.contains(iosession))
		{
			sessions.add(iosession);
			log.info("Add remote queue consumer for '{}'", queueName);
		}
		else
		{
			log.info("Remote topic consumer '{}' and session '{}' already exists", queueName, IoSessionHelper.getRemoteAddress(iosession));
		}

		instance.remoteQueueConsumers.put(queueName, sessions);
	}

	protected synchronized static void delete(String queueName)
	{
		instance.remoteQueueConsumers.remove(queueName);
	}

	protected static long notify(InternalMessage message)
	{
		return instance.doNotify(message);
	}

	protected synchronized static void remove(IoSession iosession)
	{
		Set<String> keys = instance.remoteQueueConsumers.keySet();
		for (String queueName : keys)
		{
			CopyOnWriteArrayList<IoSession> sessions = instance.remoteQueueConsumers.get(queueName);
			if (sessions != null)
			{
				if (sessions.remove(iosession))
				{
					log.info("Remove remote queue consumer for '{}' and session '{}'", queueName, IoSessionHelper.getRemoteAddress(iosession));
				}
			}
			instance.remoteQueueConsumers.put(queueName, sessions);
		}
	}

	protected synchronized static void remove(String queueName, IoSession iosession)
	{
		CopyOnWriteArrayList<IoSession> sessions = instance.remoteQueueConsumers.get(queueName);
		if (sessions != null)
		{
			if (sessions.remove(iosession))
			{
				log.info("Remove remote queue consumer for '{}' and session '{}'", queueName, IoSessionHelper.getRemoteAddress(iosession));
			}
		}
		instance.remoteQueueConsumers.put(queueName, sessions);
	}

	protected synchronized static int size(String destinationName)
	{
		CopyOnWriteArrayList<IoSession> sessions = instance.remoteQueueConsumers.get(destinationName);
		if (sessions != null)
		{
			return sessions.size();
		}
		return 0;
	}

	private Map<String, CopyOnWriteArrayList<IoSession>> remoteQueueConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<IoSession>>();

	private int currentQEP = 0;

	private Object rr_mutex = new Object();

	private RemoteQueueConsumers()
	{
	}

	protected long doNotify(InternalMessage message)
	{
		CopyOnWriteArrayList<IoSession> sessions = remoteQueueConsumers.get(message.getDestination());
		if (sessions != null)
		{
			int n = sessions.size();

			if (n > 0)
			{
				IoSession ioSession = pick(sessions);
				if (ioSession != null)
				{
					try
					{
						if (ioSession.getScheduledWriteBytes() < WRITE_BUFFER_SIZE)
						{
							ioSession.write(message);
							return 2 * 60 * 1000; // 2mn
						}
						else
						{
							String log_msg = String.format("Write Queue is full, delay message. MessageId: '%s', Destination: '%s', Target Agent: '%s'", message.getMessageId(), message.getDestination(), ioSession.getRemoteAddress().toString());
							log.warn(log_msg);
							
							String dname = String.format("/system/warn/write-queue/#%s#", GcsInfo.getAgentName());
							String info_msg = String.format("%s#%s#%s", message.getMessageId(), message.getDestination(), ioSession.getRemoteAddress().toString());
							InternalPublisher.send(dname, info_msg);
							
							return -1;
						}
					}
					catch (Throwable ct)
					{
						log.error(ct.getMessage(), ct);
						try
						{
							ioSession.close();
						}
						catch (Throwable ict)
						{
							log.error(ict.getMessage(), ict);
						}
					}
				}
			}
		}

		if (log.isDebugEnabled())
		{
			log.debug("There are no remote consumers for queue: {}", message.getDestination());
		}

		return -1;
	}

	private IoSession pick(CopyOnWriteArrayList<IoSession> sessions)
	{
		synchronized (rr_mutex)
		{
			int n = sessions.size();
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
				return sessions.get(currentQEP);
			}
			catch (Throwable t)
			{
				try
				{
					currentQEP = 0;
					return sessions.get(currentQEP);
				}
				catch (Throwable t2)
				{
					return null;
				}

			}
		}
	}
}
