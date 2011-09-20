package pt.com.broker.monitorization.db.queries;

import java.util.List;
import java.util.Map;

public interface QueryDataProvider
{
	public String getType();

	public String getData(String queryType, Map<String, List<String>> params);
}
