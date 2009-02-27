package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.net.ProtocolHandler;
import pt.com.protobuf.codec.ProtoBufDecoder;
import pt.com.protobuf.codec.ProtoBufEncoder;
import pt.com.thrift.codec.ThriftDecoder;
import pt.com.thrift.codec.ThriftEncoder;
import pt.com.types.NetAction;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetProtocolType;
import pt.com.types.SimpleFramingDecoder;
import pt.com.types.SimpleFramingEncoder;
import pt.com.xml.codec.SoapDecoder;
import pt.com.xml.codec.SoapEncoder;

public class BrokerProtocolHandler extends ProtocolHandler<NetMessage>
{
	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private final BrokerClient _brokerClient;

	private final NetworkConnector _connector;

	private final Map decoders = new HashMap();
	private final Map encoders = new HashMap();

	private static final int MAX_SIZE = 4 * 1024;

	private boolean has_extra_header_info = true;
	private short proto_type = 1;

	public BrokerProtocolHandler(BrokerClient brokerClient, NetProtocolType ptype) throws UnknownHostException, IOException
	{
		_brokerClient = brokerClient;

		_connector = new NetworkConnector(brokerClient.getHost(), brokerClient.getPort());

		decoders.put((short) 0, new SoapDecoder(MAX_SIZE));
		decoders.put((short) 1, new ProtoBufDecoder(MAX_SIZE));
		decoders.put((short) 2, new ThriftDecoder(MAX_SIZE));

		encoders.put((short) 0, new SoapEncoder());
		encoders.put((short) 1, new ProtoBufEncoder());
		encoders.put((short) 2, new ThriftEncoder());

		if (ptype != null)
		{
			switch (ptype)
			{
			case SOAP:
				proto_type = 0;
				has_extra_header_info = false;
				break;
			case PROTOCOL_BUFFER:
				proto_type = 1;
				break;
			case THRIFT:
				proto_type = 2;
				break;
			default:
				proto_type = 1;
				break;
			}
		}
	}

	public BrokerProtocolHandler(BrokerClient brokerClient) throws UnknownHostException, IOException
	{
		this(brokerClient, null);
	}

	@Override
	public NetworkConnector getConnector()
	{
		return _connector;
	}

	@Override
	public void onConnectionClose()
	{
		log.debug("Connection Closed");

	}

	@Override
	public void onConnectionOpen()
	{
		log.debug("Connection Opened");
		try
		{
			_brokerClient.sendSubscriptions();
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}

	@Override
	public void onError(Throwable error)
	{
		log.error(error.getMessage(), error);
	}

	@Override
	protected void handleReceivedMessage(NetMessage message)
	{
		NetAction action = message.getAction();

		switch (action.getActionType())
		{
		case NOTIFICATION:
			NetNotification notification = action.getNotificationMessage();
			SyncConsumer sc = SyncConsumerList.get(notification.getDestination());
			if (sc.count() > 0)
			{
				sc.offer(notification);
				sc.decrement();
			}
			else
			{
				_brokerClient.notifyListener(notification);
			}
			break;
		case PONG:
			try
			{
				_brokerClient.feedStatusConsumer(action.getPongMessage());
			}
			catch (Throwable e)
			{
				// TODO decide what to do with exception
				e.printStackTrace();
			}
			break;
		case FAULT:
			NetFault fault = action.getFaultMessage();
			log.error(fault.getMessage());
			// TODO: Probably throwing an exeption is not a good idea
			throw new RuntimeException(fault.getMessage());
		case ACCEPTED:
			// TODO: handle ACK
			// Accepted accepted = request.body.accepted;
			break;
		default:
			throw new RuntimeException("Unexepected ActionType in received message. ActionType: " + action.getActionType());

		}
	}

	@Override
	public NetMessage decode(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		short protocolType;
		short protocolVersion;

		if (has_extra_header_info)
		{
			protocolType = in.readShort();
			protocolVersion = in.readShort();
		}
		else
		{
			protocolType = 0;
			protocolVersion = 0;
		}

		SimpleFramingDecoder decoder = (SimpleFramingDecoder) decoders.get(protocolType);

		if (decoder == null)
		{
			throw new RuntimeException("Received message uses an unknown encoding");
		}

		byte[] data = new byte[len];
		in.readFully(data);
		
		NetMessage message = (NetMessage) decoder.processBody(data, protocolType, protocolVersion);
		return message;
	}

	@Override
	public void encode(NetMessage message, DataOutputStream out) throws IOException
	{
		SimpleFramingEncoder encoder = (SimpleFramingEncoder) encoders.get(proto_type);
		byte[] encodedMsg = encoder.processBody(message, (short) proto_type, (short) 0);
		out.writeInt(encodedMsg.length);
		if (has_extra_header_info)
		{
			out.writeShort(proto_type);
			out.writeShort(0);
		}
		out.write(encodedMsg);
	}
}
