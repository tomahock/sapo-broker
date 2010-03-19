package pt.com.broker.messaging;

import java.util.HashMap;
import java.util.Map;

import org.caudexorigo.text.StringUtils;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.ListenerChannel;
import pt.com.gcs.messaging.MessageListener;

/**
 * BrokerListener is a base class for types representing local message consumers.
 */
public abstract class BrokerListener implements MessageListener
{
	private final ListenerChannel lchannel;
	private final String subscriptionKey;
	private final MessageListener.Type type;

	public BrokerListener(ListenerChannel lchannel, String subscriptionKey)
	{
		super();

		if (lchannel == null)
		{
			throw new NullPointerException("Can not use a null ListenerChannel as argument");
		}

		if (StringUtils.isBlank(subscriptionKey))
		{
			throw new IllegalArgumentException("Can not use a blank subscriptiok key as argument");
		}

		this.lchannel = lchannel;
		this.subscriptionKey = subscriptionKey;
		type = MessageListener.Type.LOCAL;
	}

	@Override
	public String getsubscriptionKey()
	{
		return subscriptionKey;
	}

	@Override
	public ListenerChannel getChannel()
	{
		return lchannel;
	}

	protected static NetMessage buildNotification(InternalMessage msg, DestinationType dtype)
	{
		return buildNotification(msg, null, dtype);
	}

	protected static NetMessage buildNotification(InternalMessage msg, String subscriptionName, DestinationType dtype)
	{
		NetNotification notification = new NetNotification(msg.getDestination(), dtype, msg.getContent(), subscriptionName);

		NetAction action = new NetAction(NetAction.ActionType.NOTIFICATION);
		action.setNotificationMessage(notification);

		notification.getMessage().setMessageId(msg.getMessageId());

		Map<String, String> params = new HashMap<String, String>();
		params.put("FROM", msg.getSourceApp());
		params.put("ACTION", "http://services.sapo.pt/broker/notification/" + msg.getMessageId());
		params.put("PUBLISHING_AGENT", msg.getPublishingAgent());

		NetMessage message = new NetMessage(action, params);

		return message;
	}

	@Override
	public Type getType()
	{
		return type;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lchannel == null) ? 0 : lchannel.hashCode());
		result = prime * result + ((subscriptionKey == null) ? 0 : subscriptionKey.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BrokerListener other = (BrokerListener) obj;
		if (lchannel == null)
		{
			if (other.lchannel != null)
				return false;
		}
		else if (!lchannel.equals(other.lchannel))
			return false;
		if (subscriptionKey == null)
		{
			if (other.subscriptionKey != null)
				return false;
		}
		else if (!subscriptionKey.equals(other.subscriptionKey))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "BrokerListener [type=" + getType().toString() + ", lchannel=" + lchannel + ", subscriptionKey=" + subscriptionKey + "]";
	}

}
