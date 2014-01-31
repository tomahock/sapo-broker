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
	private static final String ENCODING = "UTF-8";
	private static final String CONTENT_TYPE = "application/json";
	private static final Charset UTF8 = Charset.forName("utf-8");

	@Override
	public void service(ChannelHandlerContext context, HttpRequest request, HttpResponse response)
	{
		String path = request.getUri();

		path = path.substring(getPrefix().length()).toLowerCase();

		HashMap<String, String> params = getParams(request);

		String data = getData(path, params);

		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(ChannelBuffers.BIG_ENDIAN, data, UTF8);

		response.addHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
		response.addHeader(HttpHeaders.Names.CONTENT_ENCODING, ENCODING);

		response.setContent(buffer);
	}

	protected abstract String getPrefix();

	protected abstract String getData(String path, Map<String, String> params);

	private static HashMap<String, String> getParams(HttpRequest request)
	{
		HashMap<String, String> headers = new HashMap<String, String>();

		String uri = request.getUri();
		int index = uri.indexOf('?');
		if (index == -1)
		{
			return headers;
		}
		String queryString = uri.substring(index + 1);
		String[] parts = queryString.split("&|=");

		if ((parts.length % 2) != 0)
		{
			return headers;
		}

		for (int i = 0; i != parts.length;)
		{
			String name = parts[i];
			String value = parts[i + 1];
			i += 2;

			headers.put(name, value);
		}

		return headers;
	}

}
