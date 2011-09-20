package pt.com.broker.http;

import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.http.netty.RequestRouter;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * BrokerRequestRouter routes incoming HTTP request to the respective handlers.
 * 
 */

public class BrokerRequestRouter implements RequestRouter
{
	private final BrokerHttpAction broker_action = new BrokerHttpAction();

	private final StatusAction status_action = new StatusAction();

	private final AdminAction admin_action = new AdminAction();

	private final SubscriptionsAction subscription_action = new SubscriptionsAction();

	private final MiscInfoAction misc_action = new MiscInfoAction();

	public HttpAction map(HttpRequest req)
	{
		String path = req.getUri();

		if (path.equals("/broker/producer"))
		{
			return broker_action;
		}
		else if (path.equals("/broker/status"))
		{
			return status_action;
		}
		else if (path.equals("/broker/admin"))
		{
			return admin_action;
		}
		else if (path.equals("/broker/subscriptions"))
		{
			return subscription_action;
		}
		else if (path.equals("/broker/miscinfo"))
		{
			return misc_action;
		}

		return null;
	}
}