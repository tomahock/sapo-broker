package pt.com.broker.ws.responses;

public class QueueMessages {
	
	private String queueName;
	private MessageList messagesList;
	
	public QueueMessages(){
		
	}
	
	public QueueMessages(String queueName, MessageList messagesList){
		this.queueName = queueName;
		this.messagesList = messagesList;
	}

	public String getQueueName() {
		return queueName;
	}

	public MessageList getMessagesList() {
		return messagesList;
	}

}
