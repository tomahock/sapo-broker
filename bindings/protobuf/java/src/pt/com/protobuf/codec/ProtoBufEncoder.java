package pt.com.protobuf.codec;

import java.util.Iterator;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.protobuf.codec.PBMessage.Atom;
import pt.com.protobuf.codec.PBMessage.Atom.Accepted;
import pt.com.protobuf.codec.PBMessage.Atom.AcknowledgeMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Action;
import pt.com.protobuf.codec.PBMessage.Atom.BrokerMessage;
import pt.com.protobuf.codec.PBMessage.Atom.Fault;
import pt.com.protobuf.codec.PBMessage.Atom.Header;
import pt.com.protobuf.codec.PBMessage.Atom.Ping;
import pt.com.protobuf.codec.PBMessage.Atom.Poll;
import pt.com.protobuf.codec.PBMessage.Atom.Pong;
import pt.com.protobuf.codec.PBMessage.Atom.Publish;
import pt.com.protobuf.codec.PBMessage.Atom.Subscribe;
import pt.com.protobuf.codec.PBMessage.Atom.Unsubscribe;
import pt.com.types.NetAccepted;
import pt.com.types.NetAcknowledgeMessage;
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
import pt.com.types.SimpleFramingEncoder;
import pt.com.types.NetAction.DestinationType;

import com.google.protobuf.ByteString;

public class ProtoBufEncoder extends SimpleFramingEncoder
{

	private static final Logger log = LoggerFactory.getLogger(ProtoBufEncoder.class);

	public ProtoBufEncoder()
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
			Header header = getHeaders(gcsMessage);

			PBMessage.Atom.Builder atomBuilder = PBMessage.Atom.newBuilder().setAction(getAction(gcsMessage));
			if (header != null)
				atomBuilder.setHeader(getHeaders(gcsMessage));

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
	public void processBody(Object message, ProtocolEncoderOutput pout, Short protocolType, Short protocolVersion)
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
			Header header = getHeaders(gcsMessage);

			PBMessage.Atom.Builder atomBuilder = PBMessage.Atom.newBuilder().setAction(getAction(gcsMessage));
			if (header != null)
				atomBuilder.setHeader(getHeaders(gcsMessage));

			IoBuffer wbuf = IoBuffer.allocate(2048, false);
			wbuf.setAutoExpand(true);
			wbuf.putInt(0);
			wbuf.putShort(protocolType.shortValue());
			wbuf.putShort(protocolVersion.shortValue());
			atomBuilder.build().writeTo(wbuf.asOutputStream());
			wbuf.putInt(0, (wbuf.position() - 8) | 1 << 31);
			wbuf.flip();

			pout.write(wbuf);
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

		Iterator<NetParameter> params = gcsMessage.getHeaders();
		if (params != null)
		{
			while (params.hasNext())
			{
				hasParams = true;
				NetParameter param = params.next();
				if (param != null)
					builder.addParameter(PBMessage.Atom.Parameter.newBuilder().setName(param.getName()).setValue(param.getValue()));
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

}
