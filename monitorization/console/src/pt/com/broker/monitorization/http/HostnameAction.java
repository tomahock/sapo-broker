package pt.com.broker.monitorization.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.caudexorigo.http.netty.HttpAction;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import pt.com.broker.monitorization.AgentHostname;

public class HostnameAction extends HttpAction
{
	private final String pathPrefix;

	private final static String NAME = "name";

	public HostnameAction(String queryPrefix)
	{
		pathPrefix = queryPrefix;
	}

	public void service(ChannelHandlerContext context, HttpRequest request, HttpResponse response)
	{
		String path = request.getUri();

		path = path.substring(pathPrefix.length()).toLowerCase();

		Map<String, List<String>> params = getParams(request);

		List<String> result = params.get(NAME);

		String hostname = null;

		if ((result != null) && (result.size() == 1))
		{
			String name = result.get(0);
			hostname = AgentHostname.get(name);
		}

		StringBuffer sb = new StringBuffer();

		sb.append("{");

		if (hostname != null)
		{
			sb.append("\"hostname\":");
			sb.append("\"");
			sb.append(hostname);
			sb.append("\"");
		}

		sb.append("}");

		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(ChannelBuffers.BIG_ENDIAN, sb.toString(), Charset.forName("utf-8"));

		response.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
		response.addHeader(HttpHeaders.Names.CONTENT_ENCODING, "UTF-8");
		response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.writerIndex());

		response.setContent(buffer);
	}

	private static Map<String, List<String>> getParams(HttpRequest request)
	{
		return new QueryStringDecoder(request.getUri()).getParameters();
	}
}
