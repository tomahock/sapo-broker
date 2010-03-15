package pt.com.broker.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.ForwardResult;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.ForwardResult.Result;

/**
 * QueueSessionListener represents a local (agent connected) clients who subscribed to a specific topic.
 * 
 */
public class QueueSessionListener extends BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(QueueSessionListener.class);
	
	private static final long MAX_WRITE_TIME = 250;

	private static final long RESERVE_TIME = 2 * 60 * 1000; // reserve for 2mn
	
	private static final String ACK_REQUIRED = "ACK_REQUIRED";

	public static class ChannelInfo
	{
		public Channel channel;
		public AtomicLong deliveryTime;
		public volatile boolean wasDeliverySuspeded;
		public final boolean ackRequired;

		public ChannelInfo(Channel channel)
		{
			this(channel, false);
		}
		
		public ChannelInfo(Channel channel, boolean ackRequired)
		{
			this.channel = channel;
			deliveryTime = new AtomicLong(0);
			this.ackRequired = ackRequired; 
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

	private volatile int currentQEP = 0;

	private final List<ChannelInfo> sessions = new ArrayList<ChannelInfo>();

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

	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
	private static final ForwardResult success = new ForwardResult(Result.SUCCESS, RESERVE_TIME);
	private static final ForwardResult ackNotRequired = new ForwardResult(Result.NOT_ACKNOWLEDGE);
	
	public ForwardResult onMessage(final InternalMessage msg)
	{
		if (msg == null)
			return failed;

		final ChannelInfo channelInfo = pick();
		if (channelInfo == null)
		{
			return failed;
		}
		final Channel channel = channelInfo.channel;

		try
		{
			final NetMessage response = BrokerListener.buildNotification(msg, _dname, pt.com.broker.types.NetAction.DestinationType.QUEUE);
			
			if(!channelInfo.ackRequired)
			{
				response.getHeaders().put(ACK_REQUIRED, "false");
			}
			
			if (channel.isWritable())
			{
				channel.write(response);

				channelInfo.wasDeliverySuspeded = false;
			}
			else
			{
				ChannelFuture writeFuture = channel.write(response);
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
								log.info("Suspending message delivery from queue '{}' to session '{}'.", _dname, channelInfo.channel.getRemoteAddress().toString());
							}
							channelInfo.wasDeliverySuspeded = true;
						}
					}
				});
			}
			
			if(channelInfo.ackRequired)
				return success;
			
			return ackNotRequired;
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
				((BrokerProtocolHandler) channel.getPipeline().get("broker-handler")).exceptionCaught(channel, e, null);
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);
			}
		}

		return failed;
	}

	private ChannelInfo pick()
	{
		long currentTime = System.currentTimeMillis();
		synchronized (mutex)
		{
			int n = getSessions().size();
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
					int idx = (currentQEP + i) % n;
					ChannelInfo channelInfo = getSessions().get(idx);
					if (channelInfo.isReady(currentTime))
					{
						return channelInfo;
					}
				}
			}
			catch (Exception e)
			{
				currentQEP = 0;
				return null;
			}
		}
		return null;
	}

	public int addConsumer(Channel channel, boolean ackRequired)
	{
		synchronized (mutex)
		{
			ChannelInfo ci = new ChannelInfo(channel);
			if (!getSessions().contains(ci))
			{
				getSessions().add(ci);

				log.info(String.format("Create message consumer for queue: '%s', address: '%s', Total sessions: '%s'", _dname, channel.getRemoteAddress().toString(), getSessions().size()));
			}
			if(!ackRequired)
			{
				log.info(String.format("Adding queue consumer to '%s' that dosen't require ACK.", this._dname));
			}
			return getSessions().size();
		}
	}

	public int removeSessionConsumer(Channel channel)
	{

		synchronized (mutex)
		{

			if (getSessions().remove(new ChannelInfo(channel)))
			{
				log.info(String.format("Remove message consumer for queue: '%s', address: '%s', Remaining sessions: '%s'", _dname, channel.getRemoteAddress().toString(), getSessions().size()));
			}

			if (getSessions().isEmpty())
			{
				QueueSessionListenerList.remove(_dname);
				Gcs.removeAsyncConsumer(this);
			}

			return getSessions().size();
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
			return getSessions().size();
		}
	}

	@Override
	public boolean ready()
	{
		synchronized (mutex)
		{
			long currentTimeMillis = System.currentTimeMillis();

			for (ChannelInfo channelInfo : getSessions())
			{
				if (channelInfo.isReady(currentTimeMillis))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	public List<ChannelInfo> getSessions()
	{
		return sessions;
	}

}
