package pt.com.thrift.codec;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.thrift.AcknowledgeMessage;
import pt.com.thrift.Action;
import pt.com.thrift.ActionType;
import pt.com.thrift.BrokerMessage;
import pt.com.thrift.DestinationType;
import pt.com.thrift.Fault;
import pt.com.thrift.Notification;
import pt.com.thrift.Ping;
import pt.com.thrift.Poll;
import pt.com.thrift.Pong;
import pt.com.thrift.Publish;
import pt.com.thrift.Subscribe;
import pt.com.thrift.ThriftMessage;
import pt.com.thrift.Unsubscribe;
import pt.com.types.Constants;
import pt.com.types.NetAccepted;
import pt.com.types.NetAcknowledgeMessage;
import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetPing;
import pt.com.types.NetPoll;
import pt.com.types.NetPong;
import pt.com.types.NetPublish;
import pt.com.types.NetSubscribe;
import pt.com.types.NetUnsubscribe;
import pt.com.types.SimpleFramingDecoderV2;

public class ThriftDecoder extends SimpleFramingDecoderV2
{

	private static final Logger log = LoggerFactory.getLogger(ThriftDecoder.class);

	public ThriftDecoder()
	{
		super(Constants.MAX_MESSAGE_SIZE);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		NetMessage message = null;
		try
		{
			ThriftMessage tm = new ThriftMessage();
			TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
			deserializer.deserialize(tm, packet);
			message = constructMessage(tm);
		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Thrift message.", e);
		}
		return message;
	}

	private NetMessage constructMessage(ThriftMessage tm)
	{

		NetMessage message = new NetMessage(extractAction(tm.getAction()), tm.header.parameters);
		return message;
	}

	private NetAction extractAction(Action action)
	{
		NetAction.ActionType actionType = translateActionType(action.getAction_type());
		NetAction netAction = new NetAction(actionType);

		switch (actionType)
		{
		case ACCEPTED:
			netAction.setAcceptedMessage(extractAcceptedMessage(action));
			break;
		case ACKNOWLEDGE_MESSAGE:
			netAction.setAcknowledgeMessage(extractAcknowledgeMessage(action));
			break;
		case FAULT:
			netAction.setFaultMessage(extractFaultMessage(action));
			break;
		case NOTIFICATION:
			netAction.setNotificationMessage(extractNotificationMessage(action));
			break;
		case POLL:
			netAction.setPollMessage(extractPoolMessage(action));
			break;
		case PUBLISH:
			netAction.setPublishMessage(extractPublishMessage(action));
			break;
		case SUBSCRIBE:
			netAction.setSubscribeMessage(extractSubscribeMessage(action));
			break;
		case UNSUBSCRIBE:
			netAction.setUnsbuscribeMessage(extractUnsubscribeMessage(action));
			break;
		case PING:
			netAction.setPingMessage(extractPingMessage(action));
			break;
		case PONG:
			netAction.setPongMessage(extractPongMessage(action));
			break;

		}
		return netAction;
	}

	private NetPing extractPingMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Ping ping = action.getPing();

		NetPing netPing = new NetPing(ping.getTimestamp());

