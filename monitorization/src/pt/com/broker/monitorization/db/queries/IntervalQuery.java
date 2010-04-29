package pt.com.broker.monitorization.db.queries;

import java.security.InvalidParameterException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caudexorigo.text.DateUtil;

public class IntervalQuery extends QueryGenerator
{
	private IntervalQuery(Map<String, List<String>> queryParameters)
	{
		super(queryParameters);
	}

	protected void generateSqlQuery()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("select ");



		sb.append("distinct agent_name, event_time, subject, predicate, object_value from raw_data ");

		if (queryParameters.size() > 0)
		{
			sb.append("where ");
			processFilters(sb);

			processOrder(sb);
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

	private void processFilters(StringBuilder sb)
	{
		boolean conditionAdded = false;
		boolean oneConditionAdded = false; // at least on condition added?

		// process Agent
		List<String> list = queryParameters.get(AGENT_PARAMETER);

		conditionAdded = generateExpression(sb, AGENT_ATTRIBUTE, list, oneConditionAdded);
		oneConditionAdded = oneConditionAdded ? true : conditionAdded;

		list = queryParameters.get(SUBJECT_PARAMETER);
		conditionAdded = generateExpression(sb, SUBJECT_ATTRIBUTE, list, oneConditionAdded);
		oneConditionAdded = oneConditionAdded ? true : conditionAdded;

		list = queryParameters.get(PREDICATE_PARAMETER);
		conditionAdded = generateExpression(sb, PREDICATE_ATTRIBUTE, list, oneConditionAdded);
		oneConditionAdded = oneConditionAdded ? true : conditionAdded;

		conditionAdded = generateTimeExpression(sb, queryParameters, oneConditionAdded);
		oneConditionAdded = oneConditionAdded ? true : conditionAdded;

	}

	private boolean generateExpression(StringBuilder sb, String attribute, List<String> list, boolean previousConditionAdded)
	{
		boolean conditionAdded = false;
		if (list != null)
		{
			if (previousConditionAdded)
			{
				sb.append(" and ");
			}

			sb.append(" ( ");

			conditionAdded = true;

			String param = list.get(0);
			String[] parts = param.split(";");

			sb.append(attribute);
			sb.append(" = '");
			sb.append(parts[0]);
			sb.append("'");
			for (int i = 1; i < parts.length; ++i)
			{
				sb.append(" or ");
				sb.append(attribute);
				sb.append(" = '");
				sb.append(parts[i]);
				sb.append("'");
			}

			sb.append(" ) ");
		}
		return conditionAdded;
	}

	private boolean generateTimeExpression(StringBuilder sb, Map<String, List<String>> queryParameters, boolean previousConditionAdded)
	{
		boolean conditionAdded = false;

		List<String> list = queryParameters.get(FROM_PARAMETER);
		if (list != null)
		{
			if (previousConditionAdded)
			{
				sb.append(" and ");
			}
			sb.append(" ( ");
			conditionAdded = true;
			String param = list.get(0);

			Date dateFrom = DateUtil.parseISODate(param);

			sb.append(TIME_ATTRIBUTE);
			sb.append(" > '");
			sb.append(new Timestamp(dateFrom.getTime()));

			sb.append("' ) ");
		}

		list = queryParameters.get(TO_PARAMETER);
		if (list != null)
		{
			if (previousConditionAdded)
			{
				sb.append(" and ");
			}
			sb.append(" ( ");
			conditionAdded = true;
			String param = list.get(0);

			Date dateTo = DateUtil.parseISODate(param);

			sb.append(TIME_ATTRIBUTE);
			sb.append(" < '");
			sb.append(new Timestamp(dateTo.getTime()));

			sb.append("' ) ");
		}
		return conditionAdded;
	}

	// order=time#DESC;subject-ASC;predicate
	private void processOrder(StringBuilder sb)
	{
		List<String> list = queryParameters.get(ORDER_BY);
		if ((list != null) && (list.size() != 0))
		{
			String order = list.get(0);
			String[] attParts = order.split(";");
			sb.append("order by ");
			for (int i = 0; i != attParts.length; ++i)
			{
				String attPart = attParts[i];

				String[] parts = attPart.split("-");
				sb.append(parts[0]);
				String orderParam = "DESC";
				if (parts.length == 2)
				{
					orderParam = parts[1];
					if (!orderParam.equals("DESC") && !orderParam.equals("ASC"))
					{
						throw new IllegalArgumentException("Illegal order argument: " + orderParam);
					}
				}

				sb.append(" ");
				sb.append(orderParam);
				if (i < (attParts.length - 1))
				{
					sb.append(", ");
				}
			}
		}
	}

	public static QueryGenerator getInstance(Map<String, List<String>> parameters)
	{
		Map<String, List<String>> queryParams = new HashMap<String, List<String>>();

		// (parameters) Map<String, List<String>> --> (queryParams) Map<String, List<String>> ???

		for (Map.Entry<String, List<String>> entry : parameters.entrySet())
		{
			processEntry(entry, queryParams);
		}

		return new IntervalQuery(queryParams);
	}
}
