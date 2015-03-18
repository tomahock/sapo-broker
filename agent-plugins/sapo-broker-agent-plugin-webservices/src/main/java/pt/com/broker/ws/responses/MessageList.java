package pt.com.broker.ws.responses;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import pt.com.broker.types.NetMessage;

public class MessageList {
	
	@JsonProperty("messages")
	private List<NetMessage> messages;
	
	public MessageList(){
		
	}
	
	public MessageList(List<NetMessage> messages){
		this.messages = messages;
	}

	public List<NetMessage> getMessages() {
		return messages;
	}

}
