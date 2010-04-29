package pt.com.broker.monitorization.db.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class QueryGenerator
{
	protected static final String AGENT_PARAMETER = "agent";
	protected static final String AGENT_ATTRIBUTE = "agent_name";
	protected static final String SUBJECT_PARAMETER = "subject";
	protected static final String SUBJECT_ATTRIBUTE = "subject";
	protected static final String PREDICATE_PARAMETER = "predicate";
	protected static final String PREDICATE_ATTRIBUTE = "predicate";
	// time
	protected static final String FROM_PARAMETER = "from";
	protected static final String TO_PARAMETER = "to";
	protected static final String LAST_ENTRY_PARAMETER_VALUE = "last";
	protected static final String TIME_PARAMETER = "date";
	protected static final String TIME_ATTRIBUTE = "event_time";
	
	// value
	protected static final String VALUE_ATTRIBUTE = "object_value";

	// results
	protected static final String RESULTS_COUNT_PARAMETER = "count";

	// order by
	protected static final String ORDER_BY = "order";

	protected final Map<String, List<String>> queryParameters;

	protected String sqlQuery;

	protected QueryGenerator(Map<String, List<String>> queryParameters)
	{
		this.queryParameters = queryParameters;

		generateSqlQuery();
	}

	public String getSqlQuery()
	{
		return sqlQuery;
	}

	protected abstract void generateSqlQuery();

	
	protected static void processEntry(Entry<String, List<String>> entry, Map<String, List<String>> queryParams)
	{
		String key = entry.getKey();
		if (entry.getValue().size() != 1)
		{
			throw new IllegalArgumentException(String.format("Query parameter '%s' should only have one value", key));
		}

		String parameterValue = entry.getValue().get(0);
		if (!validateParameterName(key))
		{
			throw new IllegalArgumentException(String.format("Invalid parameter: '%s'", key));
		}
		if (!validateParameterValue(entry.getValue().get(0)))
		{
			throw new IllegalArgumentException(String.format("Invalid parameter value: '%s'", key));
		}

		List<String> list = queryParams.get(key);
		if (list == null)
		{
			list = new ArrayList<String>();
			queryParams.put(key, list);
		}

		list.add(parameterValue);
	}

	protected static boolean validateParameterName(String paramenterName)
	{
		return true;
	}

	protected static boolean validateParameterValue(String parameterValue)
	{
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((queryParameters == null) ? 0 : queryParameters.hashCode());
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
		QueryGenerator other = (QueryGenerator) obj;
		if (queryParameters == null)
		{
			if (other.queryParameters != null)
				return false;
		}

		if (this.queryParameters.size() != other.queryParameters.size())
			return false;

		for (Map.Entry<String, List<String>> entry : this.queryParameters.entrySet())
		{
			List<String> thislist = entry.getValue();
			List<String> otherlist = other.queryParameters.get(entry.getKey());

			if ((thislist == null) && (otherlist == null))
				continue;

			if ((thislist != null) && (otherlist == null))
				return false;
			if ((otherlist != null) && (thislist == null))
				return false;

			if (thislist.size() != otherlist.size())
			{
				return false;
			}

			for (int i = 0; i != thislist.size(); ++i)
			{
				if (thislist.get(i) != otherlist.get(i))
					return false;
			}
		}

		return true;
	}
}
