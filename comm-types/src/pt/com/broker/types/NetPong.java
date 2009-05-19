package pt.com.broker.types;

public class NetPong
{
	private final static String universalActionId = "5E4FF374-B9AC-459b-B078-89A587D21001";

	private String actionId;

	public NetPong(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public static String getUniversalActionId()
	{
		return universalActionId;
	}
}