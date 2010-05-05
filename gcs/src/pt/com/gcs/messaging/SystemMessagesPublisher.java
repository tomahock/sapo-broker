package pt.com.gcs.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetMessage;
import pt.com.broker.types.stats.MiscStats;

/**
 * SystemMessagesPublisher is responsible for holding and delivering system messages such as SYSTEM_TOPIC and SYSTEM_QUEUE. If these messages are not acknowledged them are resent.
 */
public class SystemMessagesPublisher
{
	private static class TimeoutMessage
	{
		final NetMessage message;
		long timeout;
		final Channel session;

		TimeoutMessage(NetMessage message, Channel session, long timeout)
		{
			this.message = message;
			this.session = session;
			this.timeout = timeout;
		}
	}

	private static Logger log = LoggerFactory.getLogger(SystemMessagesPublisher.class);

	private static final long ACKNOWLEDGE_INTERVAL = 5 * 1000;

	static
	{
		Runnable command = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					long currentTime = System.currentTimeMillis();
					List<TimeoutMessage> retryMessages = new ArrayList<TimeoutMessage>();

					synchronized (pending_messages)
					{
						for (Map.Entry<String, TimeoutMessage> entry : pending_messages.entrySet())
						{
							if (entry.getValue().timeout <= currentTime)
							{
								retryMessages.add(entry.getValue());
								break;
							}
						}
					}

					for (TimeoutMessage tm : retryMessages)
					{
						log.info("System message with message id '{}' timed out. Remote address: '{}'", tm.message.getAction().getNotificationMessage().getMessage().getMessageId(), tm.session.getRemoteAddress().toString());
						
						if(tm.session.isWritable())
						{
							tm.session.write(tm.message);
						}
						else
						{
							log.warn(String.format("System message with id: '%s' could not be sent. Closing connection.", tm.message.getAction().getNotificationMessage().getMessage().getMessageId()));
							MiscStats.newSystemMessageFailed();
							
							tm.session.close();
						}
						tm.timeout = currentTime + ACKNOWLEDGE_INTERVAL;
					}

				}
				catch (Throwable t)
				{
					log.error("Timeout verification runnable", t);
				}
			}

		};

		GcsExecutor.scheduleAtFixedRate(command, ACKNOWLEDGE_INTERVAL, 500, TimeUnit.MILLISECONDS);
	}

	private static Map<String, TimeoutMessage> pending_messages = new HashMap<String, TimeoutMessage>();

	public static void sendMessage(NetMessage message, Channel channel)
	{
		TimeoutMessage tm = new TimeoutMessage(message, channel, System.currentTimeMillis() + ACKNOWLEDGE_INTERVAL);

		String messageId = message.getAction().getNotificationMessage().getMessage().getMessageId();
		synchronized (pending_messages)
		{
			pending_messages.put(messageId, tm);
		}
		
		if(channel.isWritable())
		{
			channel.write(message);
		}
		else
		{
			log.warn(String.format("System message with id: '%s' could not be sent. Closing connection.", messageId));
			MiscStats.newSystemMessageFailed();
			channel.close();
		}
	}

	public static void sessionClosed(Channel session)
	{
		List<String> message_identifiers = new ArrayList<String>();

		synchronized (pending_messages)
		{
			for (Map.Entry<String, TimeoutMessage> entry : pending_messages.entrySet())
			{
				if (entry.getValue().session.equals(session))
				{
					message_identifiers.add(entry.getValue().message.getAction().getNotificationMessage().getMessage().getMessageId());
				}
			}
			if(message_identifiers.size() !=  0)
			{
				log.info("Removing pending system messages because channel '{}' has closed.", session);
			}
			for (String msgId : message_identifiers)
			{
				pending_messages.remove(msgId);
			}
		}
	}	

	public static void messageAcknowledged(String messageId)
	{
		synchronized (pending_messages)
		{
			pending_messages.remove(messageId);
		}
	}
	
	public static int getPendingMessagesCount()
	{
		synchronized (pending_messages)
		{
			return pending_messages.size();
		}
	}

}
