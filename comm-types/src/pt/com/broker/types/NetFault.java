package pt.com.broker.types;

import java.util.Map;

import pt.com.broker.types.NetAction.ActionType;

/**
 * Represents a Fault message.
 * 
 */

public final class NetFault
{
	public static final NetMessage InvalidMessageSizeErrorMessage;
	public static final NetMessage UnknownEncodingProtocolMessage; // Not sent
	public static final NetMessage UnknownEncodingVersionMessage; // Not sent
	public static final NetMessage InvalidMessageFormatErrorMessage;
	public static final NetMessage UnexpectedMessageTypeErrorMessage;
	public static final NetMessage InvalidDestinationNameErrorMessage;
	public static final NetMessage InvalidMessageDestinationTypeErrorMessage;
	public static final NetMessage MaximumNrQueuesReachedErrorMessage;
	public static final NetMessage MaximumDistinctSubscriptionsReachedErrorMessage;
	public static final NetMessage PollTimeoutErrorMessage;
	public static final NetMessage NoMessageInQueueErrorMessage;
	public static final NetMessage NoVirtualQueueSupportErrorMessage;
	public static final NetMessage AuthenticationFailedErrorMessage;
	public static final NetMessage UnknownAuthenticationTypeMessage;
	public static final NetMessage AccessDeniedErrorMessage;
	public static final NetMessage InvalidAuthenticationChannelType;

	static
	{
		InvalidMessageSizeErrorMessage = buildNetFaultMessage("1101", "Invalid message size");
		UnknownEncodingProtocolMessage = buildNetFaultMessage("1102", "Unknown encoding protocol");
		UnknownEncodingVersionMessage = buildNetFaultMessage("1103", "Unknown encoding version");
		InvalidMessageFormatErrorMessage = buildNetFaultMessage("1201", "Invalid message format");
		UnexpectedMessageTypeErrorMessage = buildNetFaultMessage("1202", "Unexpected message type");
		InvalidDestinationNameErrorMessage = buildNetFaultMessage("2001", "Invalid destination name");
		InvalidMessageDestinationTypeErrorMessage = buildNetFaultMessage("2002", "Invalid destination type");
		MaximumNrQueuesReachedErrorMessage = buildNetFaultMessage("2003", "Maximum number of queues reached");
		MaximumDistinctSubscriptionsReachedErrorMessage = buildNetFaultMessage("2004", "Maximum distinct subscriptions reached");
		PollTimeoutErrorMessage = buildNetFaultMessage("2005", "Message poll timeout");
		NoMessageInQueueErrorMessage = buildNetFaultMessage("2006", "No message in local agent queue.");
		NoVirtualQueueSupportErrorMessage = buildNetFaultMessage("2007", "Virtual queues are not supported.");
		AuthenticationFailedErrorMessage = buildNetFaultMessage("3101", "Authentication failed");
		UnknownAuthenticationTypeMessage = buildNetFaultMessage("3102", "Unknown authentication type");
		InvalidAuthenticationChannelType = buildNetFaultMessage("3103", "Invalid authentication channel type");
		AccessDeniedErrorMessage = buildNetFaultMessage("3201", "Access denied");
	}

	private String actionId;
	private String code;
	private String message;
	private String detail;

	private Map<String, String> headers;

	public NetFault(String code, String message)
	{
		this.code = code;
		this.message = message;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public String getCode()
	{
		return code;
	}

	public String getMessage()
	{
		return message;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	public String getDetail()
	{
		return detail;
	}

	public static NetMessage buildNetFaultMessage(String code, String message)
	{
		NetFault fault = new NetFault(code, message);
		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(fault);
		NetMessage msg = new NetMessage(action);
		return msg;
	}

	public static NetMessage getMessageFaultWithActionId(NetMessage message, String actionId)
	{
		NetFault fault = message.getAction().getFaultMessage();
		NetFault newFault = new NetFault(fault.getCode(), fault.getMessage());
		newFault.setActionId(actionId);
		newFault.setDetail(fault.getDetail());

		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(newFault);

		return new NetMessage(action, message.getHeaders());
	}

	public static NetMessage getMessageFaultWithDetail(NetMessage message, String detail)
	{
		NetFault fault = message.getAction().getFaultMessage();
		NetFault newFault = new NetFault(fault.getCode(), fault.getMessage());
		newFault.setActionId(fault.getActionId());
		newFault.setDetail(detail);

		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(newFault);

		return new NetMessage(action, message.getHeaders());
	}

	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}
}