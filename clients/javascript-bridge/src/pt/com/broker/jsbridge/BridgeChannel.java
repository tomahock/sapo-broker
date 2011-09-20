package pt.com.broker.jsbridge;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.jsbridge.protocol.JsonMessage;
import pt.com.broker.jsbridge.protocol.JsonMessage.MessageType;
import pt.com.broker.jsbridge.protocol.JsonSerializer;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class BridgeChannel implements BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(BridgeChannel.class);

	private static final Charset utf8 = Charset.forName("UTF-8");
	private final String name;
	private final String topic;
	private final boolean allowSubscription;
	private final boolean allowPublication;
	private final String brokerHost;
	private final int brokerPort;
	private final MessageTransformer downstreamFilter;
	private final MessageTransformer upstreamFilter;
	private final BrokerClient brokerClient;
	private final Set<Channel> subscribers = Collections.newSetFromMap(new ConcurrentHashMap<Channel, Boolean>());

	private Object sizeLock = new Object();

	public BridgeChannel(String name, String topic, boolean allowSubscription, boolean allowPublication, String brokerHost, int brokerPort, MessageTransformer downstreamFilter, MessageTransformer upstreamFilter)
	{
		super();
		this.name = name;
		this.topic = topic;
		this.allowSubscription = allowSubscription;
		this.allowPublication = allowPublication;
		this.brokerHost = brokerHost;
		this.brokerPort = brokerPort;
		this.downstreamFilter = downstreamFilter;
		this.upstreamFilter = upstreamFilter;

		try
		{
			this.brokerClient = new BrokerClient(brokerHost, brokerPort);
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}

	}

	public void publish(String payload) throws Throwable
	{
		if (allowPublication)
		{
			NetBrokerMessage msg = new NetBrokerMessage(payload);

			if (upstreamFilter != null)
			{
				brokerClient.publishMessage(upstreamFilter.transform(msg), topic);
			}
			else
			{
				brokerClient.publishMessage(msg, topic);
			}
		}
	}

	public void subscribe(Channel channel) throws Throwable
	{
		if (allowSubscription)
		{
			NetSubscribe subscribe = new NetSubscribe(topic, DestinationType.TOPIC);
			synchronized (sizeLock)
			{
				subscribers.add(channel);
				if (subscribers.size() == 1)
				{
					brokerClient.addAsyncConsumer(subscribe, this);
				}
			}
		}
	}

	public boolean unsubscribe(Channel channel) throws Throwable
	{
		if (channel != null)
		{
			synchronized (sizeLock)
			{
				boolean result = subscribers.remove(channel);
				if (subscribers.size() == 0)
				{
					brokerClient.unsubscribe(DestinationType.TOPIC, topic);
				}
				return result;
			}
		}
		else
		{
			return false;
		}
	}

	public String getName()
	{
		return name;
	}

	public String getTopic()
	{
		return topic;
	}

	public boolean allowSubscription()
	{
		return allowSubscription;
	}

	public boolean allowPublication()
	{
		return allowPublication;
	}

	public String getBrokerHost()
	{
		return brokerHost;
	}

	public int getBrokerPort()
	{
		return brokerPort;
	}

	public MessageTransformer getDownstreamFilter()
	{
		return downstreamFilter;
	}

	public MessageTransformer getUpstreamFilter()
	{
		return upstreamFilter;
	}

	@Override
	public boolean isAutoAck()
	{
		return false;
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		if (allowSubscription)
		{
			byte[] payload = new byte[0];

			if (downstreamFilter != null)
			{
				payload = downstreamFilter.transform(notification.getMessage()).getPayload();
			}
			else
			{
				payload = notification.getMessage().getPayload();
			}

			JsonMessage pubMessage = new JsonMessage(MessageType.NOTIFICATION, name);
			pubMessage.setPayload(new String(payload));
			String jsonPubMessage = "";

			try
			{
				jsonPubMessage = JsonSerializer.toJson(pubMessage);
			}
			catch (Throwable t)
			{
				log.error("Failed to serialize message for publishing", t);
				return;
			}

			WebSocketFrame wsf = new DefaultWebSocketFrame(jsonPubMessage);
			int mark = wsf.getBinaryData().readerIndex();

			for (Channel channel : subscribers)
			{
				channel.write(wsf);
				wsf.getBinaryData().readerIndex(mark);
			}
		}
	}
}
