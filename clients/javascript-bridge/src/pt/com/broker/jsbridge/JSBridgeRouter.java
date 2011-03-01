package pt.com.broker.jsbridge;

import java.net.URI;

import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.http.netty.RequestRouter;
import org.caudexorigo.http.netty.StaticFileAction;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class JSBridgeRouter implements RequestRouter
{

	private final HttpAction static_file;

	public JSBridgeRouter(URI root_uri)
	{
		super();
		static_file = new StaticFileAction(root_uri);
	}

	@Override
	public HttpAction map(HttpRequest req)
	{
		return static_file;
	}

}