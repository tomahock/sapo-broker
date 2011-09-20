package pt.com.broker.codec.thrift;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAcknowledge;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetPing;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetUnsubscribe;
import pt.com.broker.types.stats.EncodingStats;

/**
 * Thrift utility class for encoding and decoding.
 * 
 */

public class ThriftBindingSerializer implements BindingSerializer
{
	private static final Logger log = LoggerFactory.getLogger(ThriftBindingSerializer.class);

	static private NetAction.ActionType translateActionType(pt.com.broker.codec.thrift.ActionType actionType)
	{
		switch (actionType)
		{
		case ACCEPTED:
			return NetAction.ActionType.ACCEPTED;
		case ACKNOWLEDGE:
			return NetAction.ActionType.ACKNOWLEDGE;
		case FAULT:
			return NetAction.ActionType.FAULT;
		case PING:
			return NetAction.ActionType.PING;
		case PONG:
			return NetAction.ActionType.PONG;
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
		case AUTH:
			return NetAction.ActionType.AUTH;
		}
		throw new RuntimeException("Unexpected ActionType: " + actionType);
	}

	static private NetAction.DestinationType translateDestinationType(pt.com.broker.codec.thrift.DestinationType destinationType)
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
		throw new RuntimeException("Unexpected detination type: " + destinationType);
	}

	private NetMessage constructMessage(Atom tm)
	{
		NetMessage message = new NetMessage(extractAction(tm.getAction()), tm.header == null ? null : tm.header.parameters);
		return message;
	}

	private NetAccepted extractAcceptedMessage(Action action)
	{
		NetAccepted netAccepted = new NetAccepted(action.getAccepted().getAction_id());

		return netAccepted;
	}

	private NetAcknowledge extractAcknowledgeMessage(Action action)
	{
		Acknowledge ThriftAckMsg = action.getAck_message();
		String destination = ThriftAckMsg.getDestination();
		String messageId = ThriftAckMsg.getMessage_id();
		NetAcknowledge ackMessage = new NetAcknowledge(destination, messageId);
		if (action.getAck_message().isSetAction_id())
			ackMessage.setActionId(action.getAck_message().getAction_id());

		return ackMessage;
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
		case ACKNOWLEDGE:
			netAction.setAcknowledgeMessage(extractAcknowledgeMessage(action));
			break;
		case FAULT:
			netAction.setFaultMessage(extractFaultMessage(action));
			break;
		case NOTIFICATION:
			netAction.setNotificationMessage(extractNotificationMessage(action));
			break;
		case POLL:
			netAction.setPollMessage(extractPollMessage(action));
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
		case AUTH:
			netAction.setAuthenticationMessage(extractAuthenticationMessage(action));

		}
		return netAction;
	}

	private NetAuthentication extractAuthenticationMessage(Action action)
	{
		Authentication auth = action.getAuth();
		NetAuthentication netAuthentication = new NetAuthentication(auth.getToken(), auth.getAuthentication_type());
		netAuthentication.setActionId(auth.getAction_id());
		netAuthentication.setRoles(auth.getRoles());
		netAuthentication.setUserId(auth.getUser_id());

		return netAuthentication;
	}

	private NetFault extractFaultMessage(Action action)
	{
		Fault fault = action.getFault();
		String code = fault.getFault_code();
		String message = fault.getFault_message();

		NetFault netFault = new NetFault(code, message);

		if (fault.isSetAction_id())
			netFault.setActionId(fault.getAction_id());

		if (fault.isSetFault_detail())
			netFault.setDetail(fault.getFault_detail());

		return netFault;
	}

	private NetNotification extractNotificationMessage(Action action)
	{
		Notification notification = action.getNotification();

		String dest = notification.getDestination();
		NetAction.DestinationType destType = translateDestinationType(notification.getDestination_type());
		NetBrokerMessage brkMsg = obtainBrokerMessage(notification.getMessage());
		String subs = notification.getSubscription();

		NetNotification netNotification = new NetNotification(dest, destType, brkMsg, subs);

		return netNotification;
	}

	private NetPing extractPingMessage(Action action)
	{
		Ping ping = action.getPing();

		NetPing netPing = new NetPing(ping.getAction_id());

		return netPing;
	}

	private NetPoll extractPollMessage(Action action)
	{
		Poll poll = action.getPoll();
		String destination = poll.getDestination();

		NetPoll pollMsg = new NetPoll(destination, poll.getTimeout());

		if (poll.isSetAction_id())
			pollMsg.setActionId(poll.getAction_id());

		return pollMsg;
	}

	private NetPong extractPongMessage(Action action)
	{
		Pong pong = action.getPong();

		NetPong netPong = new NetPong(pong.getAction_id());

		return netPong;
	}

	private NetPublish extractPublishMessage(Action action)
	{
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
		Unsubscribe unsubs = action.getUnsubscribe();

		String dest = unsubs.getDestination();
		NetAction.DestinationType destType = translateDestinationType(unsubs.getDestination_type());

		NetUnsubscribe cgsUnsubs = new NetUnsubscribe(dest, destType);

		if (unsubs.isSetAction_id())
			cgsUnsubs.setActionId(unsubs.getAction_id());

		return cgsUnsubs;
	}

	private Accepted getAccepted(NetMessage netMessage)
	{
		NetAccepted netAccepted = netMessage.getAction().getAcceptedMessage();
		Accepted struct = new Accepted();
		struct.setAction_id((netAccepted.getActionId()));
		return struct;
	}

	private Acknowledge getAcknowledge(NetMessage netMessage)
	{
		NetAcknowledge net = netMessage.getAction().getAcknowledgeMessage();

		Acknowledge struct = new Acknowledge();

		struct.setDestination(net.getDestination());
		struct.setMessage_id(net.getMessageId());
		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Action getAction(NetMessage netMessage)
	{
		Action ac = new Action();

		switch (netMessage.getAction().getActionType())
		{
		case ACCEPTED:
			ac.setAction_type(ActionType.ACCEPTED);
			ac.setAccepted(getAccepted(netMessage));
			break;
		case ACKNOWLEDGE:
			ac.setAction_type(ActionType.ACKNOWLEDGE);
			ac.setAck_message(getAcknowledge(netMessage));
			break;
		case FAULT:
			ac.setAction_type(ActionType.FAULT);
			ac.setFault(getFault(netMessage));
			break;
		case NOTIFICATION:
			ac.setAction_type(ActionType.NOTIFICATION);
			ac.setNotification(getNotification(netMessage));
			break;
		case POLL:
			ac.setAction_type(ActionType.POLL);
			ac.setPoll(getPoll(netMessage));
			break;
		case PUBLISH:
			ac.setAction_type(ActionType.PUBLISH);
			ac.setPublish(getPublish(netMessage));
			break;
		case SUBSCRIBE:
			ac.setAction_type(ActionType.SUBSCRIBE);
			ac.setSubscribe(getSubscribe(netMessage));
			break;
		case UNSUBSCRIBE:
			ac.setAction_type(ActionType.UNSUBSCRIBE);
			ac.setUnsubscribe(getUnsubscribe(netMessage));
			break;
		case PING:
			ac.setAction_type(ActionType.PING);
			ac.setPing(getPing(netMessage));
			break;
		case PONG:
			ac.setAction_type(ActionType.PONG);
			ac.setPong(getPong(netMessage));
			break;
		case AUTH:
			ac.setAction_type(ActionType.AUTH);
			ac.setAuth(getAuth(netMessage));
		}
		return ac;
	}

	private Authentication getAuth(NetMessage netMessage)
	{
		NetAuthentication netAuthentication = netMessage.getAction().getAuthenticationMessage();
		// Authentication auth = new Authentication(netAuthentication.getActionId(), netAuthentication.getAuthenticationType(), netAuthentication.getToken(), netAuthentication.getUserId(), netAuthentication.getRoles());
		Authentication auth = new Authentication();
		auth.setAction_id(netAuthentication.getActionId());
		auth.setAuthentication_type(netAuthentication.getAuthenticationType());
		auth.setToken(netAuthentication.getToken());
		auth.setUser_id(netAuthentication.getUserId());
		auth.setRoles(netAuthentication.getRoles());
		return auth;
	}

	private Fault getFault(NetMessage netMessage)
	{
		NetFault net = netMessage.getAction().getFaultMessage();

		Fault struct = new Fault();

		struct.setFault_code(net.getCode());
		struct.setFault_message(net.getMessage());

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());
		if (net.getDetail() != null)
			struct.setFault_detail(net.getDetail());

		return struct;
	}

	private Header getHeaders(NetMessage netMessage)
	{
		Header header = new Header();
		header.setParameters(netMessage.getHeaders());

		return header;
	}

	private BrokerMessage getMessageBroker(NetBrokerMessage message)
	{
		BrokerMessage struct = new BrokerMessage();

		struct.setPayload(message.getPayload());

		struct.setMessage_id(message.getMessageId());
		struct.setExpiration(message.getExpiration());
		struct.setTimestamp(message.getTimestamp());

		return struct;
	}

	private Notification getNotification(NetMessage netMessage)
	{
		NetNotification net = netMessage.getAction().getNotificationMessage();

		String subs = net.getSubscription();
		if (subs == null)
			subs = "";

		Notification struct = new Notification();
		struct.setDestination(net.getDestination());
		struct.setMessage(getMessageBroker(net.getMessage()));
		struct.setDestination_type(translate(net.getDestinationType()));
		struct.setSubscription(subs);

		return struct;
	}

	private Ping getPing(NetMessage netMessage)
	{
		NetPing netPing = netMessage.getAction().getPingMessage();

		Ping struct = new Ping();
		struct.setAction_id(netPing.getActionId());

		return struct;
	}

	private Poll getPoll(NetMessage netMessage)
	{
		NetPoll net = netMessage.getAction().getPollMessage();

		Poll struct = new Poll();
		struct.setDestination(net.getDestination());

		struct.setTimeout(net.getTimeout());

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Pong getPong(NetMessage netMessage)
	{
		NetPong netPong = netMessage.getAction().getPongMessage();

		Pong struct = new Pong();
		struct.setAction_id(netPong.getActionId());

		return struct;
	}

	private Publish getPublish(NetMessage netMessage)
	{
		NetPublish net = netMessage.getAction().getPublishMessage();

		Publish struct = new Publish();
		struct.setDestination(net.getDestination());
		struct.setMessage(getMessageBroker(net.getMessage()));
		struct.setDestination_type(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Subscribe getSubscribe(NetMessage netMessage)
	{
		NetSubscribe net = netMessage.getAction().getSubscribeMessage();

		Subscribe struct = new Subscribe();
		struct.setDestination(net.getDestination());
		struct.setDestination_type(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Unsubscribe getUnsubscribe(NetMessage netMessage)
	{
		NetUnsubscribe net = netMessage.getAction().getUnsbuscribeMessage();

		Unsubscribe struct = new Unsubscribe();
		struct.setDestination(net.getDestination());
		struct.setDestination_type(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	@Override
	public byte[] marshal(NetMessage netMessage)
	{
		byte[] result = null;

		try
		{
			Atom tm = new Atom();
			Header header = getHeaders(netMessage);
			Action ac = getAction(netMessage);

			if (header != null)
				tm.setHeader(header);

			if (ac != null)
				tm.setAction(ac);

			TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
			result = serializer.serialize(tm);

			EncodingStats.newThriftEncodedMessage();
		}
		catch (Throwable e)
		{
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
		return result;
	}

	@Override
	public void marshal(NetMessage message, OutputStream out)
	{
		try
		{
			out.write(marshal(message));
			EncodingStats.newThriftEncodedMessage();
		}
		catch (Throwable e)
		{
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
	}

	private NetBrokerMessage obtainBrokerMessage(BrokerMessage message)
	{

		NetBrokerMessage brkMsg = new NetBrokerMessage(message.getPayload());

		if (message.getTimestamp() != -1)
			brkMsg.setTimestamp(message.getTimestamp());

		if (message.getExpiration() != -1)
			brkMsg.setExpiration(message.getExpiration());

		if (StringUtils.isNotBlank(message.getMessage_id()))
			brkMsg.setMessageId(message.getMessage_id());

		return brkMsg;
	}

	private pt.com.broker.codec.thrift.DestinationType translate(pt.com.broker.types.NetAction.DestinationType destinationType)
	{
		switch (destinationType)
		{
		case QUEUE:
			return pt.com.broker.codec.thrift.DestinationType.QUEUE;
		case TOPIC:
			return pt.com.broker.codec.thrift.DestinationType.TOPIC;
		case VIRTUAL_QUEUE:
			return pt.com.broker.codec.thrift.DestinationType.VIRTUAL_QUEUE;
		}
		throw new RuntimeException("Unexpected detination type: " + destinationType);
	}

	@Override
	public NetMessage unmarshal(byte[] packet)
	{
		NetMessage message = null;
		try
		{
			Atom tm = new Atom();
			TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
			deserializer.deserialize(tm, packet);
			message = constructMessage(tm);

			EncodingStats.newThriftDecodedMessage();
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
		return message;
	}

	@Override
	public NetMessage unmarshal(InputStream in)
	{
		NetMessage message = null;
		try
		{
			UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();

			int b = 0;
			while ((b = in.read()) < -1)
			{
				out.write(b);
			}
			Atom tm = new Atom();
			TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
			deserializer.deserialize(tm, out.toByteArray());
			message = constructMessage(tm);
			EncodingStats.newThriftDecodedMessage();
		}
		catch (Throwable e)
		{
			log.error("Error parsing Thrift message.", e);
		}
		return message;
	}

	@Override
	public NetProtocolType getProtocolType()
	{
		return NetProtocolType.THRIFT;
	}
}
