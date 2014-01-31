package pt.com.broker.http;

import java.io.IOException;
import java.io.OutputStream;

import org.caudexorigo.Shutdown;
import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.io.IOUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerInfo;
import pt.com.gcs.conf.GcsInfo;

/**
 * StatusAction outputs agent status in XML.
 * 
 */

public class MiscInfoAction extends HttpAction
{
	private static final Logger log = LoggerFactory.getLogger(MiscInfoAction.class);

	private static final String templateLocation = "/pt/com/broker/http/miscinfo.template";

	private static String template;

	public MiscInfoAction()
	{
		try
		{
			template = IOUtils.toString(MiscInfoAction.class.getResourceAsStream(templateLocation));
		}
		catch (IOException e)
		{
			Shutdown.now(e);
		}
	}

	@Override
	public void service(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response)
	{
		ChannelBuffer bbo = ChannelBuffers.dynamicBuffer();
		OutputStream out = new ChannelBufferOutputStream(bbo);
		Channel channel = ctx.getChannel();

		try
		{
			if (template != null)
			{
				String agentName = GcsInfo.constructAgentName(GcsInfo.getAgentHost(), GcsInfo.getAgentPort());

				String smessage = String.format(template, SubscriptionsAction.getCss(), agentName, BrokerInfo.getVersion());
				byte[] bmessage = smessage.getBytes("UTF-8");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Cache-Control", "no-cache");
				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");

				response.setStatus(HttpResponseStatus.OK);

				out.write(bmessage);
			}
			else
			{
				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
				out.write("No template loadded.".getBytes("UTF-8"));
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			log.error("HTTP Service error, cause:" + e.getMessage() + " client:" + channel.getRemoteAddress());
		}
		finally
		{
			response.setContent(bbo);
		}
	}
}