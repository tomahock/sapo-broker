package pt.com.broker.monitorization.http;

import java.net.URI;

import org.caudexorigo.http.netty.StaticFileAction;

public class GzipEncodingAction extends StaticFileAction
{
	public GzipEncodingAction(URI root_uri)
	{
		super(root_uri);
	}

	@Override
	public String getContentEncoding()
	{
		return "gzip";
	}
}