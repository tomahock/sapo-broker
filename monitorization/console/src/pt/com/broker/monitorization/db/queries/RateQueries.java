package pt.com.broker.monitorization.db.queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.com.broker.monitorization.db.queries.agents.AgentFaultsRateQuery;
import pt.com.broker.monitorization.db.queries.agents.AgentInputRateQuery;
import pt.com.broker.monitorization.db.queries.agents.AgentOutputRateQuery;
import pt.com.broker.monitorization.db.queries.agents.AgentQueueCountQuery;
import pt.com.broker.monitorization.db.queries.faults.FaultRateStaticQuery;
import pt.com.broker.monitorization.db.queries.queues.QueueCountRateQuery;
import pt.com.broker.monitorization.db.queries.queues.QueueCountStaticQuery;
import pt.com.broker.monitorization.db.queries.queues.QueueInputRateQuery;
import pt.com.broker.monitorization.db.queries.queues.QueueOutputRateQuery;
import pt.com.broker.monitorization.db.queries.subscriptions.SubscriptionDiscardedRateQuery;
import pt.com.broker.monitorization.db.queries.subscriptions.SubscriptionOutputRateQuery;
import pt.com.broker.monitorization.db.queries.subscriptions.SubscriptionRateQuery;

public class RateQueries implements QueryDataProvider
{
	private static Map<String, StaticQuery> queries = new HashMap<String, StaticQuery>();
	private static final String QUERY_TYPE_PARAM = "ratetype";

	private static final String TYPE = "rate";

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

		SubscriptionRateQuery sorq = new SubscriptionOutputRateQuery();
		queries.put(sorq.getId(), sorq);

		SubscriptionDiscardedRateQuery sdrq = new SubscriptionDiscardedRateQuery();
		queries.put(sdrq.getId(), sdrq);

		AgentQueueCountQuery aqc = new AgentQueueCountQuery();
		queries.put(aqc.getId(), aqc);

		AgentFaultsRateQuery afrq = new AgentFaultsRateQuery();
		queries.put(afrq.getId(), afrq);

		AgentInputRateQuery airq = new AgentInputRateQuery();
		queries.put(airq.getId(), airq);

		AgentOutputRateQuery aorq = new AgentOutputRateQuery();
		queries.put(aorq.getId(), aorq);

		QueueCountRateQuery qcrq = new QueueCountRateQuery();
		queries.put(qcrq.getId(), qcrq);

		QueueInputRateQuery qirq = new QueueInputRateQuery();
		queries.put(qirq.getId(), qirq);

		QueueOutputRateQuery qorq = new QueueOutputRateQuery();
		queries.put(qorq.getId(), qorq);
	}

	public String getData(String queryType, Map<String, List<String>> params)
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

	@Override
	public String getType()
	{
		return TYPE;
	}
}
