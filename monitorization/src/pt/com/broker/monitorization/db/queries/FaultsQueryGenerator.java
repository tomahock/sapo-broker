package pt.com.broker.monitorization.db.queries;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaultsQueryGenerator extends QueryGenerator
{
	
	protected static final String ID_PARAMETER = "id";
	protected static final String ID_ATTRIBUTE = "id";
	protected static final String AGENT_PARAMETER = "agent";
	
	protected static final String GROUP_BY_PARAMETER = "groupby";
	protected static final String SHORT_MSG_PARAMETER = "shortmessage";
	

	// time
	private FaultsQueryGenerator(Map<String, List<String>> queryParameters)
	{
		super(queryParameters);
	}

	protected void generateSqlQuery()
	{
		StringBuilder sb = new StringBuilder();
		
		
		// test for particular case GROUP BY.
		List<String> group = queryParameters.get(GROUP_BY_PARAMETER);
		if(group!= null)
		{
			if(group.get(0).equals(SHORT_MSG_PARAMETER))
			{
				sb.append("select short_message, count(id) from fault_data where event_time > (now()- time '00:05')");
				List<String> agent = queryParameters.get(AGENT_PARAMETER);
				if(agent != null)
				{
					sb.append(" and agent_name = '");
					sb.append(agent.get(0));
					sb.append("' ");
				}
				sb.append(" group by short_message limit 10");
				
				sqlQuery = sb.toString();
			}
			return;
		}

		sb.append("select distinct id, agent_name, event_time, message, short_message from fault_data ");

		if (queryParameters.size() > 0)
		{
			List<String> agent = queryParameters.get(AGENT_PARAMETER);
			List<String> id = queryParameters.get(ID_PARAMETER);
			
			if(agent != null)
			{
				sb.append("where ");
				
				sb.append(AGENT_ATTRIBUTE);
				sb.append(String.format(" = '%s' ", agent.get(0)));
			}
			else if (id != null)
			{
				sb.append("where ");
				
				sb.append(ID_ATTRIBUTE);
				sb.append(String.format(" = '%s' ", id.get(0)));
			}
			
			sb.append( " order by event_time DESC");
			
		}
		
		List<String> list = queryParameters.get(RESULTS_COUNT_PARAMETER);
		if (list != null)
		{
			boolean invalid = false;
			String strCount = null;
			try
			{
				if (list.size() == 1)
				{
					strCount = list.get(0);
					Integer.parseInt(strCount); // for validation only
				}
				else
				{
					invalid = true;
				}
			}
			catch (NumberFormatException nfe)
			{
				invalid = true;
			}

			if (invalid)
			{
				throw new InvalidParameterException(String.format("'%s' must be a positive integer.", RESULTS_COUNT_PARAMETER) );
			}
			sb.append(" limit ");
			sb.append(strCount);
		}

		sqlQuery = sb.toString();
	}

	
	public static QueryGenerator getInstance(Map<String, List<String>> parameters)
	{
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();

		// (parameters) Map<String, List<String>> --> (queryParams) Map<String, List<String>> ???

		for (Map.Entry<String, List<String>> entry : parameters.entrySet())
		{
			processEntry(entry, queryParams);
		}

		return new FaultsQueryGenerator(queryParams);
	}
}
