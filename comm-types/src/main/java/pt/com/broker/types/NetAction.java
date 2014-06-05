package pt.com.broker.types;

import java.util.Objects;

/**
 * Represents an Action.
 * 
 */

public final class NetAction
{

	public enum ActionType
	{
		PUBLISH, POLL, ACCEPTED, ACKNOWLEDGE, SUBSCRIBE, UNSUBSCRIBE, NOTIFICATION, FAULT, PING, PONG, AUTH
	};

	public enum DestinationType
	{
		TOPIC, QUEUE, VIRTUAL_QUEUE
	};

	protected ActionType actionType;

	private NetPublish publishMessage;
	private NetPoll pollMessage;
	private NetAccepted acceptedMessage;
	private NetAcknowledge acknowledgeMessage;
	private NetSubscribe subscribeMessage;
	private NetUnsubscribe unsbuscribeMessage;
	private NetNotification notificationMessage;
	private NetFault faultMessage;
	private NetPing pingMessage;
	private NetPong pongMessage;
	private NetAuthentication authenticationMessage;


    private Object netActionMessage;



	public NetAction(ActionType actionType)
	{
		this.actionType = actionType;
	}



    public NetAction(NetUnsubscribe netUnsubscribe){

        this(ActionType.UNSUBSCRIBE);

        setUnsbuscribeMessage(netUnsubscribe);
    }


    public NetAction(NetNotification notification)
    {
        this(ActionType.NOTIFICATION);

        setNotificationMessage(notification);
    }

   public NetAction(NetPing ping)
    {
        this(ActionType.PING);

        setPingMessage(ping);
    }

    public NetAction(NetPong pong)
    {
        this(ActionType.PONG);

        setPongMessage(pong);
    }
    public NetAction(NetSubscribe netSubscribe)
    {
        this(ActionType.SUBSCRIBE);

        setSubscribeMessage(netSubscribe);
    }


    public NetAction(NetPublish netPublish)
    {
        this(ActionType.PUBLISH);

        setPublishMessage(netPublish);
    }

    public NetAction(NetAcknowledge netAcknowledge){
        this(ActionType.ACKNOWLEDGE);

        setAcknowledgeMessage(netAcknowledge);
    }


    public NetAction(NetPoll netPoll){
        this(ActionType.POLL);

        setPollMessage(netPoll);
    }

    public NetAction(NetAuthentication netAuthentication){
        this(ActionType.AUTH);

        setAuthenticationMessage(netAuthentication);
    }



	public ActionType getActionType()
	{
		return actionType;
	}

	public void setPublishMessage(NetPublish publishMessage)
	{
		this.publishMessage = publishMessage;
        setNetActionMessage(publishMessage);
	}

	public NetPublish getPublishMessage()
	{
		return publishMessage;
	}

	public void setPollMessage(NetPoll pollMessage)
	{
		this.pollMessage = pollMessage;
        setNetActionMessage(pollMessage);
	}

	public NetPoll getPollMessage()
	{
		return pollMessage;
	}

	public void setAcceptedMessage(NetAccepted acceptedMessage)
	{
		this.acceptedMessage = acceptedMessage;
        setNetActionMessage(acceptedMessage);
	}

	public NetAccepted getAcceptedMessage()
	{
		return acceptedMessage;
	}

	public void setAcknowledgeMessage(NetAcknowledge acknowledgeMessage)
	{
		this.acknowledgeMessage = acknowledgeMessage;
        setNetActionMessage(acknowledgeMessage);
	}

	public NetAcknowledge getAcknowledgeMessage()
	{
		return acknowledgeMessage;
	}

	public void setSubscribeMessage(NetSubscribe subscribeMessage)
	{
		this.subscribeMessage = subscribeMessage;
        setNetActionMessage(subscribeMessage);
	}

	public NetSubscribe getSubscribeMessage()
	{
		return subscribeMessage;
	}

	public void setUnsbuscribeMessage(NetUnsubscribe unsbuscribeMessage)
	{
		this.unsbuscribeMessage = unsbuscribeMessage;
        setNetActionMessage(unsbuscribeMessage);
	}

	public NetUnsubscribe getUnsbuscribeMessage()
	{
		return unsbuscribeMessage;
	}

	public void setNotificationMessage(NetNotification notificationMessage)
	{
		this.notificationMessage = notificationMessage;
	}

	public NetNotification getNotificationMessage()
	{
		return notificationMessage;
	}

	public void setFaultMessage(NetFault faultMessage)
	{
		this.faultMessage = faultMessage;
        setNetActionMessage(faultMessage);
	}

	public NetFault getFaultMessage()
	{
		return faultMessage;
	}

	public void setPingMessage(NetPing pingMessage)
	{
		this.pingMessage = pingMessage;
        setNetActionMessage(pingMessage);
	}

	public NetPing getPingMessage()
	{
		return pingMessage;
	}

	public void setPongMessage(NetPong pongMessage)
	{
		this.pongMessage = pongMessage;
        setNetActionMessage(pongMessage);
	}

	public NetPong getPongMessage()
	{
		return pongMessage;
	}

	public void setAuthenticationMessage(NetAuthentication authenticationMessage)
	{
		this.authenticationMessage = authenticationMessage;
        setNetActionMessage(authenticationMessage);
	}

	public NetAuthentication getAuthenticationMessage()
	{
		return authenticationMessage;
	}


    public Object getNetActionMessage(){

        return netActionMessage;
    }

    private void setNetActionMessage(Object netActionMessage){
        this.netActionMessage = netActionMessage;
    }
}