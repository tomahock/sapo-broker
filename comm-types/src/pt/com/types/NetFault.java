package pt.com.types;

public final class NetFault
{
	private String actionId;
	private String code;
	private String message;
	private String detail;

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
}