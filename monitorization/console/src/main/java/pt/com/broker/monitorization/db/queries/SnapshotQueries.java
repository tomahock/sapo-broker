package pt.com.broker.monitorization.db.queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.com.broker.monitorization.db.queries.agents.AgentStatusSnapshotQuery;
import pt.com.broker.monitorization.db.queries.queues.QueueCountSnapshotQuery;

public class SnapshotQueries implements QueryDataProvider
{
	private static Map<String, ComposedResultQuery> queries = new HashMap<String, ComposedResultQuery>();
	private static final String QUERY_TYPE_PARAM = "type";

	private static final String TYPE = "snapshot";

	static
	{
		QueueCountSnapshotQuery qcsq = new QueueCountSnapshotQuery();
		queries.put(qcsq.getId(), qcsq);

		DropboxCountSnapshotQuery dcsq = new DropboxCountSnapshotQuery();
		queries.put(dcsq.getId(), dcsq);

		SysMsgFailedDeliverySnapshotQuery smfdq = new SysMsgFailedDeliverySnapshotQuery();
		queries.put(smfdq.getId(), smfdq);

		AgentStatusSnapshotQuery assq = new AgentStatusSnapshotQuery();
		queries.put(assq.getId(), assq);
	}

	public String getData(String queryType, Map<String, List<String>> params)
	{
		List<String> list = params.get(QUERY_TYPE_PARAM);
		if ((list != null) && (list.size() == 1))
		{
			queryType = list.get(0);
		}

		ComposedResultQuery sq = queries.get(queryType);
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
