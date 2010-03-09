package pt.com.broker.messaging;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.InternalMessage;

/**
 * TopicSubscriber represents a local (agent connected) clients who subscribed to a specific topic.
 * 
 */

public class TopicSubscriber extends BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(TopicSubscriber.class);

	public static class ChannelInfo
	{
		public Channel channel;
		public AtomicBoolean discardMessages;

		public ChannelInfo(Channel channel)
		{
			this.channel = channel;
			discardMessages = new AtomicBoolean(false);
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

	}

	private final Set<ChannelInfo> _sessions = new CopyOnWriteArraySet<ChannelInfo>();

	private final String _dname;

	private Object slock = new Object();

	private static final long MAX_WRITE_TIME = 250;

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

	AtomicInteger droppedMessages = new AtomicInteger(0);

	private Channel channel;

	public long onMessage(final InternalMessage amsg)
	{
		if (amsg == null)
			return -1;

		try
		{
			final NetMessage response = BrokerListener.buildNotification(amsg, _dname, DestinationType.TOPIC);
			synchronized (slock)
			{
				for (final ChannelInfo channelInfo : getSessions())
				{
					channel = channelInfo.channel;
					try
					{
						if (channelInfo.channel.isWritable())
						{
							channel.write(response);
							if (channelInfo.discardMessages.get())
							{
								log.info("Stopped discarding messages for topic '{}' and session '{}'.", _dname, channelInfo.channel.toString());
								channelInfo.discardMessages.set(false);
							}
						}
						else
						{

							if (channelInfo.discardMessages.get())
							{
								String message = String.format("Slow client for '%s'. Message with id '%s' will be discarded. Client Address: '%s'. Messages discarded: '%s'", _dname, amsg.getMessageId(), channel.getRemoteAddress().toString(), droppedMessages.incrementAndGet());
								log.warn(message);
								continue;
							}

							ChannelFuture writeFuture = channel.write(response);

							final long writeStartTime = System.nanoTime();
							writeFuture.addListener(new ChannelFutureListener()
							{
								@Override
								public void operationComplete(ChannelFuture channelFuture) throws Exception
								{
									final long writeTime = ((System.nanoTime() - writeStartTime) / (1000 * 1000));

									if (writeTime >= MAX_WRITE_TIME)
									{
										channelInfo.discardMessages.set(true);
										log.info("Started discarding messages for topic '{}' and session '{}'.", _dname, channelInfo.channel.toString());
									}
								}
							});
						}

					}
					catch (Throwable t)
					{
						try
						{
							((BrokerProtocolHandler) channel.getPipeline().get("broker-handler")).exceptionCaught(channel, t, null);
						}
						catch (Throwable t1)
						{
							log.error("Could not propagate error to the client session! Message: {}", t1.getMessage());
						}
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

	public int removeSessionConsumer(Channel channel)
	{
		synchronized (slock)
		{
			if (getSessions().remove(new ChannelInfo(channel)))
			{
				int subscriberCount = getSessions().size();
				log.info("Remove local 'Topic' consumer for subscription: '{}', address: '{}'", _dname, channel.getRemoteAddress().toString());
				log.info("Local subscriber count for '{}': {}", _dname, subscriberCount);
				return subscriberCount;
			}
			return getSessions().size();
		}
	}

	public int addConsumer(Channel channel)
	{
		synchronized (slock)
		{
			if (getSessions().add(new ChannelInfo(channel)))
			{
				int subscriberCount = getSessions().size();
				log.info("Create local 'Topic' consumer for subscription: '{}', address: '{}'", _dname, channel.getRemoteAddress().toString());
				log.info("Local subscriber count for '{}': {}", _dname, subscriberCount);
				return subscriberCount;
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
		synchronized (slock)
		{
			return getSessions().size();
		}
	}
	
	
	@Override
	public boolean ready()
	{
		return true;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	public Set<ChannelInfo> getSessions()
	{
		return _sessions;
	}
}
