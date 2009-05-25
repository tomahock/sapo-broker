package pt.com.broker.client;

import java.util.Collection;
import java.util.List;
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
import pt.com.broker.client.utils.CircularContainer;
import pt.com.broker.types.NetAcknowledgeMessage;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetPing;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetUnsubscribe;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;

public abstract class BaseBrokerClient
{
	public enum BrokerClientState
	{
		UNSTARTED, CONNECT, OK, AUTH, FAIL, CLOSE;
	}

	public interface BrokerClientStateOk
	{
		void onOk(BrokerClient brokerClient);
	}

	private static final Logger log = LoggerFactory.getLogger(BaseBrokerClient.class);

	protected String _appName;
	protected final ConcurrentMap<String, BrokerListener> _async_listeners = new ConcurrentHashMap<String, BrokerListener>();
	protected final BlockingQueue<NetPong> _bstatus = new LinkedBlockingQueue<NetPong>();
	protected final List<BrokerAsyncConsumer> _consumerList = new CopyOnWriteArrayList<BrokerAsyncConsumer>();

	private NetProtocolType protocolType;
	protected BrokerClientState state = BrokerClientState.UNSTARTED;

	protected BrokerProtocolHandler _netHandler;
	protected CircularContainer<HostInfo> hosts;
//	protected SecureSessionInfo secureSessionInfo;

