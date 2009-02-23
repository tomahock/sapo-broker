package pt.com.protobuf.codec;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.protobuf.codec.PBMessage.Atom;
import pt.com.protobuf.codec.PBMessage.Atom.AcknowledgeMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Action;
import pt.com.protobuf.codec.PBMessage.Atom.BrokerMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Fault;
import pt.com.protobuf.codec.PBMessage.Atom.Header;
import pt.com.protobuf.codec.PBMessage.Atom.Notification;
import pt.com.protobuf.codec.PBMessage.Atom.Parameter;
import pt.com.protobuf.codec.PBMessage.Atom.Poll;
import pt.com.protobuf.codec.PBMessage.Atom.Publish;
import pt.com.protobuf.codec.PBMessage.Atom.Subscribe;
import pt.com.protobuf.codec.PBMessage.Atom.Unsubscribe;
import pt.com.types.NetAccepted;
import pt.com.types.NetAcknowledgeMessage;
import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetParameter;
import pt.com.types.NetPing;
import pt.com.types.NetPoll;
import pt.com.types.NetPong;
import pt.com.types.NetPublish;
import pt.com.types.NetSubscribe;
import pt.com.types.NetUnsubscribe;
import pt.com.types.SimpleFramingDecoder;

public class ProtoBufDecoder extends SimpleFramingDecoder
{

	private static final Logger log = LoggerFactory.getLogger(ProtoBufDecoder.class);

	public ProtoBufDecoder(int max_message_size)
	{
		super(max_message_size);
	}

	@Override
	public Object processBody(byte[] packet, short protocolType, short protocolVersion)
	{
		NetMessage message = null;
		try
		{
			PBMessage.Atom atom = PBMessage.Atom.parseFrom(packet);
			message = constructMessage(atom);
		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
		return message;
	}

	private NetMessage constructMessage(PBMessage.Atom atom)
	{
		List<NetParameter> params = null;
		if (atom.hasHeader())
		{
			params = extractParameters(atom.getHeader());
		}

		NetMessage message = new NetMessage(extractAction(atom.getAction()), params);
		return message;
	}

	private List<NetParameter> extractParameters(Header header)
	{

		int paramsCount = header.getParameterCount();

		List<NetParameter> paramsList = new ArrayList<NetParameter>(paramsCount);

		for (int i = 0; i != paramsCount; ++i)
		{
			Parameter param = header.getParameter(i);
			paramsList.add(new NetParameter(param.getName(), param.getValue()));
		}

		return paramsList;
	}

	private NetAction extractAction(Action action)
	{
		NetAction.ActionType actionType = translate(action.getActionType());
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
		Atom.Ping ping = action.getPing();

		NetPing netPing = new NetPing(ping.getTimestamp());

		return netPing;
	}

	private NetPong extractPongMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Atom.Pong pong = action.getPong();

		NetPong netPong = new NetPong(pong.getTimestamp());

		return netPong;
	}

	private NetBrokerMessage obtainBrokerMessage(BrokerMessage message)
	{

		NetBrokerMessage brkMsg = new NetBrokerMessage(message.getPayload().toByteArray());

		if (message.hasTimestamp())
			brkMsg.setTimestamp(message.getTimestamp());
		if (message.hasExpiration())
			brkMsg.setExpiration(message.getExpiration());
		if (message.hasMessageId())
			brkMsg.setMessageId(message.getMessageId());

		return brkMsg;
	}

	static private NetAction.ActionType translate(PBMessage.Atom.Action.ActionType actionType)
	{
		switch (actionType)
		{
		case ACCEPTED:
			return NetAction.ActionType.ACCEPTED;
		case ACKNOWLEDGE_MESSAGE:
			return NetAction.ActionType.ACKNOWLEDGE_MESSAGE;
		case FAULT:
			return NetAction.ActionType.FAULT;
		case NOTIFICATION:
			return NetAction.ActionType.NOTIFICATION;
		case POLL:
			return NetAction.ActionType.POLL;
		case PUBLISH:
			return NetAction.ActionType.PUBLISH;
		case SUBSCRIBE:
			return NetAction.ActionType.SUBSCRIBE;
		case UNSUBSCRIBE:
			return NetAction.ActionType.UNSUBSCRIBE;
		}
		// TODO: Throw checked exception
		return NetAction.ActionType.ACCEPTED;
	}

