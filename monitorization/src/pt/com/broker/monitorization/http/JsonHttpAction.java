package pt.com.broker.monitorization.http;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.caudexorigo.http.netty.HttpAction;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;


public abstract class JsonHttpAction extends HttpAction
{

	@Override
	public void writeResponse(ChannelHandlerContext context, HttpRequest request, HttpResponse response)
	{
		String path = request.getUri();
//		int index = path.indexOf('?');

//		String uriBase = (index == -1) ? path : path.substring(0, index);
//
//		String lowerUri = uriBase.toLowerCase();

		path = path.substring(getPrefix().length()).toLowerCase();

		HashMap<String, String> params = getParams(request);

		String data = getData(path, params);

		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(ChannelBuffers.BIG_ENDIAN, data, Charset.forName("utf-8"));

		response.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
		response.addHeader(HttpHeaders.Names.CONTENT_ENCODING, "UTF-8");
		response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.writerIndex());

		response.setContent(buffer);
	}

	protected abstract String getPrefix();
	protected abstract String getData(String path, Map<String, String> params);
	
	private static HashMap<String,String> getParams(HttpRequest request)
	{
		HashMap<String, String> headers = new HashMap<String, String>();
		
		String uri = request.getUri();
		int index = uri.indexOf('?');
		if(index == -1)
		{
			return headers;
		}
		String queryString =uri.substring(index+1); 
		String[] parts = queryString.split("&|=");
		
		if( (parts.length %2) != 0)
		{
			return headers;
		}
		
		for(int i = 0; i != parts.length; )
		{
			String name =  parts[i];
			String value =  parts[i+1];
			i+=2;
			
			headers.put(name, value);
		}	
		
		return headers; 
	}
	
}
