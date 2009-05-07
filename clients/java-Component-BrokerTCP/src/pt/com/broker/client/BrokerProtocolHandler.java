package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient.BrokerClientState;
import pt.com.broker.client.messaging.PendingAcceptRequestsManager;
import pt.com.broker.client.net.ProtocolHandler;
import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.authentication.AuthenticationCredentialsProvider;
import pt.com.types.BindingSerializer;
import pt.com.types.NetAccepted;
import pt.com.types.NetAction;
import pt.com.types.NetAuthentication;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetProtocolType;
import pt.com.types.NetAction.ActionType;

public class BrokerProtocolHandler extends ProtocolHandler<NetMessage>
{

	private static final int MAX_NUMBER_OF_TRIES = 15;

	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private final BrokerClient _brokerClient;

	long connectionVersion = 0;

	private final NetworkConnector _connector;
	private final SslNetworkConnector _sslConnector;

	private BindingSerializer serializer;

	private short proto_type = 1;

	private ClientAuthInfo userCredentials;
	private ClientAuthInfo providerCredentials;
	private AuthenticationCredentialsProvider authProvider;

	private HostInfo hostInfo = null;

	public BrokerProtocolHandler(BrokerClient brokerClient, NetProtocolType ptype, String keystoreLocation, char[] keystorePw) throws UnknownHostException, IOException, Throwable
	{
		_brokerClient = brokerClient;

		setHostInfo(brokerClient.getHostInfo());

		_connector = new NetworkConnector(this, getHostInfo());
		_connector.connect();
		if (getHostInfo().getSslPort() != 0)
		{
			_sslConnector = new SslNetworkConnector(this, getHostInfo(), keystoreLocation, keystorePw);
			_sslConnector.connect();
		}
		else
			_sslConnector = null;

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
		_brokerClient.setState(BrokerClientState.OK);
	}

	public BrokerProtocolHandler(BrokerClient brokerClient, String keystoreLocation, char[] keystorePw) throws Throwable
	{
		this(brokerClient, null, keystoreLocation, keystorePw);
	}

	public BrokerProtocolHandler(BrokerClient brokerClient) throws Throwable
	{
		this(brokerClient, null, null, null);
	}

	@Override
	public NetworkConnector getConnector()
	{
		return _connector;
	}

	@Override
	public SslNetworkConnector getSslConnector()
	{
		return _sslConnector;
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
			_brokerClient.sendSubscriptions();
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
			this._brokerClient.setState(BrokerClientState.FAIL);
			int count = 0;

			do
			{
				setHostInfo(_brokerClient.getHostInfo());

				try
				{
					// Close existing connections
					this._connector.close();
					if (_sslConnector != null)
						this._sslConnector.close();

					// Try to reconnected them
					this._brokerClient.setState(BrokerClientState.CONNECT);
					long newConnectionVersion = ++this.connectionVersion;
					this._connector.connect(getHostInfo(), newConnectionVersion);
					if (_sslConnector != null)
						this._sslConnector.connect(getHostInfo(), newConnectionVersion);

					// AUTH
					if (_brokerClient.isAuthenticationRequired())
					{
						this._brokerClient.setState(BrokerClientState.AUTH);
						_brokerClient.obtainCredentials();
						_brokerClient.authenticateClient();
					}

					this._brokerClient.setState(BrokerClientState.OK);

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

			this._brokerClient.setState(BrokerClientState.CLOSE);
			this.notifyAll();

			onError(new Exception("Unable to reconnect after " + MAX_NUMBER_OF_TRIES + " tries!"));
		}

	}

	@Override
	public void onError(Throwable error)
	{
		_brokerClient.getErrorListener().onError(error);
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
			_brokerClient.getErrorListener().onFault(fault);
			break;
		case ACCEPTED:
			NetAccepted accepted = action.getAcceptedMessage();
			PendingAcceptRequestsManager.acceptedMessageReceived(accepted.getActionId());
			break;
		case AUTH:
			NetAuthentication auth = action.getAuthorizationMessage();
			ClientBrokerProtocolHandlerAuthenticationHelper.handleAuthMessage(this, auth);
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
		if (_brokerClient.getState() == BrokerClientState.CLOSE)
		{
			onError(new Exception("Message cannot be sent because clien was closed"));
			return;
		}

		if ((_brokerClient.getState() == BrokerClientState.OK) || ((_brokerClient.getState() == BrokerClientState.AUTH) && message.getAction().getActionType() == ActionType.AUTH))
		{
			super.sendMessage(message);
			return;
		}
		synchronized (this)
		{
			while ((_brokerClient.getState() != BrokerClientState.OK) && (_brokerClient.getState() != BrokerClientState.CLOSE))
			{
				this.wait();
			}
			if ((_brokerClient.getState() == BrokerClientState.OK))
			{
				super.sendMessage(message);
				return;
			}
			// BrokerCliet state is CLOSE. onFailure already invoked. Notify message lost
			onError(new Exception("Message Lost due to failure of agent"));
			log.error("Message Lost due to failure of agent!");
		}
	}

	@Override
	public void sendMessageOverSsl(NetMessage message) throws Throwable
	{
		if (_brokerClient.getState() == BrokerClientState.CLOSE)
		{
			onError(new Exception("Message cannot be sent because clien was closed"));
			return;
		}

		if ((_brokerClient.getState() == BrokerClientState.OK) || ((_brokerClient.getState() == BrokerClientState.AUTH) && message.getAction().getActionType() == ActionType.AUTH))
		{
			super.sendMessageOverSsl(message);
			return;
		}
		synchronized (this)
		{
			while ((_brokerClient.getState() != BrokerClientState.OK) && (_brokerClient.getState() != BrokerClientState.CLOSE))
			{
				this.wait();
			}
			if ((_brokerClient.getState() == BrokerClientState.OK))
			{
				super.sendMessageOverSsl(message);
				return;
			}
			// BrokerCliet state is CLOSE. onFailure already invoked. Notify message lost
			onError(new Exception("Message Lost due to failure of agent"));
			log.error("Message Lost due to failure of agent!");
		}
	}

	public void setCredentials(ClientAuthInfo userCredentials, ClientAuthInfo providerCredentials, AuthenticationCredentialsProvider authProvider)
	{
		this.userCredentials = userCredentials;
		this.providerCredentials = providerCredentials;
		this.authProvider = authProvider;

		// TODO: Resolve the credentials renovation issue
	}

	public BrokerClient getBrokerClient()
	{
		return _brokerClient;
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
