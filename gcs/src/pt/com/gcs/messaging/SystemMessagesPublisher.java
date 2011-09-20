package pt.com.gcs.messaging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetMessage;
import pt.com.broker.types.stats.MiscStats;
import pt.com.gcs.conf.GcsInfo;

/**
 * SystemMessagesPublisher is responsible for holding and delivering system messages such as SYSTEM_TOPIC and SYSTEM_QUEUE. If these messages are not acknowledged them are resent.
 */
public class SystemMessagesPublisher
{
	private static Logger log = LoggerFactory.getLogger(SystemMessagesPublisher.class);

	private static ConcurrentHashMap<String, NetMessage> pending_messages = new ConcurrentHashMap<String, NetMessage>();

	private static final String fault_destination = String.format("/system/faults/#%s#", GcsInfo.getAgentName());

	private static final String fault_template = "<s:Envelope xmlns:s='http://www.w3.org/2003/05/soap-envelope' xmlns:w='http://www.w3.org/2005/08/addressing'>%n<s:Header><w:From><w:Address>%s</w:Address></w:From></s:Header>%n<s:Body>%n<s:Fault><s:Code><s:Value>s:Receiver</s:Value></s:Code><s:Reason><s:Text>%s</s:Text></s:Reason></s:Fault>%n</s:Body>%n</s:Envelope>";

	public static void sendMessage(NetMessage message, final Channel channel)
	{
		final String messageId = message.getAction().getNotificationMessage().getMessage().getMessageId();

		if (log.isDebugEnabled())
		{
			String debMsg = String.format("Sending system message. Destination Channel: '%s'. Message payload: '%s'. MsgId: '%s'", channel.toString(), new String(message.getAction().getNotificationMessage().getMessage().getPayload()), messageId);
			log.debug(debMsg);
		}

		if (channel.isWritable())
		{
			pending_messages.put(messageId, message);
			channel.write(message);

			Runnable r = new Runnable()
			{
				public void run()
				{
					NetMessage netMessage = pending_messages.remove(messageId);
					if (netMessage != null)
					{
						// message wasn't removed meanwhile (not acknowledged)

						log.info(String.format("Message with id '%s' wasn't acknowledged. Closing channel '%s'. Message: '%s'", messageId, channel.toString(), new String(netMessage.getAction().getNotificationMessage().getMessage().getPayload())));
						MiscStats.newSystemMessageFailed();
						closeChannel(channel);
					}
				}
			};

			GcsExecutor.schedule(r, 1000, TimeUnit.MILLISECONDS);
		}
		else
		{
			log.info(String.format("Closing channel '%s'. Channel was not writable.", channel.toString()));
			closeChannel(channel);
		}
	}

	private static final void closeChannel(final Channel channel)
	{
		try
		{
			if (!channel.isConnected())
			{
				// Channel already closed
				return;
			}

			ChannelFuture f = channel.close();

			f.awaitUninterruptibly(250, TimeUnit.MILLISECONDS);

			if (!(f.isDone() && f.isSuccess()))
			{
				String message = String.format("Unable to close connection to agent: '%s'", f.getChannel().getRemoteAddress());

				log.error(message);

				String error_message = String.format(fault_template, GcsInfo.getAgentName(), message);
				InternalPublisher.send(fault_destination, String.format(fault_template, GcsInfo.getAgentName(), error_message));
			}
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}

	public static void messageAcknowledged(String messageId)
	{
		pending_messages.remove(messageId);
	}
}