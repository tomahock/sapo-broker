package pt.com.broker.ws.responses;

import java.util.ArrayList;
import java.util.List;

import pt.com.broker.ws.models.Queue;

import com.google.common.base.Preconditions;
//import org.codehaus.jackson.annotate.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueList
{

	public static final String JSON_PROP_QUEUES = "queues";
	public static final String JSON_PROP_QUEUES_COUNT = "total_queues";

	@JsonProperty(JSON_PROP_QUEUES)
	private List<Queue> queues;
	@JsonProperty(JSON_PROP_QUEUES_COUNT)
	private Integer count;

	public QueueList()
	{
		this.queues = new ArrayList<Queue>();
		this.count = 0;
	}

	public QueueList(List<Queue> queues)
	{
		Preconditions.checkNotNull(queues, "The queues list cannot be null.");
		this.queues = queues;
		this.count = queues.size();
	}

	public List<Queue> getQueues()
	{
		return queues;
	}

	public Integer getCount()
	{
		return count;
	}

}
