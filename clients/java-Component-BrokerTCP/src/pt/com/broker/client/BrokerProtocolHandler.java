package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.net.ProtocolHandler;
import pt.com.protobuf.codec.ProtoBufDecoder;
import pt.com.protobuf.codec.ProtoBufEncoder;
import pt.com.types.NetAction;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;

public class BrokerProtocolHandler extends ProtocolHandler<NetMessage>
{
	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private final BrokerClient _brokerClient;

	private final NetworkConnector _connector;

	private ProtoBufDecoder decoder = new ProtoBufDecoder(4 * 1024);
	ProtoBufEncoder encoder = new ProtoBufEncoder();

	public BrokerProtocolHandler(BrokerClient brokerClient) throws UnknownHostException, IOException
	{
		_brokerClient = brokerClient;

		_connector = new NetworkConnector(brokerClient.getHost(), brokerClient.getPort());
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
		int sizeHeader = in.readInt();
		int flag = (sizeHeader & (1 << 31));
		if (flag == 0)
		{
			throw new RuntimeException("Received message does not have an extended size header");
		}
		short protocolType = in.readShort();
		if (protocolType != 1)
		{
			throw new RuntimeException("Received message was not coded using ProtoBuf encoding");
		}
		short protocolVersion = in.readShort();

		int len = (sizeHeader ^ (1 << 31));

		byte[] data = new byte[len];
		in.readFully(data);

		NetMessage message = (NetMessage) decoder.processBody(data, protocolType, protocolVersion);
		return message;
	}

	@Override
	public void encode(NetMessage message, DataOutputStream out) throws IOException
	{
		byte[] encodedMsg = encoder.processBody(message, (short) 1, (short) 0);
		out.writeInt(encodedMsg.length | (1 << 31));
		out.writeShort(1);
		out.writeShort(0);
		out.write(encodedMsg);
	}
}
