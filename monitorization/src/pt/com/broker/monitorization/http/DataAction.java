package pt.com.broker.monitorization.http;

import java.util.Map;

public class DataAction extends JsonHttpAction
{
	private static final String DATA_PREFIX = "/data/";

	@Override
	protected String getPrefix()
	{
		return DATA_PREFIX;
	}
	
	@Override
	protected String getData(String path, Map<String, String> params)
	{
		return DataFetcher.getData(path, params);
	}

}