	static private NetAction.DestinationType translate(PBMessage.Atom.DestinationType destinationType)
	{
		switch (destinationType)
		{
		case QUEUE:
			return NetAction.DestinationType.QUEUE;
		case TOPIC:
			return NetAction.DestinationType.TOPIC;
		case VIRTUAL_QUEUE:
			return NetAction.DestinationType.VIRTUAL_QUEUE;
		}
		// TODO: Throw checked exception
		return NetAction.DestinationType.TOPIC;
	}

	private NetAccepted extractAcceptedMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		NetAccepted netAccepted = new NetAccepted(action.getAccepted().getActionId());

		return netAccepted;
	}

	private NetAcknowledgeMessage extractAcknowledgeMessage(Action action)
	{
		AcknowledgeMessage protoBufAckMsg = action.getAckMessage();
		// TODO: Verify if it's valid. Throw check exception if not
		String destination = protoBufAckMsg.getDestination();
		String messageId = protoBufAckMsg.getMessageId();
		NetAcknowledgeMessage ackMessage = new NetAcknowledgeMessage(destination, messageId);
		if (action.getAckMessage().hasActionId())
			ackMessage.setActionId(action.getAckMessage().getActionId());

		return ackMessage;
	}

	private NetFault extractFaultMessage(Action action)
	{
		Fault fault = action.getFault();
		// TODO: Verify if it's valid. Throw check exception if not
		String code = fault.getFaultCode();
		String message = fault.getFaultMessage();

		NetFault netFault = new NetFault(code, message);

		if (fault.hasActionId())
			netFault.setActionId(fault.getActionId());

		if (fault.hasFaultDetail())
			netFault.setDetail(fault.getFaultCode());

		return netFault;
	}

	private NetNotification extractNotificationMessage(Action action)
	{
		Notification notification = action.getNotification();
		// TODO: Verify if it's valid. Throw check exception if not

		String dest = notification.getDestination();
		NetAction.DestinationType destType = translate(notification.getDestinationType());
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

		if (poll.hasActionId())
			pollMsg.setActionId(poll.getActionId());

		return pollMsg;
	}

	private NetPublish extractPublishMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Publish pub = action.getPublish();

		String dest = pub.getDestination();
		NetAction.DestinationType destType = translate(pub.getDestinationType());
		NetBrokerMessage brkMsg = obtainBrokerMessage(pub.getMessage());

		NetPublish netPub = new NetPublish(dest, destType, brkMsg);

		if (pub.hasActionId())
			netPub.setActionId(pub.getActionId());

		return netPub;
	}

	private NetSubscribe extractSubscribeMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Subscribe subs = action.getSubscribe();

		String dest = subs.getDestination();
		NetAction.DestinationType destType = translate(subs.getDestinationType());

		NetSubscribe netSubs = new NetSubscribe(dest, destType);

		if (subs.hasActionId())
			netSubs.setActionId(subs.getActionId());

		return netSubs;
	}

	private NetUnsubscribe extractUnsubscribeMessage(Action action)
	{
		// TODO: Verify if it's valid. Throw check exception if not
		Unsubscribe unsubs = action.getUnsubscribe();

		String dest = unsubs.getDestination();
		NetAction.DestinationType destType = translate(unsubs.getDestinationType());

		NetUnsubscribe cgsUnsubs = new NetUnsubscribe(dest, destType);

		if (unsubs.hasActionId())
			cgsUnsubs.setActionId(unsubs.getActionId());

		return cgsUnsubs;
	}
}
