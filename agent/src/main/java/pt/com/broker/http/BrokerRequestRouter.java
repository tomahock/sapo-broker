package pt.com.broker.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.caudexorigo.http.netty4.HttpAction;
import org.caudexorigo.http.netty4.RequestRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BrokerRequestRouter routes incoming HTTP request to the respective handlers.
 * 
 */

public class BrokerRequestRouter implements RequestRouter
{

	public static final String ACTION_ROUTE = "/broker/producer";
	public static final String ADMIN_ROUTE = "/broker/admin";
	public static final String STATUS_ROUTE = "/broker/status";
	public static final String SUBSCRIPTIONS_ROUTE = "/broker/subscriptions";

	private static final Logger log = LoggerFactory.getLogger(BrokerRequestRouter.class);

	private final Map<String, HttpAction> routes = new HashMap<String, HttpAction>();

	// Actions instantiation
	private final BrokerHttpAction brokerAction = new BrokerHttpAction();
	private final AdminAction adminAction = new AdminAction();
	private final StatusAction statusAction = new StatusAction();
	private final SubscriptionsAction subscriptionsAction = new SubscriptionsAction();

	public BrokerRequestRouter()
	{
		routes.put(ACTION_ROUTE, brokerAction);
		routes.put(ADMIN_ROUTE, adminAction);
		routes.put(STATUS_ROUTE, statusAction);
		routes.put(SUBSCRIPTIONS_ROUTE, subscriptionsAction);
	}

	public HttpAction map(FullHttpRequest req)
	{
		String path = StringUtils.substringBefore(req.getUri(), "?");
		log.debug("Trying to map HttpRequest: {}", path);
		return routes.get(path);
	}

	@Override
	public HttpAction map(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
	{
		return map(fullHttpRequest);
	}
}