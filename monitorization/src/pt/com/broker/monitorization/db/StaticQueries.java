package pt.com.broker.monitorization.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.com.broker.monitorization.db.queries.AgentFaultsRateQuery;
import pt.com.broker.monitorization.db.queries.AgentInputRateQuery;
import pt.com.broker.monitorization.db.queries.AgentOutputRateQuery;
import pt.com.broker.monitorization.db.queries.AgentQueueCountQuery;
import pt.com.broker.monitorization.db.queries.FaultRateStaticQuery;
import pt.com.broker.monitorization.db.queries.InputRateStaticQuery;
import pt.com.broker.monitorization.db.queries.OutputRateStaticQuery;
import pt.com.broker.monitorization.db.queries.QueueCountStaticQuery;
import pt.com.broker.monitorization.db.queries.StaticQuery;
import pt.com.broker.monitorization.db.queries.SubscriptionDiscardedRateQuery;
import pt.com.broker.monitorization.db.queries.SubscriptionOutputRateQuery;

public class StaticQueries
{
	private static Map<String, StaticQuery> queries = new HashMap<String, StaticQuery>();
	private static final String QUERY_TYPE_PARAM = "querytype";

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

		AgentQueueCountQuery aqc = new AgentQueueCountQuery();
		queries.put(aqc.getId(), aqc);

		AgentFaultsRateQuery afrq = new AgentFaultsRateQuery();
		queries.put(afrq.getId(), afrq);

		AgentInputRateQuery airq = new AgentInputRateQuery();
		queries.put(airq.getId(), airq);

		AgentOutputRateQuery aorq = new AgentOutputRateQuery();
		queries.put(aorq.getId(), aorq);

		SubscriptionOutputRateQuery sorq = new SubscriptionOutputRateQuery();
		queries.put(sorq.getId(), sorq);
		
		SubscriptionDiscardedRateQuery  sdrq = new SubscriptionDiscardedRateQuery();
		queries.put(sdrq.getId(), sdrq);
		
	}

	public static String getData(String queryType, Map<String, List<String>> params)
	{
		List<String> list = params.get(QUERY_TYPE_PARAM);
		if ((list != null) && (list.size() == 1))
		{
			queryType = list.get(0);
		}

		StaticQuery sq = queries.get(queryType);
		if (sq == null)
		{
			return "";
		}
		return sq.getJsonData(params);
	}
}
