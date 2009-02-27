package pt.com.thrift.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.thrift.Accepted;
import pt.com.thrift.AcknowledgeMessage;
import pt.com.thrift.Action;
import pt.com.thrift.ActionType;
import pt.com.thrift.BrokerMessage;
import pt.com.thrift.Fault;
import pt.com.thrift.Header;
import pt.com.thrift.Notification;
import pt.com.thrift.Ping;
import pt.com.thrift.Poll;
import pt.com.thrift.Pong;
import pt.com.thrift.Publish;
import pt.com.thrift.Subscribe;
import pt.com.thrift.ThriftMessage;
import pt.com.thrift.Unsubscribe;
import pt.com.types.NetAccepted;
import pt.com.types.NetAcknowledgeMessage;
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
import pt.com.types.SimpleFramingEncoderV2;
import pt.com.types.NetAction.DestinationType;

public class ThriftEncoder extends SimpleFramingEncoderV2
{

	private static final Logger log = LoggerFactory.getLogger(ThriftEncoder.class);

	public ThriftEncoder()
	{

	}

	@Override
	public byte[] processBody(Object message, Short protocolType, Short protocolVersion)
	{
		byte[] result = null;
		if (!(message instanceof NetMessage))
		{
			// TODO: decide what to do with error
			log.error("Error encoding message.");
			return result;
		}

		NetMessage gcsMessage = (NetMessage) message;

		try
		{

			ThriftMessage tm = new ThriftMessage();
			Header header = getHeaders(gcsMessage);
			Action ac = getAction(gcsMessage);

			if (header != null)
				tm.setHeader(header);

			if (ac != null)
				tm.setAction(ac);

			TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
			result = serializer.serialize(tm);

		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
		return result;
	}

	@Override
	public void processBody(Object message, IoBuffer wbuf, Short protocolType, Short protocolVersion)
	{
		if (!(message instanceof NetMessage))
		{
			// TODO: decide what to do with error
			log.error("Error encoding message.");
			return;
		}

		NetMessage gcsMessage = (NetMessage) message;
		try
		{
			ThriftMessage tm = new ThriftMessage();
			Header header = getHeaders(gcsMessage);
			Action ac = getAction(gcsMessage);

			if (header != null)
				tm.setHeader(header);

			if (ac != null)
				tm.setAction(ac);

			TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
			byte[] result = serializer.serialize(tm);

			wbuf.put(result);
		}
		catch (Throwable e)
		{
			// TODO: decide what to do with exception
			log.error("Error parsing Protocol Buffer message.", e.getMessage());
		}
	}

	private Action getAction(NetMessage gcsMessage)
	{
		Action ac = new Action();

		switch (gcsMessage.getAction().getActionType())
		{
		case ACCEPTED:
			ac.setAction_type(ActionType.ACCEPTED);
			ac.setAccepted(getAccepted(gcsMessage));
			break;
		case ACKNOWLEDGE_MESSAGE:
			ac.setAction_type(ActionType.ACKNOWLEDGE_MESSAGE);
			ac.setAck_message(getAcknowledge(gcsMessage));
			break;
		case FAULT:
			ac.setAction_type(ActionType.FAULT);
			ac.setFault(getFault(gcsMessage));
			break;
		case NOTIFICATION:
			ac.setAction_type(ActionType.NOTIFICATION);
			ac.setNotification(getNotification(gcsMessage));
			break;
		case POLL:
			ac.setAction_type(ActionType.POLL);
			ac.setPoll(getPool(gcsMessage));
			break;
		case PUBLISH:
			ac.setAction_type(ActionType.PUBLISH);
			ac.setPublish(getPublish(gcsMessage));
			break;
		case SUBSCRIBE:
			ac.setAction_type(ActionType.SUBSCRIBE);
			ac.setSubscribe(getSubscribe(gcsMessage));
			break;
		case UNSUBSCRIBE:
			ac.setAction_type(ActionType.UNSUBSCRIBE);
			ac.setUnsubscribe(getUnsubscribe(gcsMessage));
			break;
		case PING:
			ac.setAction_type(ActionType.PING);
			ac.setPing(getPing(gcsMessage));
			break;
		case PONG:
			ac.setAction_type(ActionType.PONG);
			ac.setPong(getPong(gcsMessage));
			break;
		}
		return ac;
	}

	private Ping getPing(NetMessage gcsMessage)
	{
		NetPing gcsPing = gcsMessage.getAction().getPingMessage();

		Ping struct = new Ping();
		struct.setTimestamp(gcsPing.getTimestamp());

		return struct;
	}

	private Pong getPong(NetMessage gcsMessage)
	{
		NetPong gcsPong = gcsMessage.getAction().getPongMessage();

		Pong struct = new Pong();
		struct.setTimestamp(gcsPong.getTimestamp());

		return struct;
	}

	private Accepted getAccepted(NetMessage gcsMessage)
	{
		NetAccepted gcsAccepted = gcsMessage.getAction().getAcceptedMessage();
		Accepted struct = new Accepted();
		struct.setAction_id((gcsAccepted.getActionId()));
		return struct;
	}

	private AcknowledgeMessage getAcknowledge(NetMessage gcsMessage)
	{
		NetAcknowledgeMessage net = gcsMessage.getAction().getAcknowledgeMessage();

		AcknowledgeMessage struct = new AcknowledgeMessage();

		struct.setDestination(net.getDestination());
		struct.setMessage_id(net.getMessageId());
		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Fault getFault(NetMessage gcsMessage)
	{
		NetFault net = gcsMessage.getAction().getFaultMessage();

		Fault struct = new Fault();

		struct.setFault_code(net.getCode());
		struct.setFault_message(net.getMessage());

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());
		if (net.getDetail() != null)
			struct.setFault_detail(net.getDetail());

		return struct;
	}

	private Notification getNotification(NetMessage gcsMessage)
	{
		NetNotification net = gcsMessage.getAction().getNotificationMessage();

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

	private Poll getPool(NetMessage gcsMessage)
	{
		NetPoll net = gcsMessage.getAction().getPollMessage();

		Poll struct = new Poll();
		struct.setDestination(net.getDestination());

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Publish getPublish(NetMessage gcsMessage)
	{
		NetPublish net = gcsMessage.getAction().getPublishMessage();

		Publish struct = new Publish();
		struct.setDestination(net.getDestination());
		struct.setMessage(getMessageBroker(net.getMessage()));
		struct.setDestination_type(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Subscribe getSubscribe(NetMessage gcsMessage)
	{
		NetSubscribe net = gcsMessage.getAction().getSubscribeMessage();

		Subscribe struct = new Subscribe();
		struct.setDestination(net.getDestination());
		struct.setDestination_type(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Unsubscribe getUnsubscribe(NetMessage gcsMessage)
	{
		NetUnsubscribe net = gcsMessage.getAction().getUnsbuscribeMessage();

		Unsubscribe struct = new Unsubscribe();
		struct.setDestination(net.getDestination());
		struct.setDestination_type(translate(net.getDestinationType()));

		if (net.getActionId() != null)
			struct.setAction_id(net.getActionId());

		return struct;
	}

	private Header getHeaders(NetMessage gcsMessage)
	{
		Header header = new Header();
		header.setParameters(gcsMessage.getHeaders());

		return header;
	}

	private BrokerMessage getMessageBroker(NetBrokerMessage message)
	{
		BrokerMessage struct = new BrokerMessage();

		struct.setPayload(message.getPayload());

		if (message.getMessageId() != null)
			struct.setMessage_id(message.getMessageId());

		if (message.getExpiration() != -1)
			struct.setExpiration(message.getExpiration());

		if (message.getTimestamp() != -1)
			struct.setTimestamp(message.getTimestamp());

		return struct;
	}

	private int translate(DestinationType destinationType)
	{
		switch (destinationType)
		{
		case QUEUE:
			return pt.com.thrift.DestinationType.QUEUE;
		case TOPIC:
			return pt.com.thrift.DestinationType.TOPIC;
		case VIRTUAL_QUEUE:
			return pt.com.thrift.DestinationType.VIRTUAL_QUEUE;
		}
		// TODO: Throw checked exception
		return pt.com.thrift.DestinationType.TOPIC;
	}



}
