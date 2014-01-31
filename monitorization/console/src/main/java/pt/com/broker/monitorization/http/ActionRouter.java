package pt.com.broker.monitorization.http;

import java.net.URI;

import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.http.netty.RequestRouter;
import org.caudexorigo.http.netty.StaticFileAction;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class ActionRouter implements RequestRouter
{
	private static final String DATAQUERY_PREFIX = "/dataquery/";
	private static final String AGENT_NAME = "/hostname";

	// These HttpAction are stateless
	// private static final DataAction dataAction = new DataAction();
	private static final DataQueryAction dataQueryAction = new DataQueryAction(DATAQUERY_PREFIX);
	private static final HostnameAction hostnameAction = new HostnameAction(AGENT_NAME);
	private static final RedirectAction redirect = new RedirectAction("/main.html");

	private final URI root_uri;

	public ActionRouter(URI root_uri)
	{
		this.root_uri = root_uri;
	}

	@Override
	public HttpAction map(HttpRequest request)
	{
		String path = request.getUri();

		int index = path.indexOf('?');

		String uriBase = (index == -1) ? path : path.substring(0, index);

		String lowerUri = uriBase.toLowerCase();

		if (lowerUri.startsWith(DATAQUERY_PREFIX))
		{
			return dataQueryAction;
		}
		else if (lowerUri.startsWith(AGENT_NAME))
		{
			return hostnameAction;
		}
		else if (path.endsWith(".gz.js"))
		{
			return new GzipEncodingAction(root_uri);
		}
		else if (path.equals("/"))
		{
			return redirect;
		}

		return new StaticFileAction(root_uri);
	}
}