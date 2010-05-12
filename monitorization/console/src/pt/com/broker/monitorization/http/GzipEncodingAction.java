package pt.com.broker.monitorization.http;

import org.caudexorigo.http.netty.StaticFileAction;

public class GzipEncodingAction extends StaticFileAction
{
	public GzipEncodingAction(String rootPath)
	{
		super(rootPath);
	}

	@Override
	public String getContentEncoding()
	{
		return "gzip";
	}
}