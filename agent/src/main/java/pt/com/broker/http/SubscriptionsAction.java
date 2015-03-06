package pt.com.broker.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;
import java.util.Map;

import org.caudexorigo.http.netty4.HttpAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.net.stats.NetStats;
import pt.com.gcs.net.stats.SubscriptionInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builds a agent status document with all main consumer/producer status. 
 * */
public class SubscriptionsAction extends HttpAction {
	
	private static final Logger log = LoggerFactory.getLogger(SubscriptionsAction.class);

	@Override
	public void service(ChannelHandlerContext ctx, FullHttpRequest request,	FullHttpResponse response) {
		String agentName = GcsInfo.constructAgentName(GcsInfo.getAgentHost(), GcsInfo.getAgentPort());
		
		log.debug("Agent name: {}", agentName);
		NetStats netStats = NetStats.getStats(agentName);
		Map<String, List<SubscriptionInfo>> subscriptions = netStats.getSubscriptions();
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(subscriptions);
			log.debug("Subscriptions: {}", json);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
