package pt.com.gcs.stats;

public class ChannelStatistics {
	
	private String channelName;
	private Long receivedMessages;
	
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public Long getReceivedMessages() {
		return receivedMessages;
	}
	public void setReceivedMessages(Long receivedMessages) {
		this.receivedMessages = receivedMessages;
	}

}
