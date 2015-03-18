package pt.com.broker.ws.responses;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import pt.com.broker.ws.models.Queue;

public class QueueList {
	
	@JsonProperty("queues")
	private List<Queue> queues;
	
	public QueueList(){
		
	}
	
	public QueueList(List<Queue> queues){
		this.queues = queues;
	}

	public List<Queue> getQueues() {
		return queues;
	}
	
}
