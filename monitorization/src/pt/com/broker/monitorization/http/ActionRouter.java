package pt.com.broker.monitorization.http;

import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.http.netty.RequestRouter;
import org.caudexorigo.http.netty.StaticFileAction;
import org.jboss.netty.handler.codec.http.HttpRequest;

import pt.com.broker.monitorization.configuration.ConfigurationInfo;

public class ActionRouter implements RequestRouter
{
	private static final String DATA_PREFIX = "/data/";
	private static final String ACTION_PREFIX = "/action/";
	
	
	// These HttpAction are stateless
	private static final DataAction dataAction = new DataAction();
	private static final ActionExecuterAction actionExecuter = new ActionExecuterAction();
	
	
	@Override
	public HttpAction map(HttpRequest request)
	{
		String path = request.getUri();
		int index = path.indexOf('?');
		
		String uriBase = (index == -1) ? path : path.substring(0, index);
		
		String lowerUri = uriBase.toLowerCase();
		
		if (lowerUri.startsWith(DATA_PREFIX))
		{
			return dataAction;
		}
		else if(lowerUri.startsWith(ACTION_PREFIX))
		{
			return actionExecuter;
		}
		
		return new StaticFileAction(ConfigurationInfo.getWwwrootPath());
	}

}
