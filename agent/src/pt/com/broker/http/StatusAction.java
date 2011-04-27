package pt.com.broker.http;

import java.io.OutputStream;
import java.util.Date;

import org.caudexorigo.http.netty.HttpAction;
import org.caudexorigo.text.DateUtil;
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

/**
 * StatusAction outputs agent status in XML.
 * 
 */

public class StatusAction extends HttpAction
{
	private static final Logger log = LoggerFactory.getLogger(StatusAction.class);

	private static final String template = "<mq:Status xmlns:mq=\"http://services.sapo.pt/broker\">%n<mq:Message>%s</mq:Message>%n<mq:Timestamp>%s</mq:Timestamp>%n<mq:Version>%s</mq:Version>%n</mq:Status>";

	public StatusAction()
	{
	}

	@Override
	public void service(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response)
	{
		ChannelBuffer bbo = ChannelBuffers.dynamicBuffer();
		OutputStream out = new ChannelBufferOutputStream(bbo);
		Channel channel = ctx.getChannel();

		try
		{
			String smessage = String.format(template, "Agent is alive", DateUtil.formatISODate(new Date()), BrokerInfo.getVersion());
			byte[] bmessage = smessage.getBytes("UTF-8");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml");

			response.setStatus(HttpResponseStatus.OK);

			out.write(bmessage);
		}
		catch (Throwable e)
		{
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			log.error("HTTP Service error, cause:" + e.getMessage() + " client:" + channel.getRemoteAddress());
		}
		finally
		{
			response.setContent(bbo);
		}
	}
}
