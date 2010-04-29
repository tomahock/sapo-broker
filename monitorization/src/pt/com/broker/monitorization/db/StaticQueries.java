package pt.com.broker.monitorization.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.com.broker.monitorization.db.queries.FaultRateStaticQuery;
import pt.com.broker.monitorization.db.queries.InputRateStaticQuery;
import pt.com.broker.monitorization.db.queries.OutputRateStaticQuery;
import pt.com.broker.monitorization.db.queries.QueueCountStaticQuery;
import pt.com.broker.monitorization.db.queries.StaticQuery;

public class StaticQueries
{
	private static Map<String, StaticQuery> queries = new HashMap<String, StaticQuery>();

	static
	{
		QueueCountStaticQuery qsq = new QueueCountStaticQuery();
		queries.put(qsq.getId(), qsq);

		FaultRateStaticQuery frq = new FaultRateStaticQuery();
		queries.put(frq.getId(), frq);

		OutputRateStaticQuery orq = new OutputRateStaticQuery();
		queries.put(orq.getId(), orq);

		InputRateStaticQuery irq = new InputRateStaticQuery();
		queries.put(irq.getId(), irq);

	}

	public static String getData(String queryType, Map<String,List<String>> params)
	{
		StaticQuery sq = queries.get(queryType);
		if (sq == null)
		{
			return "";
		}
		return sq.getJsonData(params);
	}
}
