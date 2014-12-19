package pt.com.gcs.stats;

import java.util.List;

public class Statistics {
	
	private List<QueueStatistics> queueStatistics;
	private List<TopicStatistics> topicStatistics;
	private List<ChannelStatistics> channelStatistics;
	private SystemStatistics systemStatistics;
	
	public List<QueueStatistics> getQueueStatistics() {
		return queueStatistics;
	}
	public void setQueueStatistics(List<QueueStatistics> queueStatistics) {
		this.queueStatistics = queueStatistics;
	}
	public List<TopicStatistics> getTopicStatistics() {
		return topicStatistics;
	}
	public void setTopicStatistics(List<TopicStatistics> topicStatistics) {
		this.topicStatistics = topicStatistics;
	}
	public List<ChannelStatistics> getChannelStatistics() {
		return channelStatistics;
	}
	public void setChannelStatistics(List<ChannelStatistics> channelStatistics) {
		this.channelStatistics = channelStatistics;
	}
	public SystemStatistics getSystemStatistics() {
		return systemStatistics;
	}
	public void setSystemStatistics(SystemStatistics systemStatistics) {
		this.systemStatistics = systemStatistics;
	}

}