		return netPing;
	}

	private NetPong extractPongMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Pong pong = action.getPong();

		NetPong netPong = new NetPong(pong.getTimestamp());

		return netPong;
	}

	private NetBrokerMessage obtainBrokerMessage(BrokerMessage message)
	{

		NetBrokerMessage brkMsg = new NetBrokerMessage(message.getPayload());

		brkMsg.setTimestamp(message.getTimestamp());
		brkMsg.setExpiration(message.getExpiration());
		brkMsg.setMessageId(message.getMessage_id());

		return brkMsg;
	}

	static private NetAction.ActionType translateActionType(int actionType)
	{
		switch (actionType)
		{
		case ActionType.ACCEPTED:
			return NetAction.ActionType.ACCEPTED;
		case ActionType.ACKNOWLEDGE_MESSAGE:
			return NetAction.ActionType.ACKNOWLEDGE_MESSAGE;
		case ActionType.FAULT:
			return NetAction.ActionType.FAULT;
		case ActionType.NOTIFICATION:
			return NetAction.ActionType.NOTIFICATION;
		case ActionType.POLL:
			return NetAction.ActionType.POLL;
		case ActionType.PUBLISH:
			return NetAction.ActionType.PUBLISH;
		case ActionType.SUBSCRIBE:
			return NetAction.ActionType.SUBSCRIBE;
		case ActionType.UNSUBSCRIBE:
			return NetAction.ActionType.UNSUBSCRIBE;
		}
		// TODO: Throw checked exception
		return NetAction.ActionType.ACCEPTED;
	}

	static private NetAction.DestinationType translateDestinationType(int destinationType)
	{
		switch (destinationType)
		{
		case DestinationType.QUEUE:
			return NetAction.DestinationType.QUEUE;
		case DestinationType.TOPIC:
			return NetAction.DestinationType.TOPIC;
		case DestinationType.VIRTUAL_QUEUE:
			return NetAction.DestinationType.VIRTUAL_QUEUE;
		}
		// TODO: Throw checked exception
		return NetAction.DestinationType.TOPIC;
	}

	private NetAccepted extractAcceptedMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		NetAccepted netAccepted = new NetAccepted(action.getAccepted().getAction_id());

		return netAccepted;
	}

	private NetAcknowledgeMessage extractAcknowledgeMessage(Action action)
	{
		AcknowledgeMessage ThriftAckMsg = action.getAck_message();
		// TODO: Verify if it's valid. Throw check exception if not
		String destination = ThriftAckMsg.getDestination();
		String messageId = ThriftAckMsg.getMessage_id();
		NetAcknowledgeMessage ackMessage = new NetAcknowledgeMessage(destination, messageId);
		if (action.getAck_message().isSetAction_id())
			ackMessage.setActionId(action.getAck_message().getAction_id());

		return ackMessage;
	}

	private NetFault extractFaultMessage(Action action)
	{
		Fault fault = action.getFault();
		// TODO: Verify if it's valid. Throw check exception if not
		String code = fault.getFault_code();
		String message = fault.getFault_message();

		NetFault netFault = new NetFault(code, message);

		if (fault.isSetAction_id())
			netFault.setActionId(fault.getAction_id());

		if (fault.isSetFault_detail())
			netFault.setDetail(fault.getFault_code());

		return netFault;
	}

	private NetNotification extractNotificationMessage(Action action)
	{
		Notification notification = action.getNotification();
		// TODO: Verify if it's valid. Throw check exception if not

		String dest = notification.getDestination();
		NetAction.DestinationType destType = translateDestinationType(notification.getDestination_type());
		NetBrokerMessage brkMsg = obtainBrokerMessage(notification.getMessage());
		String subs = notification.getSubscription();

		NetNotification netNotification = new NetNotification(dest, destType, brkMsg, subs);

		return netNotification;
	}

	private NetPoll extractPoolMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Poll poll = action.getPoll();
		String destination = poll.getDestination();

		NetPoll pollMsg = new NetPoll(destination);

		if (poll.isSetAction_id())
			pollMsg.setActionId(poll.getAction_id());

		return pollMsg;
	}

	private NetPublish extractPublishMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Publish pub = action.getPublish();

		String dest = pub.getDestination();
		NetAction.DestinationType destType = translateDestinationType(pub.getDestination_type());
		NetBrokerMessage brkMsg = obtainBrokerMessage(pub.getMessage());

		NetPublish netPub = new NetPublish(dest, destType, brkMsg);

		if (pub.isSetAction_id())
			netPub.setActionId(pub.getAction_id());

		return netPub;
	}

	private NetSubscribe extractSubscribeMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Subscribe subs = action.getSubscribe();

		String dest = subs.getDestination();
		NetAction.DestinationType destType = translateDestinationType(subs.getDestination_type());

		NetSubscribe netSubs = new NetSubscribe(dest, destType);

		if (subs.isSetAction_id())
			netSubs.setActionId(subs.getAction_id());

		return netSubs;
	}

	private NetUnsubscribe extractUnsubscribeMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Unsubscribe unsubs = action.getUnsubscribe();

		String dest = unsubs.getDestination();
		NetAction.DestinationType destType = translateDestinationType(unsubs.getDestination_type());

		NetUnsubscribe cgsUnsubs = new NetUnsubscribe(dest, destType);

		if (unsubs.isSetAction_id())
			cgsUnsubs.setActionId(unsubs.getAction_id());

		return cgsUnsubs;
	}
}
