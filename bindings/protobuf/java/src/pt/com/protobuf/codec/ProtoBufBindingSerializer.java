package pt.com.protobuf.codec;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.protobuf.codec.PBMessage.Atom;
import pt.com.protobuf.codec.PBMessage.Atom.Accepted;
import pt.com.protobuf.codec.PBMessage.Atom.AcknowledgeMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Action;
import pt.com.protobuf.codec.PBMessage.Atom.BrokerMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Fault;
import pt.com.protobuf.codec.PBMessage.Atom.Header;
import pt.com.protobuf.codec.PBMessage.Atom.Notification;
import pt.com.protobuf.codec.PBMessage.Atom.Parameter;
import pt.com.protobuf.codec.PBMessage.Atom.Ping;
import pt.com.protobuf.codec.PBMessage.Atom.Poll;
import pt.com.protobuf.codec.PBMessage.Atom.Pong;
import pt.com.protobuf.codec.PBMessage.Atom.Publish;
import pt.com.protobuf.codec.PBMessage.Atom.Subscribe;
import pt.com.protobuf.codec.PBMessage.Atom.Unsubscribe;
import pt.com.types.BindingSerializer;
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
import pt.com.types.NetAction.DestinationType;

import com.google.protobuf.ByteString;

public class ProtoBufBindingSerializer implements BindingSerializer
{

	private static final Logger log = LoggerFactory.getLogger(ProtoBufBindingSerializer.class);

	@Override
	public NetMessage unmarshal(byte[] packet)
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

