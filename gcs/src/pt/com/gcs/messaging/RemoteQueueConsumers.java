package pt.com.gcs.messaging;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.messaging.QueueProcessor.ForwardResult;
import pt.com.gcs.messaging.QueueProcessor.ForwardResult.Result;

/**
 * RemoteQueueConsumers maintains current remote queue consumers (other agents).
 * 
 */
public class RemoteQueueConsumers
{
	private static final int WRITE_BUFFER_SIZE = 128 * 1024;

	private final static double MAX_SUSPENSION_TIME = 1000;

	private final static double LOW_WATER_MARK = (double) WRITE_BUFFER_SIZE;
	private final static double HIGH_WATER_MARK = LOW_WATER_MARK * 2;

	private final static double DELTA = HIGH_WATER_MARK - LOW_WATER_MARK;

	private static final long MAX_WRITE_TIME = 250;
	private static final long RESERVE_TIME = 2 * 60 * 1000; // reserve for 2mn

	public static class ChannelInfo
	{
		public Channel channel;
		public AtomicLong deliveryTime;
		public volatile boolean wasDeliverySuspeded;

		public ChannelInfo(Channel channel)
		{
			this.channel = channel;
			deliveryTime = new AtomicLong(0);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((channel == null) ? 0 : channel.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChannelInfo other = (ChannelInfo) obj;
			if (channel == null)
			{
				if (other.channel != null)
					return false;
			}
			else if (!channel.equals(other.channel))
				return false;
			return true;
		}

		public boolean isReady()
		{
			return isReady(System.currentTimeMillis());
		}

		public boolean isReady(long currentTime)
		{
			return currentTime >= deliveryTime.get();
		}
	}

	private static final RemoteQueueConsumers instance = new RemoteQueueConsumers();

	private static Logger log = LoggerFactory.getLogger(RemoteQueueConsumers.class);

	private Map<String, CopyOnWriteArrayList<ChannelInfo>> remoteQueueConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<ChannelInfo>>();

	private int currentQEP = 0;

	private Object rr_mutex = new Object();

	protected synchronized static void add(String queueName, Channel channel)
	{
		CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteQueueConsumers.get(queueName);
		if (sessions == null)
		{
			sessions = new CopyOnWriteArrayList<ChannelInfo>();
		}

		ChannelInfo channelInfo = new ChannelInfo(channel);

		if (!sessions.contains(channelInfo))
		{
			sessions.add(channelInfo);
			log.info("Add remote queue consumer for '{}'", queueName);
		}
		else
		{
			log.info("Remote topic consumer '{}' and session '{}' already exists", queueName, channel.getRemoteAddress().toString());
		}

		instance.remoteQueueConsumers.put(queueName, sessions);
	}

	protected synchronized static void delete(String queueName)
	{
		instance.remoteQueueConsumers.remove(queueName);
	}

	protected static ForwardResult notify(InternalMessage message)
	{
		return instance.doNotify(message);
	}

	protected synchronized static void remove(Channel channel)
	{
		Set<String> keys = instance.remoteQueueConsumers.keySet();
		for (String queueName : keys)
		{
			CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteQueueConsumers.get(queueName);
			if (sessions != null)
			{
				if (sessions.remove(new ChannelInfo(channel)))
				{
					log.info("Remove remote queue consumer for '{}' and session '{}'", queueName, channel.getRemoteAddress().toString());
					instance.remoteQueueConsumers.put(queueName, sessions);
				}
			}
		}
	}

	protected synchronized static void remove(String queueName, Channel channel)
	{
		CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteQueueConsumers.get(queueName);
		if (sessions != null)
		{
			if (sessions.remove(new ChannelInfo(channel)))
			{
				log.info("Remove remote queue consumer for '{}' and session '{}'", queueName, channel.getRemoteAddress().toString());
			}
			instance.remoteQueueConsumers.put(queueName, sessions);
		}
	}

	protected synchronized static int size(String destinationName)
	{
		CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteQueueConsumers.get(destinationName);
		if (sessions != null)
		{
			return sessions.size();
		}
		return 0;
	}

	private RemoteQueueConsumers()
	{
	}


	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
	private static final ForwardResult success = new ForwardResult(Result.SUCCESS, RESERVE_TIME);
	
	protected ForwardResult doNotify(final InternalMessage message)
	{
		final String dname = message.getDestination();

		CopyOnWriteArrayList<ChannelInfo> channels = remoteQueueConsumers.get(dname);
		final ChannelInfo channelInfo = pick(channels);

		if (channelInfo == null)
		{
			return failed;
		}
		final Channel channel = channelInfo.channel;

		if (channel == null)
		{
			return failed;
		}

		try
		{
			if (channel.isWritable())
			{
				channel.write(message);
				channelInfo.wasDeliverySuspeded = false;

				return success;
			}
			else
			{
				ChannelFuture writeFuture = channel.write(message);
				final long writeStartTime = System.nanoTime();

				writeFuture.addListener(new ChannelFutureListener()
				{
					@Override
					public void operationComplete(ChannelFuture future) throws Exception
					{
						final long writeTime = ((System.nanoTime() - writeStartTime) / (1000 * 1000));

						long delayTime = 0;

						if (writeTime >= MAX_WRITE_TIME)
						{
							delayTime = System.currentTimeMillis() + (writeTime / 2); // suspend delivery for the same amount of time that the previous write took.

							channelInfo.deliveryTime.set(delayTime);
							if (!channelInfo.wasDeliverySuspeded)
							{
								log.info("Suspending remote message deliverty from queue '{}' to session '{}'.", dname, channelInfo.channel.toString());
							}
						}
					}
				});
				return success;

			}
		}
		catch (Throwable ct)
		{
			log.error(ct.getMessage(), ct);
			try
			{
				channelInfo.channel.close();
			}
			catch (Throwable ict)
			{
				log.error(ict.getMessage(), ict);
			}
		}

		return failed;
	}

	private ChannelInfo pick(CopyOnWriteArrayList<ChannelInfo> channels)
	{
		synchronized (rr_mutex)
		{
			int n = channels.size();
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
					ChannelInfo sessionInfo = channels.get(currentQEP);
					if (sessionInfo.isReady())
					{
						return sessionInfo;
					}
				}
			}
			catch (Throwable t)
			{
				try
				{
					currentQEP = 0;
					do
					{
						ChannelInfo sessionInfo = channels.get(currentQEP);
						if (sessionInfo.isReady())
						{
							return sessionInfo;
						}
					}
					while ((++currentQEP) != (n - 1));
				}
				catch (Throwable t2)
				{
					return null;
				}

			}
		}
		return null;
	}

	public synchronized static Set<String> getQueueNames()
	{
		return instance.remoteQueueConsumers.keySet();
	}

	public synchronized static CopyOnWriteArrayList<ChannelInfo> getSessions(String queueName)
	{
		return instance.remoteQueueConsumers.get(queueName);
	}

	public static boolean hasReadyRecipients(String destinationName)
	{
		CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteQueueConsumers.get(destinationName);
		if (sessions != null)
		{
			for (ChannelInfo ci : sessions)
				if (ci.isReady())
					return true;
		}
		return false;
	}
}
