package pt.com.broker.jsbridge;

import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.http.netty.RequestRouter;
import org.caudexorigo.http.netty.StaticFileAction;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class JSBridgeRouter implements RequestRouter
{

	private final HttpAction static_file;

	public JSBridgeRouter(String root_path)
	{
		super();
		static_file = new StaticFileAction(root_path);
	}

	@Override
	public HttpAction map(HttpRequest arg0)
	{
		return static_file;
	}

}
