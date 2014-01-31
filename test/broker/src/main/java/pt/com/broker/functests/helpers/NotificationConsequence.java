package pt.com.broker.functests.helpers;

import java.util.Arrays;

import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;

public class NotificationConsequence extends Consequence
{
	private GenericBrokerListener brokerListener;

	// Comparation fields
	private String destination;
	private String subscription;
	private NetAction.DestinationType destinationType;
	private byte[] messagePayload;

	public NotificationConsequence(String name, String actorName, GenericBrokerListener brokerListener)
	{
		super(name, actorName);
		this.brokerListener = brokerListener;
	}

	public Step run() throws Exception
	{
		NetNotification netNotification = brokerListener.getFuture().get();

		if (destinationType != null)
		{
			if (!netNotification.getDestinationType().toString().equals(destinationType.toString()))
			{
				String reason = String.format("Destination type mismatch! Expected: %s Received: %s", destinationType, netNotification.getDestinationType());
				setReasonForFailure(reason);

				return this;
			}
		}

		if (destination != null)
		{
			if (!netNotification.getDestination().equals(destination))
			{
				String reason = String.format("Destination mismatch! Expected: %s Received: %s", destination, netNotification.getDestination());
				setReasonForFailure(reason);

				return this;
			}
		}

		if (subscription != null)
		{
			if (!netNotification.getSubscription().equals(subscription))
			{
				String reason = String.format("Subscription mismatch! Expected: %s Received: %s", subscription, netNotification.getSubscription());
				setReasonForFailure(reason);
				return this;
			}
		}

		if (messagePayload != null)
		{
			NetBrokerMessage message = netNotification.getMessage();
			if (!Arrays.equals(message.getPayload(), messagePayload))
			{
				setReasonForFailure("Binary content mismatch!");

				return this;
			}
		}

		setDone(true);
		setSucess(true);

		return this;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setSubscription(String subscription)
	{
		this.subscription = subscription;
	}

	public String getSubscription()
	{
		return subscription;
	}

	public void setDestinationType(NetAction.DestinationType destinationType)
	{
		this.destinationType = destinationType;
	}

	public NetAction.DestinationType getDestinationType()
	{
		return destinationType;
	}

	public void setMessagePayload(byte[] messagePayload)
	{
		this.messagePayload = messagePayload;
	}

	public byte[] getMessagePayload()
	{
		return messagePayload;
	}

}