	protected static final BrokerErrorListenter defaultErrorListener = new BrokerErrorListenter()
	{
		public void onFault(pt.com.broker.types.NetFault fault)
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

	protected BrokerErrorListenter errorListener;

	// Should be called by inherit types
	protected void init() throws Throwable
	{
		state = BrokerClientState.CONNECT;
		setErrorListener(getDefaultErrorListener());
		_netHandler = getBrokerProtocolHandler();
		getNetHandler().start();
		state = BrokerClientState.OK;
	}

	public BaseBrokerClient(String host, int portNumber) throws Throwable
	{
		this(host, portNumber, "BrokerClient", NetProtocolType.PROTOCOL_BUFFER);
	}

	public BaseBrokerClient(String host, int portNumber, String appName) throws Throwable
	{
		this(host, portNumber, appName, NetProtocolType.PROTOCOL_BUFFER);
	}

	public BaseBrokerClient(String host, int portNumber, String appName, NetProtocolType ptype) throws Throwable
	{
		this.hosts = new CircularContainer<HostInfo>(1);
		this.hosts.add(new HostInfo(host, portNumber));
		_appName = appName;
		protocolType = ptype;
	}

	public BaseBrokerClient(Collection<HostInfo> hosts) throws Throwable
	{
		this(hosts, "BrokerClient");
	}

	public BaseBrokerClient(Collection<HostInfo> hosts, String appName) throws Throwable
	{
		this(hosts, appName, NetProtocolType.PROTOCOL_BUFFER);
	}

	public BaseBrokerClient(Collection<HostInfo> hosts, String appName, NetProtocolType ptype) throws Throwable
	{
		this.hosts = new CircularContainer<HostInfo>(hosts);
		_appName = appName;
		protocolType = ptype;
	}

	protected abstract BrokerProtocolHandler getBrokerProtocolHandler() throws Throwable;

	public void acknowledge(NetNotification notification, AcceptRequest acceptRequest) throws Throwable
	{

		if ((notification != null) && (notification.getMessage() != null) && (StringUtils.isNotBlank(notification.getMessage().getMessageId())))
		{
			NetBrokerMessage brkMsg = notification.getMessage();

			String ackDestination = null;
			if (notification.getDestinationType() != DestinationType.TOPIC)
			{
				ackDestination = notification.getSubscription();
			}
			else
			{
				ackDestination = notification.getDestination();
			}

			NetAcknowledgeMessage ackMsg = new NetAcknowledgeMessage(ackDestination, brkMsg.getMessageId());
			if (acceptRequest != null)
			{
				ackMsg.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}

			NetAction action = new NetAction(ActionType.ACKNOWLEDGE_MESSAGE);
			action.setAcknowledgeMessage(ackMsg);
			NetMessage msg = buildMessage(action);

			getNetHandler().sendMessage(msg);

		}
		else
		{
			throw new IllegalArgumentException("Can't acknowledge invalid message.");
		}
	}

	public void acknowledge(NetNotification notification) throws Throwable
	{
		acknowledge(notification, null);
	}

	public void addAsyncConsumer(NetSubscribe subscribe, BrokerListener listener, AcceptRequest acceptRequest) throws Throwable
	{
		if ((subscribe != null) && (StringUtils.isNotBlank(subscribe.getDestination())))
		{
			synchronized (_async_listeners)
			{
				if (_async_listeners.containsKey(subscribe.getDestination()))
				{
					throw new IllegalStateException("A listener for that Destination already exists");
				}

				_async_listeners.put(subscribe.getDestination(), listener);
			}
			if (acceptRequest != null)
			{
				subscribe.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}

			NetAction netAction = new NetAction(ActionType.SUBSCRIBE);
			netAction.setSubscribeMessage(subscribe);

			NetMessage msg = buildMessage(netAction);

			getNetHandler().sendMessage(msg);

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
		addAsyncConsumer(subscribe, listener, null);
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

	public NetPong checkStatus() throws Throwable
	{
		String actionId = UUID.randomUUID().toString();
		NetPing ping = new NetPing(actionId);

		NetAction action = new NetAction(ActionType.PING);
		action.setPingMessage(ping);

		NetMessage message = buildMessage(action);

		getNetHandler().sendMessage(message);

		long timeout = System.currentTimeMillis() + (2 * 1000);
		NetPong pong = null;

		do
		{
			synchronized (_bstatus)
			{
				Sleep.time(500);
				if (System.currentTimeMillis() > timeout)
					return null;
				pong = _bstatus.peek();
				if (pong == null)
					continue;
				if (!pong.getActionId().equals(NetPong.getUniversalActionId()) && !pong.getActionId().equals(actionId))
				{
					pong = null;
				}
				_bstatus.remove();
			}
		}
		while (pong == null);

		return pong;
	}

	public void enqueueMessage(NetBrokerMessage brokerMessage, String destinationName, AcceptRequest acceptRequest)
	{

		if ((brokerMessage != null) && (StringUtils.isNotBlank(destinationName)))
		{
			NetPublish publish = new NetPublish(destinationName, pt.com.broker.types.NetAction.DestinationType.QUEUE, brokerMessage);
			if (acceptRequest != null)
			{
				publish.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}

			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage msg = buildMessage(action);

			try
			{
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
		enqueueMessage(brokerMessage, destinationName, null);
	}

	protected void feedStatusConsumer(NetPong pong) throws Throwable
	{
		_bstatus.offer(pong);
	}

	public HostInfo getHostInfo()
	{
		return hosts.get();
	}

	public void addHostInfo(HostInfo hostInfo)
	{
		hosts.add(hostInfo);
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

	public NetNotification poll(String queueName, AcceptRequest acceptRequest) throws Throwable
	{
		if (StringUtils.isNotBlank(queueName))
		{
			NetPoll poll = new NetPoll(queueName);
			if (acceptRequest != null)
			{
				poll.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}
			NetAction action = new NetAction(ActionType.POLL);
			action.setPollMessage(poll);

			NetMessage message = buildMessage(action);
			SyncConsumer sc = SyncConsumerList.get(queueName);
			sc.increment();

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
		return poll(queueName, null);
	}

	public void publishMessage(NetBrokerMessage brokerMessage, String destination, AcceptRequest acceptRequest)
	{
		if ((brokerMessage != null) && (StringUtils.isNotBlank(destination)))
		{
			NetPublish publish = new NetPublish(destination, pt.com.broker.types.NetAction.DestinationType.TOPIC, brokerMessage);
			if (acceptRequest != null)
			{
				publish.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}
			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage msg = buildMessage(action);

			try
			{
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
		publishMessage(brokerMessage, destination, null);
	}

	public void unsubscribe(NetAction.DestinationType destinationType, String destinationName, AcceptRequest acceptRequest) throws Throwable
	{
		if ((StringUtils.isNotBlank(destinationName)) && (destinationType != null))
		{
			NetUnsubscribe unsubs = new NetUnsubscribe(destinationName, destinationType);
			if (acceptRequest != null)
			{
				unsubs.setActionId(acceptRequest.getActionId());
				PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
			}
			NetAction action = new NetAction(ActionType.UNSUBSCRIBE);
			action.setUnsbuscribeMessage(unsubs);

			NetMessage message = buildMessage(action);

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
		unsubscribe(destinationType, destinationName, null);
	}

	public void close()
	{
		getNetHandler().stop();
		state = BrokerClientState.CLOSE;
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

	public BrokerClientState getState()
	{
		synchronized (this)
		{
			return state;
		}
	}

	public void setState(BrokerClientState state)
	{
		synchronized (this)
		{
			this.state = state;
		}
	}

	public void setPortocolType(NetProtocolType portocolType)
	{
		this.protocolType = portocolType;
	}

	public NetProtocolType getPortocolType()
	{
		return protocolType;
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
