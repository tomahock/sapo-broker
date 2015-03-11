package pt.com.broker.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.http.views.MustacheHttpAction;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.net.stats.NetStats;
import pt.com.gcs.net.stats.SubscriptionInfo;

/**
 * Builds a agent status document with all main consumer/producer status. 
 * */
public class SubscriptionsAction extends MustacheHttpAction {
	
	public static final String SUBSCRIPTIONS_VIEW = "views/subscriptions.mustache";
	
	public SubscriptionsAction() {
		super(SUBSCRIPTIONS_VIEW);
	}

	private static final Logger log = LoggerFactory.getLogger(SubscriptionsAction.class);
	
	public static class AgentSubscriptions {
		
		private final String agentName;
		private final Map<String, List<SubscriptionInfo>> subscriptions;
		
		public AgentSubscriptions(String agentName,
				Map<String, List<SubscriptionInfo>> subscriptions) {
			this.agentName = agentName;
			this.subscriptions = subscriptions;
		}

		public String getAgentName() {
			return agentName;
		}

		public Set<Entry<String, List<SubscriptionInfo>>> getSubscriptions() {
			return subscriptions.entrySet();
		}
		
	}

	@Override
	protected Object buildViewObject(ChannelHandlerContext ctx,
			FullHttpRequest req, FullHttpResponse resp) {
		String agentName = GcsInfo.constructAgentName(GcsInfo.getAgentHost(), GcsInfo.getAgentPort());
		log.debug("Agent name: {}", agentName);
		NetStats netStats = NetStats.getStats(agentName);
		Map<String, List<SubscriptionInfo>> subscriptions = netStats.getSubscriptions();
		AgentSubscriptions agentSubscriptions = new AgentSubscriptions(agentName, subscriptions);
		return agentSubscriptions;
	}

}
