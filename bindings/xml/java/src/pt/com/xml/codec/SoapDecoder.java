package pt.com.xml.codec;

import java.nio.charset.Charset;

import org.caudexorigo.io.UnsynchByteArrayInputStream;

import pt.com.types.NetAccepted;
import pt.com.types.NetAcknowledgeMessage;
import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetMessage;
import pt.com.types.NetPing;
import pt.com.types.NetPoll;
import pt.com.types.NetPong;
import pt.com.types.NetPublish;
import pt.com.types.NetSubscribe;
import pt.com.types.NetUnsubscribe;
import pt.com.types.SimpleFramingDecoder;
import pt.com.xml.Acknowledge;
import pt.com.xml.BrokerMessage;
import pt.com.xml.Notify;
import pt.com.xml.Poll;
import pt.com.xml.SoapEnvelope;
import pt.com.xml.SoapSerializer;
import pt.com.xml.Unsubscribe;

public class SoapDecoder extends SimpleFramingDecoder
{
	private static final Charset CHARSET = Charset.forName("UTF-8");

	public SoapDecoder(int max_message_size)
	{
		super(max_message_size, false);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{

		System.out.println("SoapDecoder.processBody()");
		UnsynchByteArrayInputStream bin = new UnsynchByteArrayInputStream(packet);
		SoapEnvelope msg = SoapSerializer.FromXml(bin);

		NetMessage message = null;

		String actionId = null;
		if (msg.body.notify != null)
		{
			Notify sb = msg.body.notify;
			actionId = sb.actionId;

			NetAction netAction = new NetAction(NetAction.ActionType.SUBSCRIBE);
			NetAction.DestinationType dtype = NetAction.DestinationType.valueOf(sb.destinationType);
			NetSubscribe netSubscribe = new NetSubscribe(sb.destinationName, dtype);
			netSubscribe.setActionId(actionId);

			message = new NetMessage(netAction);
			message.getAction().setSubscribeMessage(netSubscribe);

		}
		else if (msg.body.publish != null)
		{
			BrokerMessage xmsg = msg.body.publish.brokerMessage;
			actionId = msg.body.publish.actionId;

			NetAction netAction = new NetAction(NetAction.ActionType.PUBLISH);
			NetAction.DestinationType dtype = NetAction.DestinationType.TOPIC;

			NetBrokerMessage netBkMsg = new NetBrokerMessage(xmsg.textPayload.getBytes(CHARSET));
			netBkMsg.setMessageId(xmsg.messageId);

			NetPublish netPublish = new NetPublish(xmsg.destinationName, dtype, netBkMsg);
			netPublish.setActionId(actionId);

			message = new NetMessage(netAction);
			message.getAction().setPublishMessage(netPublish);

		}
		else if (msg.body.enqueue != null)
		{
			BrokerMessage xmsg = msg.body.publish.brokerMessage;
			actionId = msg.body.publish.actionId;

			NetAction netAction = new NetAction(NetAction.ActionType.PUBLISH);
			NetAction.DestinationType dtype = NetAction.DestinationType.QUEUE;

			NetBrokerMessage netBkMsg = new NetBrokerMessage(xmsg.textPayload.getBytes(CHARSET));
			netBkMsg.setMessageId(xmsg.messageId);

			NetPublish netPublish = new NetPublish(xmsg.destinationName, dtype, netBkMsg);
			netPublish.setActionId(actionId);

			message = new NetMessage(netAction);
			message.getAction().setPublishMessage(netPublish);
		}
		else if (msg.body.poll != null)
		{
			actionId = msg.body.poll.actionId;
			Poll poll = msg.body.poll;

			NetAction netAction = new NetAction(NetAction.ActionType.POLL);
			NetPoll netPoll = new NetPoll(poll.destinationName);
			netPoll.setActionId(actionId);

			message = new NetMessage(netAction);
			message.getAction().setPollMessage(netPoll);

		}
		else if (msg.body.acknowledge != null)
		{
			actionId = msg.body.acknowledge.actionId;
			Acknowledge ack = msg.body.acknowledge;

			NetAction netAction = new NetAction(NetAction.ActionType.ACKNOWLEDGE_MESSAGE);
			NetAcknowledgeMessage netAck = new NetAcknowledgeMessage(ack.destinationName, ack.messageId);
			netAck.setActionId(actionId);

			message = new NetMessage(netAction);
			message.getAction().setAcknowledgeMessage(netAck);
		}
		else if (msg.body.accepted != null)
		{
			actionId = msg.body.accepted.actionId;
			NetAction netAction = new NetAction(NetAction.ActionType.ACCEPTED);
			NetAccepted netAccepted = new NetAccepted(actionId);
			message = new NetMessage(netAction);
			message.getAction().setAcceptedMessage(netAccepted);
		}
		else if (msg.body.unsubscribe != null)
		{
			Unsubscribe unsubs = msg.body.unsubscribe;
			actionId = msg.body.unsubscribe.actionId;

			NetAction netAction = new NetAction(NetAction.ActionType.UNSUBSCRIBE);
			NetAction.DestinationType dtype = NetAction.DestinationType.valueOf(unsubs.destinationType);
			NetUnsubscribe netUnsubscribe = new NetUnsubscribe(unsubs.destinationName, dtype);
			netUnsubscribe.setActionId(actionId);

			message = new NetMessage(netAction);
			message.getAction().setUnsbuscribeMessage(netUnsubscribe);
		}
		else if (msg.body.checkStatus != null)
		{
			NetAction netAction = new NetAction(NetAction.ActionType.PING);
			NetPing netPing = new NetPing(System.currentTimeMillis());
			message = new NetMessage(netAction);
			message.getAction().setPingMessage(netPing);
		}
		else if (msg.body.status != null)
		{
			NetAction netAction = new NetAction(NetAction.ActionType.PONG);
			NetPong netPong = new NetPong(System.currentTimeMillis());
			message = new NetMessage(netAction);
			message.getAction().setPongMessage(netPong);
		}
		else
		{
			throw new RuntimeException("Not a valid request");
		}
		return message;
	}

}
