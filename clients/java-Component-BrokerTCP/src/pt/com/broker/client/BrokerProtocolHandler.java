package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BaseBrokerClient.BrokerClientState;
import pt.com.broker.client.messaging.PendingAcceptRequestsManager;
import pt.com.broker.client.net.ProtocolHandler;
import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.authentication.AuthenticationCredentialsProvider;
import pt.com.types.BindingSerializer;
import pt.com.types.NetAccepted;
import pt.com.types.NetAction;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetProtocolType;
import pt.com.types.NetAction.ActionType;

public class BrokerProtocolHandler extends ProtocolHandler<NetMessage>
{

	private static final int MAX_NUMBER_OF_TRIES = 15;

	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private final BaseBrokerClient brokerClient;

	long connectionVersion = 0;

	private final BaseNetworkConnector connector;

	private BindingSerializer serializer;

	private short proto_type = 1;

	
	private HostInfo hostInfo = null;

	public BrokerProtocolHandler(BaseBrokerClient brokerClient, NetProtocolType ptype, BaseNetworkConnector connector) throws UnknownHostException, IOException, Throwable
	{
		this.brokerClient = brokerClient;
		this.connector = connector;

		setHostInfo(brokerClient.getHostInfo());

		connector.connect();

		try
		{
			if (ptype == null)
				ptype = NetProtocolType.PROTOCOL_BUFFER;

			switch (ptype)
			{
			case SOAP:
				proto_type = 0;
				serializer = (BindingSerializer) Class.forName("pt.com.xml.codec.SoapBindingSerializer").newInstance();
				break;
			case PROTOCOL_BUFFER:
				proto_type = 1;
				serializer = (BindingSerializer) Class.forName("pt.com.protobuf.codec.ProtoBufBindingSerializer").newInstance();
				break;
			case THRIFT:
				proto_type = 2;
				serializer = (BindingSerializer) Class.forName("pt.com.thrift.codec.ThriftBindingSerializer").newInstance();
				break;
			default:
				throw new Exception("Invalid Protocol Type: " + ptype);
			}

		}
		catch (Throwable t)
		{
			log.error("Put the binding implentation of your choice in the classpath and try again", t);
			Shutdown.now();
		}
		brokerClient.setState(BrokerClientState.OK);
	}


	@Override
	public BaseNetworkConnector getConnector()
	{
		return connector;
	}


	@Override
	public void onConnectionClose()
	{
		log.debug("Connection Closed");
	}

	@Override
	public void onConnectionOpen()
	{
		if (closed.get())
			return;
		log.debug("Connection Opened");
		try
		{
			brokerClient.sendSubscriptions();
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}

	protected synchronized void onIOFailure(long connectionVersion)
	{
		if (connectionVersion == this.connectionVersion)
		{
			log.warn("onIoFailure -  connectionVersion: '{}'", connectionVersion);
			this.brokerClient.setState(BrokerClientState.FAIL);
			int count = 0;

			do
			{
				setHostInfo(brokerClient.getHostInfo());

				try
				{
					// Close existing connections
					this.connector.close();

					// Try to reconnected them
					this.brokerClient.setState(BrokerClientState.CONNECT);
					long newConnectionVersion = ++this.connectionVersion;
					this.connector.connect(getHostInfo(), newConnectionVersion);

					// AUTH
					
					//TODO: resolve this
					
//					if (brokerClient.isAuthenticationRequired())
//					{
//						this.brokerClient.setState(BrokerClientState.AUTH);
//						brokerClient.obtainCredentials();
//						brokerClient.authenticateClient();
//					}

					this.brokerClient.setState(BrokerClientState.OK);

					// READ THREADS
					start();

					// Send subs
					onConnectionOpen();

					this.notifyAll();

					return;

				}
				catch (Throwable t)
				{
					Sleep.time((++count) * 1000);
				}
			}
			while (count != MAX_NUMBER_OF_TRIES);

			this.brokerClient.setState(BrokerClientState.CLOSE);
			this.notifyAll();

			onError(new Exception("Unable to reconnect after " + MAX_NUMBER_OF_TRIES + " tries!"));
		}

	}

	@Override
	public void onError(Throwable error)
	{
		brokerClient.getErrorListener().onError(error);
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
				brokerClient.notifyListener(notification);
			}
			break;
		case PONG:
			try
			{
				brokerClient.feedStatusConsumer(action.getPongMessage());
			}
			catch (Throwable e)
			{
				// TODO decide what to do with exception
				e.printStackTrace();
			}
			break;
		case FAULT:
			NetFault fault = action.getFaultMessage();
			brokerClient.getErrorListener().onFault(fault);
			break;
		case ACCEPTED:
			NetAccepted accepted = action.getAcceptedMessage();
			PendingAcceptRequestsManager.acceptedMessageReceived(accepted.getActionId());
			break;
		default:
			throw new RuntimeException("Unexepected ActionType in received message. ActionType: " + action.getActionType());

		}
	}

	@Override
	public NetMessage decode(DataInputStream in) throws IOException
	{
		short protocolType = in.readShort();
		short protocolVersion = in.readShort();
		int len = in.readInt();

		if (serializer == null)
		{
			throw new RuntimeException("Received message uses an unknown encoding");
		}

		byte[] data = new byte[len];
		in.readFully(data);

		NetMessage message = (NetMessage) serializer.unmarshal(data);
		return message;
	}

	@Override
	public void encode(NetMessage message, DataOutputStream out) throws IOException
	{
		short protocolType = proto_type;
		short protocolVersion = (short) 0;

		byte[] encodedMsg = serializer.marshal(message);

		out.writeShort(protocolType);
		out.writeShort(protocolVersion);
		out.writeInt(encodedMsg.length);

		out.write(encodedMsg);
	}

	@Override
	public void sendMessage(final NetMessage message) throws Throwable
	{
		if (brokerClient.getState() == BrokerClientState.CLOSE)
		{
			onError(new Exception("Message cannot be sent because clien was closed"));
			return;
		}

		if ((brokerClient.getState() == BrokerClientState.OK) || ((brokerClient.getState() == BrokerClientState.AUTH) && message.getAction().getActionType() == ActionType.AUTH))
		{
			super.sendMessage(message);
			return;
		}
		synchronized (this)
		{
			while ((brokerClient.getState() != BrokerClientState.OK) && (brokerClient.getState() != BrokerClientState.CLOSE))
			{
				this.wait();
			}
			if ((brokerClient.getState() == BrokerClientState.OK))
			{
				super.sendMessage(message);
				return;
			}
			// BrokerCliet state is CLOSE. onFailure already invoked. Notify message lost
			onError(new Exception("Message Lost due to failure of agent"));
			log.error("Message Lost due to failure of agent!");
		}
	}
	
	public BaseBrokerClient getBrokerClient()
	{
		return brokerClient;
	}

	public void setHostInfo(HostInfo hostInfo)
	{
		this.hostInfo = hostInfo;
	}

	public HostInfo getHostInfo()
	{
		return hostInfo;
	}

}
