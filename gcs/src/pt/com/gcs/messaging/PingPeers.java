package pt.com.gcs.messaging;

import java.nio.charset.Charset;
import java.util.Set;

import org.caudexorigo.ErrorAnalyser;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;

public class PingPeers implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(PingPeers.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");

	@Override
	public void run()
	{
		Set<Channel> peers = Gcs.getManagedConnectorSessions();

		log.info("Pinging peers.");

		for (Channel peer : peers)
		{
			try
			{
				NetBrokerMessage brkMsg = new NetBrokerMessage("ping".getBytes(UTF8));
				brkMsg.setMessageId(MessageId.getMessageId());

				NetNotification notification = new NetNotification("/system/peer", DestinationType.TOPIC, brkMsg, "/system/peer");

				NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
				naction.setNotificationMessage(notification);

				NetMessage nmsg = new NetMessage(naction);
				nmsg.getHeaders().put("TYPE", "PING");

				SystemMessagesPublisher.sendMessage(nmsg, peer);
			}
			catch (Exception e)
			{
				Throwable rootCause = ErrorAnalyser.findRootCause(e);
				log.error("Failed to send ping system message.", rootCause);
			}
		}
	}
}
