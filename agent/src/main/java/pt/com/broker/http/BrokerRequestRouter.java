package pt.com.broker.http;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.caudexorigo.http.netty4.HttpAction;
import org.caudexorigo.http.netty4.RequestRouter;

/**
 * BrokerRequestRouter routes incoming HTTP request to the respective handlers.
 * 
 */

public class BrokerRequestRouter implements RequestRouter
{
	private final BrokerHttpAction broker_action = new BrokerHttpAction();

	private final StatusAction status_action = new StatusAction();

	public HttpAction map(FullHttpRequest req)
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

		return null;
	}


    @Override
    public HttpAction map(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
       return map(fullHttpRequest);
    }
}