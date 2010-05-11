package pt.com.broker.monitorization.http;

import org.caudexorigo.http.netty.HttpAction;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class RedirectAction extends HttpAction
{

	private final String path;

	public RedirectAction(String path)
	{
		super();
		this.path = path;
	}

	@Override
	public void writeResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res)
	{
		res.addHeader(HttpHeaders.Names.LOCATION, path);

	}

}
