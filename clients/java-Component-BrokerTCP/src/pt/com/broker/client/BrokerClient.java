package pt.com.broker.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.messaging.BrokerListener;
import pt.com.types.NetAcknowledgeMessage;
import pt.com.types.NetAction;
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

	private final Object mutex = new Object();

	public BrokerClient(String host, int portNumber) throws Throwable
	{
		this(host, portNumber, "brokerClient");
	}

	public BrokerClient(String host, int portNumber, String appName) throws Throwable
	{
		this(host, portNumber, appName, NetProtocolType.PROTOCOL_BUFFER);
	}

	public BrokerClient(String host, int portNumber, String appName, NetProtocolType ptype) throws Throwable
	{
		_host = host;
		_portNumber = portNumber;
		_appName = appName;
		_netHandler = new BrokerProtocolHandler(this, ptype);
		_netHandler.start();
	}

	public void acknowledge(NetNotification notification) throws Throwable
	{

		if ((notification != null) && (notification.getMessage() != null) && (StringUtils.isNotBlank(notification.getMessage().getMessageId())))
		{
			NetBrokerMessage brkMsg = notification.getMessage();
			NetAcknowledgeMessage ackMsg = new NetAcknowledgeMessage(notification.getDestination(), brkMsg.getMessageId());

			NetAction action = new NetAction(ActionType.ACKNOWLEDGE_MESSAGE);
			action.setAcknowledgeMessage(ackMsg);
			NetMessage msg = buildMessage("http://services.sapo.pt/broker/acknowledge", action);

			_netHandler.sendMessage(msg);
		}
		else
		{
			throw new IllegalArgumentException("Can't acknowledge invalid message.");
		}
	}

	public void addAsyncConsumer(NetSubscribe subscribe, BrokerListener listener) throws Throwable
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

			String action = buildAction(subscribe.getDestinationType());

			NetAction netAction = new NetAction(ActionType.SUBSCRIBE);
			netAction.setSubscribeMessage(subscribe);

			NetMessage msg = buildMessage(action, netAction);

			_netHandler.sendMessage(msg);

			_consumerList.add(new BrokerAsyncConsumer(subscribe, listener));
			log.info("Created new async consumer for '{}'", subscribe.getDestination());
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Notification request");
		}
	}

	protected void sendSubscriptions() throws Throwable
	{
		for (BrokerAsyncConsumer aconsumer : _consumerList)
		{
			NetSubscribe subscription = aconsumer.getSubscription();
			String action = buildAction(subscription.getDestinationType());

			NetAction netAction = new NetAction(ActionType.SUBSCRIBE);
			netAction.setSubscribeMessage(subscription);

			NetMessage msg = buildMessage(action, netAction);

			_netHandler.sendMessage(msg);
			log.info("Reconnected async consumer for '{}'", subscription.getDestination());
		}
	}

	public NetMessage buildMessage(String actionDest, NetAction action)
	{
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("ACTION", actionDest);
		headers.put("ADDRESS", _appName);
		headers.put("TO", actionDest);

		NetMessage message = new NetMessage(action, headers);

		return message;
	}

	private String buildAction(DestinationType destinationType)
	{
		String raction = "";

		switch (destinationType)
		{
		case QUEUE:
			raction = "http://services.sapo.pt/broker/listen";
			break;
		case TOPIC:
			raction = "http://services.sapo.pt/broker/subscribe";
			break;
		case VIRTUAL_QUEUE:
			raction = "http://services.sapo.pt/broker/listen";
			break;
		}
		return raction;
	}

	public NetPong checkStatus() throws Throwable
	{
		NetPing ping = new NetPing(System.currentTimeMillis());

		NetAction action = new NetAction(ActionType.PING);
		action.setPingMessage(ping);

		NetMessage message = buildMessage("http://services.sapo.pt/broker/checkstatus", action);

		_netHandler.sendMessage(message);
		return _bstatus.take();

	}

	public void close()
	{
		_netHandler.stop();
	}

	public void enqueueMessage(NetBrokerMessage brokerMessage, String destinationName)
	{

		if ((brokerMessage != null) && (StringUtils.isNotBlank(destinationName)))
		{
			NetPublish publish = new NetPublish(destinationName, pt.com.types.NetAction.DestinationType.QUEUE, brokerMessage);

			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage msg = buildMessage("http://services.sapo.pt/broker/enqueue", action);

			try
			{
				_netHandler.sendMessage(msg);
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

	public NetNotification poll(String queueName) throws Throwable
	{
		if (StringUtils.isNotBlank(queueName))
		{
			NetPoll poll = new NetPoll(queueName);
			poll.setActionId("http://services.sapo.pt/broker/poll");
			NetAction action = new NetAction(ActionType.POLL);
			action.setPollMessage(poll);

			NetMessage message = buildMessage("http://services.sapo.pt/broker/poll", action);
			SyncConsumer sc = SyncConsumerList.get(queueName);
			sc.increment();

			_netHandler.sendMessage(message);

			NetNotification m = sc.take();
			return m;
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Poll request");
		}
	}

	public void publishMessage(NetBrokerMessage brokerMessage, String destination)
	{
		if ((brokerMessage != null) && (StringUtils.isNotBlank(destination)))
		{
			NetPublish publish = new NetPublish(destination, pt.com.types.NetAction.DestinationType.TOPIC, brokerMessage);
			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage msg = buildMessage("http://services.sapo.pt/broker/publish", action);

			try
			{
				_netHandler.sendMessage(msg);
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

	public void unsubscribe(NetAction.DestinationType destinationType, String destinationName) throws Throwable
	{
		if ((StringUtils.isNotBlank(destinationName)) && (destinationType != null))
		{
			NetUnsubscribe unsubs = new NetUnsubscribe(destinationName, destinationType);
			NetAction action = new NetAction(ActionType.UNSUBSCRIBE);
			action.setUnsbuscribeMessage(unsubs);

			NetMessage message = buildMessage("http://services.sapo.pt/broker/unsubscribe", action);

			_netHandler.sendMessage(message);

			for (BrokerAsyncConsumer bac : _consumerList)
			{
				NetSubscribe n = bac.getSubscription();

				if ((n.getDestination().equals(destinationName)) && (n.getDestinationType() == destinationType))
					_consumerList.remove(bac);
			}
		}
		else
		{
			throw new IllegalArgumentException("Mal-formed Unsubscribe request");
		}
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
