package pt.com.broker.monitorization.http;

import java.util.Map;

public class ActionExecuterAction extends JsonHttpAction
{
	private static final String ACTION_PREFIX = "/action/";

	@Override
	protected String getPrefix()
	{
		return ACTION_PREFIX;
	}
	
	@Override
	protected String getData(String path, Map<String, String> params)
	{
		return ActionExecutor.execute(path, params);
	}
}
