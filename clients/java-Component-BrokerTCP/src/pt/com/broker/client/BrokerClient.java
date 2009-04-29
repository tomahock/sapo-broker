package pt.com.broker.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.messaging.BrokerErrorListenter;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.client.messaging.PendingAcceptRequestsManager;
import pt.com.broker.security.SecureSessionContainer;
import pt.com.broker.security.SecureSessionInfo;
import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.authentication.AuthenticationCredentialsProvider;
import pt.com.types.NetAcknowledgeMessage;
import pt.com.types.NetAction;
import pt.com.types.NetAuthentication;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetPing;
import pt.com.types.NetPoll;
import pt.com.types.NetPong;
import pt.com.types.NetProtocolType;
import pt.com.types.NetPublish;
import pt.com.types.NetSubscribe;
import pt.com.types.NetUnsubscribe;
import pt.com.types.NetAction.ActionType;
import pt.com.types.NetAction.DestinationType;
import pt.com.types.NetAuthentication.AuthMessageType;

public class BrokerClient
{
	private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);
	private final String _appName;
	private final ConcurrentMap<String, BrokerListener> _async_listeners = new ConcurrentHashMap<String, BrokerListener>();
	private final BlockingQueue<NetPong> _bstatus = new LinkedBlockingQueue<NetPong>();
	private final List<BrokerAsyncConsumer> _consumerList = new CopyOnWriteArrayList<BrokerAsyncConsumer>();
	private BrokerProtocolHandler _netHandler;
	private final String _host;
	private final int _portNumber;
	private int _sslPortNumber;

	private AuthenticationCredentialsProvider authProvider;
	private ClientAuthInfo userCredentials;
	private ClientAuthInfo providerCredentials;

	private SecureSessionInfo secureSessionInfo;

	private final Object mutex = new Object();
	
	
	private static final BrokerErrorListenter defaultErrorListener = new BrokerErrorListenter(){
		public void onFault(pt.com.types.NetFault fault)
		{
			log.error("Fault message received");
			log.error("	Fault code: '{}'", fault.getCode());
			log.error("	Fault message: '{}'", fault.getMessage());
			log.error("	Fault action identifier: '{}'", fault.getActionId());
			log.error("	Fault detail: '{}'", fault.getDetail());
		}
		public void onError(Throwable throwable)
		{
			log.error("An error occurred", throwable);
		}
	};
	

	private BrokerErrorListenter errorListener; 
	
	public BrokerClient(String host, int portNumber, int sslPortNumber) throws Throwable
	{
		this(host, portNumber, sslPortNumber, "brokerClient");
	}

	public BrokerClient(String host, int portNumber) throws Throwable
	{
		this(host, portNumber, 0, "brokerClient");
	}

	public BrokerClient(String host, int portNumber, int sslPortNumber, String appName) throws Throwable
	{
		this(host, portNumber, sslPortNumber, appName, NetProtocolType.PROTOCOL_BUFFER, null, null);
	}

	public BrokerClient(String host, int portNumber, String appName) throws Throwable
	{
		this(host, portNumber, 0, appName, NetProtocolType.PROTOCOL_BUFFER, null, null);
	}

	public BrokerClient(String host, int portNumber, int sslPortNumber, String appName, NetProtocolType ptype, String keystoreLocation, char[] keystorePw) throws Throwable
	{
		_host = host;
		_portNumber = portNumber;
		_sslPortNumber = sslPortNumber;
		_appName = appName;
		setErrorListener( getDefaultErrorListener() );
		_netHandler = new BrokerProtocolHandler(this, ptype, keystoreLocation, keystorePw);
		getNetHandler().start();
	}

	public BrokerClient(String host, int portNumber, String appName, NetProtocolType ptype) throws Throwable
	{
		this(host, portNumber, 0, appName, ptype, null, null);
	}

	public void setAuthenticationCredentials(AuthenticationCredentialsProvider authProvider, ClientAuthInfo userCredentials) throws Exception
	{
		synchronized (mutex)
		{
			this.authProvider = authProvider;
			this.userCredentials = userCredentials;
			obtainCredentials();
		}
	}

	public void obtainCredentials() throws Exception
	{
		synchronized (mutex)
		{
			if (authProvider == null)
				return;
			if (userCredentials == null)
				return;
			providerCredentials = authProvider.getCredentials(userCredentials);
			if (providerCredentials != null)
			{
				getNetHandler().setCredentials(userCredentials, providerCredentials, authProvider);
			}
		}
	}

	public void authenticateClient() throws Throwable
	{
		if (providerCredentials == null)
			return;

		String localCommId = SecureSessionContainer.getLocalCommunicationId();
		System.out.println("Local comm id: " + localCommId);
		NetAuthentication.AuthClientAuthentication clientAuth = new NetAuthentication.AuthClientAuthentication(providerCredentials.getToken(), localCommId);
		if (providerCredentials.getRoles() != null && providerCredentials.getRoles().size() != 0)
			clientAuth.setRoles(providerCredentials.getRoles());

		if (providerCredentials.getUserAuthenticationType() != null)
			clientAuth.setAuthenticationType(providerCredentials.getUserAuthenticationType());

		if (providerCredentials.getUserId() != null)
			clientAuth.setUserId(providerCredentials.getUserId());

		NetAuthentication netAuth = new NetAuthentication(AuthMessageType.CLIENT_AUTH);
		netAuth.setAuthClientAuthentication(clientAuth);

		NetAction action = new NetAction(ActionType.AUTH);
		action.setAuthenticationMessage(netAuth);

		NetMessage msg = new NetMessage(action);

		SecureSessionInfo ssi = new SecureSessionInfo();
		ssi.setLocalCommunicationId(localCommId);
		ssi.setAuthProvider(authProvider);
		ssi.setUserCredentials(userCredentials);
		ssi.setProviderCredentials(providerCredentials);
		ssi.setBrokerProtocolHandler(getNetHandler());
		ssi.setExpectedMessageType(AuthMessageType.SERVER_CHALLENGE);

		SecureSessionContainer.addInitializingSecureSessionInfo(ssi);

		getNetHandler().sendMessageOverSsl(msg);
	}

	public void acknowledge(NetNotification notification, boolean useSsl, AcceptRequest acceptRequest) throws Throwable
	{

		if ((notification != null) && (notification.getMessage() != null) && (StringUtils.isNotBlank(notification.getMessage().getMessageId())))
		{
			NetBrokerMessage brkMsg = notification.getMessage();
			
			String ackDestination = null;
			if(notification.getDestinationType() != DestinationType.TOPIC)
			{
				ackDestination = notification.getSubscription();
			}
			else 
			{
				ackDestination = notification.getDestination();
			}
			
			NetAcknowledgeMessage ackMsg = new NetAcknowledgeMessage(ackDestination, brkMsg.getMessageId());
			if( acceptRequest != null)
			{
				ackMsg.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}

			NetAction action = new NetAction(ActionType.ACKNOWLEDGE_MESSAGE);
			action.setAcknowledgeMessage(ackMsg);
			NetMessage msg = buildMessage(action);

			if (useSsl)
			{
				getNetHandler().sendMessageOverSsl(msg);
			}
			else
			{
				getNetHandler().sendMessage(msg);
			}
		}
		else
		{
			throw new IllegalArgumentException("Can't acknowledge invalid message.");
		}
	}

	public void acknowledge(NetNotification notification) throws Throwable
	{
		acknowledge(notification, false, null);
	}
	
	public void addAsyncConsumer(NetSubscribe subscribe, BrokerListener listener, boolean useSsl, AcceptRequest acceptRequest) throws Throwable
	{
		if ((subscribe != null) && (StringUtils.isNotBlank(subscribe.getDestination())))
		{
			synchronized (mutex)
			{
				if (_async_listeners.containsKey(subscribe.getDestination()))
				{
					throw new IllegalStateException("A listener for that Destination already exists");
				}

				_async_listeners.put(subscribe.getDestination(), listener);
			}
			if( acceptRequest != null)
			{
				subscribe.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}

			NetAction netAction = new NetAction(ActionType.SUBSCRIBE);
			netAction.setSubscribeMessage(subscribe);

			NetMessage msg = buildMessage(netAction);

			if (useSsl)
			{
				getNetHandler().sendMessageOverSsl(msg);
			}
			else
			{
				getNetHandler().sendMessage(msg);
			}

			_consumerList.add(new BrokerAsyncConsumer(subscribe, listener));
			log.info("Created new async consumer for '{}'", subscribe.getDestination());
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Notification request");
		}
	}

	public void addAsyncConsumer(NetSubscribe subscribe, BrokerListener listener) throws Throwable
	{
		addAsyncConsumer(subscribe, listener, false, null);
	}

	protected void sendSubscriptions() throws Throwable
	{
		for (BrokerAsyncConsumer aconsumer : _consumerList)
		{
			NetSubscribe subscription = aconsumer.getSubscription();

			NetAction netAction = new NetAction(ActionType.SUBSCRIBE);
			netAction.setSubscribeMessage(subscription);

			NetMessage msg = buildMessage(netAction);

			getNetHandler().sendMessage(msg);
			log.info("Reconnected async consumer for '{}'", subscription.getDestination());
		}
	}

	private NetMessage buildMessage(NetAction action)
	{
		NetMessage message = new NetMessage(action, null);

		return message;
	}

	private NetMessage buildMessage(NetAction action, Map<String, String> headers)
	{
		NetMessage message = new NetMessage(action, headers);
		
		return message;
	}

	public NetPong checkStatus() throws Throwable
	{
		String actionId = UUID.randomUUID().toString();
		NetPing ping = new NetPing(actionId);

		NetAction action = new NetAction(ActionType.PING);
		action.setPingMessage(ping);

		NetMessage message = buildMessage(action);

		getNetHandler().sendMessage(message);
	
		long timeout = System.currentTimeMillis() +  ( 2 * 1000);
		NetPong pong = null;
		
		do {
			synchronized (_bstatus)
			{
				Sleep.time(500);
				if( System.currentTimeMillis() > timeout)
					return null;
				pong = _bstatus.peek();
				if(pong == null)
					continue;
				if(!pong.getActionId().equals(NetPong.getUniversalActionId()) && !pong.getActionId().equals(actionId))
				{
					pong = null;
				}
				_bstatus.remove();
			}
		} while (pong == null);

		return pong;
	}

	public void close()
	{
		getNetHandler().stop();
	}

	public void enqueueMessage(NetBrokerMessage brokerMessage, String destinationName, boolean useSsl, AcceptRequest acceptRequest)
	{

		if ((brokerMessage != null) && (StringUtils.isNotBlank(destinationName)))
		{
			NetPublish publish = new NetPublish(destinationName, pt.com.types.NetAction.DestinationType.QUEUE, brokerMessage);
			if( acceptRequest != null)
			{
				publish.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}

			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage msg = buildMessage(action);

			try
			{
				if (useSsl)
					getNetHandler().sendMessageOverSsl(msg);
				else
					getNetHandler().sendMessage(msg);
			}
			catch (Throwable t)
			{
				log.error("Could not acknowledge message, messageId: '{}'", publish.getMessage().getMessageId());
				log.error(t.getMessage(), t);
			}
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Enqueue request");
		}
	}

	public void enqueueMessage(NetBrokerMessage brokerMessage, String destinationName)
	{
		enqueueMessage(brokerMessage, destinationName, false, null);
	}

	protected void feedStatusConsumer(NetPong pong) throws Throwable
	{
		_bstatus.offer(pong);
	}

	public String getHost()
	{
		return _host;
	}

	public int getPort()
	{
		return _portNumber;
	}

	public int getSslPort()
	{
		return _sslPortNumber;
	}

	protected void notifyListener(NetNotification notification)
	{
		for (BrokerAsyncConsumer aconsumer : _consumerList)
		{
			boolean isDelivered = aconsumer.deliver(notification);
			BrokerListener listener = aconsumer.getListener();

			if (listener.isAutoAck() && isDelivered)
			{
				try
				{
					acknowledge(notification);
				}
				catch (Throwable t)
				{
					log.error("Could not acknowledge message, messageId: '{}'", notification.getMessage().getMessageId());
					log.error(t.getMessage(), t);
				}
			}
		}
	}

	public NetNotification poll(String queueName, boolean useSsl, AcceptRequest acceptRequest) throws Throwable
	{
		if (StringUtils.isNotBlank(queueName))
		{
			NetPoll poll = new NetPoll(queueName);
			if( acceptRequest != null)
			{
				poll.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}
			NetAction action = new NetAction(ActionType.POLL);
			action.setPollMessage(poll);

			NetMessage message = buildMessage(action);
			SyncConsumer sc = SyncConsumerList.get(queueName);
			sc.increment();

			if (useSsl)
				getNetHandler().sendMessageOverSsl(message);
			else
				getNetHandler().sendMessage(message);

			NetNotification m = sc.take();
			return m;
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Poll request");
		}
	}

	public NetNotification poll(String queueName) throws Throwable
	{
		return poll(queueName, false, null);
	}

	public void publishMessage(NetBrokerMessage brokerMessage, String destination, boolean useSsl,  AcceptRequest acceptRequest)
	{
		if ((brokerMessage != null) && (StringUtils.isNotBlank(destination)))
		{
			NetPublish publish = new NetPublish(destination, pt.com.types.NetAction.DestinationType.TOPIC, brokerMessage);
			if( acceptRequest != null)
			{
				publish.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}
			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage msg = buildMessage(action);

			try
			{
				if (useSsl)
					getNetHandler().sendMessageOverSsl(msg);
				else
					getNetHandler().sendMessage(msg);
			}
			catch (Throwable e)
			{
				log.error("Could not publish message, messageId:");
				log.error(e.getMessage(), e);
			}
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Publish request");
		}
	}

	public void publishMessage(NetBrokerMessage brokerMessage, String destination)
	{
		publishMessage(brokerMessage, destination, false, null);
	}

	public void unsubscribe(NetAction.DestinationType destinationType, String destinationName, boolean useSsl, AcceptRequest acceptRequest) throws Throwable
	{
		if ((StringUtils.isNotBlank(destinationName)) && (destinationType != null))
		{
			NetUnsubscribe unsubs = new NetUnsubscribe(destinationName, destinationType);
			if( acceptRequest != null)
			{
				unsubs.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}
			NetAction action = new NetAction(ActionType.UNSUBSCRIBE);
			action.setUnsbuscribeMessage(unsubs);

			NetMessage message = buildMessage(action);

			if (useSsl)
				getNetHandler().sendMessageOverSsl(message);
			else
				getNetHandler().sendMessage(message);

			for (BrokerAsyncConsumer bac : _consumerList)
			{
				NetSubscribe n = bac.getSubscription();

				if ((n.getDestination().equals(destinationName)) && (n.getDestinationType() == destinationType))
				{
					_consumerList.remove(bac);
					break;
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Unsubscribe request");
		}
	}

	public void unsubscribe(NetAction.DestinationType destinationType, String destinationName) throws Throwable
	{
		unsubscribe(destinationType, destinationName, false, null);
	}

	public void setSecureSessionInfo(SecureSessionInfo secureSessionInfo)
	{
		this.secureSessionInfo = secureSessionInfo;
	}

	public SecureSessionInfo getSecureSessionInfo()
	{
		return secureSessionInfo;
	}

	public BrokerProtocolHandler getNetHandler()
	{
		return _netHandler;
	}

	public static BrokerErrorListenter getDefaultErrorListener()
	{
		return defaultErrorListener;
	}

	public void setErrorListener(BrokerErrorListenter errorListener)
	{
		this.errorListener = errorListener;
	}

	public BrokerErrorListenter getErrorListener()
	{
		return errorListener;
	}

	// public static void saveToDropbox(String dropboxPath, BrokerMessage
	// brkmsg, DestinationType dtype) throws Throwable
	// {
	// if ((brkmsg != null) && (StringUtils.isNotBlank(brkmsg.destinationName))
	// && (StringUtils.isNotBlank(dropboxPath)))
	// {
	// SoapEnvelope soap = new SoapEnvelope();
	//
	// if (dtype == DestinationType.TOPIC)
	// {
	// Publish pubreq = new Publish();
	// pubreq.brokerMessage = brkmsg;
	// soap.body.publish = pubreq;
	// }
	// else if (dtype == DestinationType.QUEUE)
	// {
	// Enqueue enqreq = new Enqueue();
	// enqreq.brokerMessage = brkmsg;
	// soap.body.enqueue = enqreq;
	// }
	//
	// String baseFileName = dropboxPath + File.separator +
	// UUID.randomUUID().toString();
	// String tempfileName = baseFileName + ".temp";
	// String fileName = baseFileName + ".good";
	//
	// FileOutputStream fos = new FileOutputStream(tempfileName);
	// SoapSerializer.ToXml(soap, fos);
	// fos.flush();
	// fos.close();
	// (new File(tempfileName)).renameTo(new File(fileName));
	// }
	// else
	// {
	// throw new
	// IllegalArgumentException("Missing arguments for Dropbox persistence");
	// }
	// }

}
