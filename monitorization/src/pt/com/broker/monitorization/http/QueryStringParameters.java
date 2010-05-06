package pt.com.broker.monitorization.http;

import java.util.List;
import java.util.Map;

public class QueryStringParameters
{
	
	public final static String WINDOW_PARAM = "window";
	public final static String WINDOW_PARAM_LAST = "last";
	public final static String WINDOW_PARAM_ALL = "all";
	
	private static String getParameter(String paramName, Map<String, List<String>> params)
	{
		List<String> list = params.get(paramName);
		if( (list != null) && (list.size() == 1) )
		{
			return list.get(0);
		}
		return null;
	}
	
	public static String getWindowParam(Map<String, List<String>> params)
	{
		return getParameter(WINDOW_PARAM, params);
	}
}
