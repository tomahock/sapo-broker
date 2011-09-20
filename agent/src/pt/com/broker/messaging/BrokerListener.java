package pt.com.broker.messaging;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.DeliverableMessage;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.MessageListenerBase;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.channels.ListenerChannel;

/**
 * BrokerListener is a base class for types representing local message consumers.
 */
public abstract class BrokerListener extends MessageListenerBase
{
	private static final Logger log = LoggerFactory.getLogger(BrokerListener.class);

	protected static final ForwardResult failed = new ForwardResult(Result.FAILED);

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

	protected abstract ForwardResult doOnMessage(NetMessage response);

	public ForwardResult onMessage(DeliverableMessage msg)
	{
		if (msg == null)
			return failed;

		NetMessage response;
		if (msg instanceof NetMessage)
		{
			response = (NetMessage) msg;
			return doOnMessage(response);
		}
		else
		{
			log.warn("Don't know how to handle this message type");
			return failed;
		}
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
