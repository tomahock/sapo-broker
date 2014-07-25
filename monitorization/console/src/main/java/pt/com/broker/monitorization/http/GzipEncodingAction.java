package pt.com.broker.monitorization.http;

import org.caudexorigo.http.netty.StaticFileAction;

import java.net.URI;

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