	@Override
	public byte[] marshal(NetMessage message)
	{
		byte[] result = null;

		try
		{
			Header header = getHeaders(message);

			PBMessage.Atom.Builder atomBuilder = PBMessage.Atom.newBuilder().setAction(getAction(message));
			if (header != null)
				atomBuilder.setHeader(getHeaders(message));

			Atom build = atomBuilder.build();
			result = build.toByteArray();
		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
		return result;
	}

	@Override
	public void marshal(NetMessage message, OutputStream out)
	{
		try
		{
			Header header = getHeaders(message);

			PBMessage.Atom.Builder atomBuilder = PBMessage.Atom.newBuilder().setAction(getAction(message));
			if (header != null)
				atomBuilder.setHeader(getHeaders(message));

			atomBuilder.build().writeTo(out);

		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
	}

	private Action getAction(NetMessage gcsMessage)
	{
		PBMessage.Atom.Action.Builder builder = PBMessage.Atom.Action.newBuilder();

		switch (gcsMessage.getAction().getActionType())
		{
		case ACCEPTED:
			builder.setActionType(PBMessage.Atom.Action.ActionType.ACCEPTED);
			builder.setAccepted(getAccepted(gcsMessage));
			break;
		case ACKNOWLEDGE_MESSAGE:
			builder.setActionType(PBMessage.Atom.Action.ActionType.ACKNOWLEDGE_MESSAGE);
			builder.setAckMessage(getAcknowledge(gcsMessage));
			break;
		case FAULT:
			builder.setActionType(PBMessage.Atom.Action.ActionType.FAULT);
			builder.setFault(getFault(gcsMessage));
			break;
		case NOTIFICATION:
			builder.setActionType(PBMessage.Atom.Action.ActionType.NOTIFICATION);
			builder.setNotification(getNotification(gcsMessage));
			break;
		case POLL:
			builder.setActionType(PBMessage.Atom.Action.ActionType.POLL);
			builder.setPoll(getPool(gcsMessage));
			break;
		case PUBLISH:
			builder.setActionType(PBMessage.Atom.Action.ActionType.PUBLISH);
			builder.setPublish(getPublish(gcsMessage));
			break;
		case SUBSCRIBE:
			builder.setActionType(PBMessage.Atom.Action.ActionType.SUBSCRIBE);
			builder.setSubscribe(getSubscribe(gcsMessage));
			break;
		case UNSUBSCRIBE:
			builder.setActionType(PBMessage.Atom.Action.ActionType.UNSUBSCRIBE);
			builder.setUnsubscribe(getUnsubscribe(gcsMessage));
			break;
		case PING:
			builder.setActionType(PBMessage.Atom.Action.ActionType.PING);
			builder.setPing(getPing(gcsMessage));
			break;
		case PONG:
			builder.setActionType(PBMessage.Atom.Action.ActionType.PONG);
			builder.setPong(getPong(gcsMessage));
			break;
		}
		return builder.build();
	}

	private Ping getPing(NetMessage gcsMessage)
	{
		NetPing gcsPing = gcsMessage.getAction().getPingMessage();

		PBMessage.Atom.Ping.Builder builder = PBMessage.Atom.Ping.newBuilder();
		builder.setTimestamp(gcsPing.getTimestamp());

		return builder.build();
	}

	private Pong getPong(NetMessage gcsMessage)
	{
		NetPong gcsPong = gcsMessage.getAction().getPongMessage();

		PBMessage.Atom.Pong.Builder builder = PBMessage.Atom.Pong.newBuilder();
		builder.setTimestamp(gcsPong.getTimestamp());

		return builder.build();
	}

	private Accepted getAccepted(NetMessage gcsMessage)
	{
		NetAccepted gcsAccepted = gcsMessage.getAction().getAcceptedMessage();

		PBMessage.Atom.Accepted.Builder builder = PBMessage.Atom.Accepted.newBuilder();
		builder.setActionId(gcsAccepted.getActionId());

		return builder.build();
	}

	private AcknowledgeMessage getAcknowledge(NetMessage gcsMessage)
	{
		NetAcknowledgeMessage net = gcsMessage.getAction().getAcknowledgeMessage();

		PBMessage.Atom.AcknowledgeMessage.Builder builder = PBMessage.Atom.AcknowledgeMessage.newBuilder();

		builder.setDestination(net.getDestination()).setMessageId(net.getMessageId());
		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Fault getFault(NetMessage gcsMessage)
	{
		NetFault net = gcsMessage.getAction().getFaultMessage();

		PBMessage.Atom.Fault.Builder builder = PBMessage.Atom.Fault.newBuilder();

		builder.setFaultCode(net.getCode()).setFaultMessage(net.getMessage());

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());
		if (net.getDetail() != null)
			builder.setFaultDetail(net.getDetail());

		return builder.build();
	}

	private PBMessage.Atom.Notification getNotification(NetMessage gcsMessage)
	{
		NetNotification net = gcsMessage.getAction().getNotificationMessage();

		String subs = net.getSubscription();
		if (subs == null)
			subs = "";

		PBMessage.Atom.Notification.Builder builder = PBMessage.Atom.Notification.newBuilder();
		builder.setDestination(net.getDestination()).setMessage(getMessageBroker(net.getMessage())).setDestinationType(translate(net.getDestinationType())).setSubscription(subs);

		return builder.build();
	}

	private Poll getPool(NetMessage gcsMessage)
	{
		NetPoll net = gcsMessage.getAction().getPollMessage();

		PBMessage.Atom.Poll.Builder builder = PBMessage.Atom.Poll.newBuilder();
		builder.setDestination(net.getDestination());

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Publish getPublish(NetMessage gcsMessage)
	{
		NetPublish net = gcsMessage.getAction().getPublishMessage();

		PBMessage.Atom.Publish.Builder builder = PBMessage.Atom.Publish.newBuilder();
		builder.setDestination(net.getDestination()).setMessage(getMessageBroker(net.getMessage())).setDestinationType(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Subscribe getSubscribe(NetMessage gcsMessage)
	{
		NetSubscribe net = gcsMessage.getAction().getSubscribeMessage();

		PBMessage.Atom.Subscribe.Builder builder = PBMessage.Atom.Subscribe.newBuilder();
		builder.setDestination(net.getDestination()).setDestinationType(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private Unsubscribe getUnsubscribe(NetMessage gcsMessage)
	{
		NetUnsubscribe net = gcsMessage.getAction().getUnsbuscribeMessage();

		PBMessage.Atom.Unsubscribe.Builder builder = PBMessage.Atom.Unsubscribe.newBuilder();
		builder.setDestination(net.getDestination()).setDestinationType(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			builder.setActionId(net.getActionId());

		return builder.build();
	}

	private PBMessage.Atom.Header getHeaders(NetMessage gcsMessage)
	{
		PBMessage.Atom.Header.Builder builder = PBMessage.Atom.Header.newBuilder();
		boolean hasParams = false;

		Map<String, String> params = gcsMessage.getHeaders();
		if (params != null)
		{
			Iterator<String> it = params.keySet().iterator();
			while (it.hasNext())
			{
				hasParams = true;
				String k = it.next();
				String v = params.get(k);

				if ((k != null) && (v != null))
					builder.addParameter(PBMessage.Atom.Parameter.newBuilder().setName(k).setValue(v));
			}
		}
		if (hasParams)
			return builder.build();
		return null;
	}

	private BrokerMessage getMessageBroker(NetBrokerMessage message)
	{
		PBMessage.Atom.BrokerMessage.Builder builder = PBMessage.Atom.BrokerMessage.newBuilder();

		builder.setPayload(ByteString.copyFrom(message.getPayload()));

		if (message.getMessageId() != null)
			builder.setMessageId(message.getMessageId());

		if (message.getExpiration() != -1)
			builder.setExpiration(message.getExpiration());

		if (message.getTimestamp() != -1)
			builder.setTimestamp(message.getTimestamp());

		return builder.build();
	}

	private PBMessage.Atom.DestinationType translate(DestinationType destinationType)
	{
		switch (destinationType)
		{
		case QUEUE:
			return PBMessage.Atom.DestinationType.QUEUE;
		case TOPIC:
			return PBMessage.Atom.DestinationType.TOPIC;
		case VIRTUAL_QUEUE:
			return PBMessage.Atom.DestinationType.VIRTUAL_QUEUE;
		}
		// TODO: Throw checked exception
		return PBMessage.Atom.DestinationType.TOPIC;
	}

	private NetMessage constructMessage(PBMessage.Atom atom)
	{
		Map<String, String> parameters = null;
		if (atom.hasHeader())
		{
			parameters = extractParameters(atom.getHeader());
		}

		NetMessage message = new NetMessage(extractAction(atom.getAction()), parameters);
		return message;
	}

	private Map<String, String> extractParameters(Header header)
	{

		int paramsCount = header.getParameterCount();

		Map<String, String> parameters = new HashMap<String, String>();

		for (int i = 0; i != paramsCount; ++i)
		{
			Parameter param = header.getParameter(i);
			parameters.put(param.getName(), param.getValue());
		}

		return parameters;
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
