package pt.com.broker.http;

import java.io.OutputStream;
import java.util.Date;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.caudexorigo.http.netty4.HttpAction;
import org.caudexorigo.text.DateUtil;

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
	public void service(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response)
	{
		ByteBuf bbo = Unpooled.buffer();
		OutputStream out = new ByteBufOutputStream(bbo);
		Channel channel = ctx.channel();

		try
		{
			String smessage = String.format(template, "Agent is alive", DateUtil.formatISODate(new Date()), BrokerInfo.getVersion());
			byte[] bmessage = smessage.getBytes("UTF-8");
			response.headers().set("Pragma", "no-cache");
			response.headers().set("Cache-Control", "no-cache");
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/xml");

			response.setStatus(HttpResponseStatus.OK);

			out.write(bmessage);
		}
		catch (Throwable e)
		{
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			log.error("HTTP Service error, cause:" + e.getMessage() + " client:" + channel.remoteAddress());
		}
		finally
		{
			response.content().writeBytes(bbo);
		}
	}
}
