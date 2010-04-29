package pt.com.broker.monitorization.db.queries;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LastResultQuery extends QueryGenerator
{
	protected static final String MIN_VALUE_PARAMETER = "minvalue";
	protected static final String MAX_VALUE_PARAMETER = "maxavalue";
	
	private LastResultQuery(Map<String, List<String>> queryParameters)
	{
		super(queryParameters);
	}

	protected void generateSqlQuery()
	{
		StringBuilder sb = new StringBuilder();

		boolean subjectQuery = isSubjectQuery();
		boolean agentQuery = isAgentQuery();
		boolean validPredicate = validQueryParamenter(PREDICATE_PARAMETER);

		if (subjectQuery && agentQuery)
		{
			throw new IllegalArgumentException(String.format("Can't specify simoultaneously %s and %s.", SUBJECT_PARAMETER, AGENT_PARAMETER));
		}

		if (!validPredicate)
		{
			throw new IllegalArgumentException(String.format("Invalid %s attribute.", PREDICATE_PARAMETER));
		}

		sb.append("select ");
		

		
		sb.append("distinct raw_data.agent_name, mtime, raw_data.subject, raw_data.predicate, raw_data.object_value from raw_data, ");
		sb.append("\n");
		sb.append("(select agent_name, subject,  max(event_time)  as mtime from raw_data where event_time > (now() - time '00:05') AND ");

		if (subjectQuery)
		{
			sb.append(SUBJECT_ATTRIBUTE);
			sb.append("='");
			sb.append(getParameterValue(SUBJECT_PARAMETER));
			sb.append("' and ");
		}
		else if (agentQuery)
		{
			sb.append(AGENT_ATTRIBUTE);
			sb.append(" = '");

			sb.append(getParameterValue(AGENT_PARAMETER));
			sb.append("' and ");
		}

		sb.append(PREDICATE_ATTRIBUTE);
		sb.append(" = '");
		sb.append(getParameterValue(PREDICATE_PARAMETER));
		sb.append("'");

		sb.append(" group by agent_name , subject) AS t0");
		sb.append("\n");

		sb.append(" where raw_data.agent_name= t0.agent_name and raw_data.event_time=t0.mtime and ");

		if (subjectQuery)
		{
			sb.append("raw_data.");
			sb.append(SUBJECT_ATTRIBUTE);
			sb.append("='");
			sb.append(getParameterValue(SUBJECT_PARAMETER));
			sb.append("' and ");
		}
		else if (agentQuery)
		{
			sb.append("raw_data.");
			sb.append(AGENT_ATTRIBUTE);
			sb.append(" = '");
			sb.append(getParameterValue(AGENT_PARAMETER));
			sb.append("' and ");
		}
		else
		{
			sb.append(" raw_data.subject=t0.subject and");
		}

		sb.append(" raw_data.");
		sb.append(PREDICATE_ATTRIBUTE);
		sb.append("='");
		sb.append(getParameterValue(PREDICATE_PARAMETER));
		sb.append("'");
		
		if(validQueryParamenter(MIN_VALUE_PARAMETER))
		{
			sb.append(String.format(" and %s > %s ", VALUE_ATTRIBUTE, getParameterValue(MIN_VALUE_PARAMETER))); 
		}
		if(validQueryParamenter(MAX_VALUE_PARAMETER))
		{
			sb.append(String.format(" and %s < %s ", VALUE_ATTRIBUTE, getParameterValue(MAX_VALUE_PARAMETER))); 
		}
		
		sb.append(" order by raw_data.object_value DESC");
		
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
		
		System.out.println("\n\n"+sqlQuery);
	}

	private boolean isAgentQuery()
	{
		return validQueryParamenter(AGENT_PARAMETER);
	}

	private boolean isSubjectQuery()
	{
		return validQueryParamenter(SUBJECT_PARAMETER);
	}

	private boolean validQueryParamenter(String paramName)
	{
		List<String> list = queryParameters.get(paramName);
		return (list != null) && (list.size() != 0) && (list.get(0) != null);
	}

	private String getParameterValue(String paramName)
	{
		List<String> list = queryParameters.get(paramName);
		if ((list != null) && (list.size() != 0))
		{
			return list.get(0);
		}
		return null;
	}

	public static QueryGenerator getInstance(Map<String, List<String>> parameters)
	{
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();

		// (parameters) Map<String, List<String>> --> (queryParams) Map<String, List<String>> ???

		for (Map.Entry<String, List<String>> entry : parameters.entrySet())
		{
			processEntry(entry, queryParams);
		}

		return new LastResultQuery(queryParams);
	}

}
