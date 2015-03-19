package pt.com.broker.ws.responses;

import java.util.ArrayList;
import java.util.List;

import jersey.repackaged.com.google.common.base.Preconditions;
import pt.com.broker.ws.models.Message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageList {
	
	public static final String JSON_PROP_MESSAGES 			= "messages";
	public static final String JSON_PROP_MESSAGES_COUNT 	= "total_messages";
	
	@JsonProperty(JSON_PROP_MESSAGES)
	private List<Message> messages;
	@JsonProperty(JSON_PROP_MESSAGES_COUNT)
	private Integer count;
	
	public MessageList(){
		this.messages = new ArrayList<Message>();
		this.count = 0;
	}
	
	public MessageList(List<Message> messages){
		Preconditions.checkNotNull(messages, "The message list cannot be null.");
		this.messages = messages;
		this.count = messages.size();
	}

	public List<Message> getMessages() {
		return messages;
	}

	public Integer getCount() {
		return count;
	}

}
