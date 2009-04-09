package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class BrokerProtocolHandler extends ProtocolHandler<NetMessage>
{
	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private final BrokerClient _brokerClient;

	private final NetworkConnector _connector;
	private final SslNetworkConnector _sslConnector;

	private BindingSerializer serializer;

	private short proto_type = 1;

	private ClientAuthInfo userCredentials;
	private ClientAuthInfo providerCredentials;
	private AuthenticationCredentialsProvider authProvider;

	public BrokerProtocolHandler(BrokerClient brokerClient, NetProtocolType ptype, String keystoreLocation, char[] keystorePw) throws UnknownHostException, IOException
	{
		_brokerClient = brokerClient;

		_connector = new NetworkConnector(brokerClient.getHost(), brokerClient.getPort());
		if (brokerClient.getSslPort() != 0)
			_sslConnector = new SslNetworkConnector(brokerClient.getHost(), brokerClient.getSslPort(), keystoreLocation, keystorePw);
		else
			_sslConnector = null;

		try
		{

			if (ptype != null)
			{
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
					proto_type = 1;
					serializer = (BindingSerializer) Class.forName("pt.com.protobuf.codec.ProtoBufBindingSerializer").newInstance();
					break;
				}
			}
			else
			{
				proto_type = 1;
				serializer = (BindingSerializer) Class.forName("pt.com.protobuf.codec.ProtoBufBindingSerializer").newInstance();
			}

		}
		catch (Throwable t)
		{
			log.error("Put the binding implentation of your choice in the classpath and try again");
			Shutdown.now();
		}
	}

	public BrokerProtocolHandler(BrokerClient brokerClient, String keystoreLocation, char[] keystorePw) throws UnknownHostException, IOException
	{
		this(brokerClient, null, keystoreLocation, keystorePw);
	}
	
	public BrokerProtocolHandler(BrokerClient brokerClient) throws UnknownHostException, IOException
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
		if( closed.get() )
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
			NetAccepted accepted = action.getAcceptedMessage();
			//System.out.println("BrokerProtocolHandler.handleReceivedMessage(accepted): " + accepted.getActionId());
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

	public void setCredentials(ClientAuthInfo userCredentials, ClientAuthInfo providerCredentials, AuthenticationCredentialsProvider authProvider)
	{
		this.userCredentials = userCredentials;
		this.providerCredentials = providerCredentials;
		this.authProvider = authProvider;
		
		//TODO: Resolve the credentials renovation issue
	}

	public BrokerClient getBrokerClient()
	{
		return _brokerClient;
	}

}
