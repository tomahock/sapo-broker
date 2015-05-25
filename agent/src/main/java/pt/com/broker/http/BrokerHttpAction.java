package pt.com.broker.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.OutputStream;

import org.caudexorigo.Shutdown;
import org.caudexorigo.http.netty4.HttpAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AccessControl;
import pt.com.broker.auth.AccessControl.ValidationResult;
import pt.com.broker.auth.Session;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.codec.xml.SoapSerializer;
import pt.com.broker.codec.xml.soap.SoapEnvelope;
import pt.com.broker.core.ErrorHandler;
import pt.com.broker.messaging.BrokerProducer;
import pt.com.broker.messaging.MQ;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.stats.ChannelStats;

/**
 * BrokerHttpAction is an HttpAction. It supports message publishing.
 * 
 */

public class BrokerHttpAction extends HttpAction
{
	private static final String content_type = "text/xml";

	private static final Logger log = LoggerFactory.getLogger(BrokerHttpAction.class);

	private static final BrokerProducer _http_broker = BrokerProducer.getInstance();

	private static final SoapBindingSerializer bindingSerializer = new SoapBindingSerializer();

	private ByteBuf BAD_REQUEST_RESPONSE;
	private ByteBuf ACCESS_DENIED_REQUEST_RESPONSE;
	private ByteBuf INVALID_MESSAGE_TYPE_RESPONSE;

	public BrokerHttpAction()
	{
		super();
		try
		{
			byte[] bad_arr = "<p>Only the POST verb is supported</p>".getBytes("UTF-8");
			BAD_REQUEST_RESPONSE = Unpooled.wrappedBuffer(bad_arr);

			bad_arr = "<p>Access denied</p>".getBytes("UTF-8");
			ACCESS_DENIED_REQUEST_RESPONSE = Unpooled.wrappedBuffer(bad_arr);

			bad_arr = "<p>Invalid message type. Only publish is supported (topic and queue).</p>".getBytes("UTF-8");
			INVALID_MESSAGE_TYPE_RESPONSE = Unpooled.wrappedBuffer(bad_arr);
		}
		catch (Throwable error)
		{
			log.error("Fatal JVM error!", error);
			Shutdown.now();
		}
	}

	@Override
	public void service(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response)
	{
		Channel channel = ctx.channel();
		try
		{
			if (request.getMethod().equals(HttpMethod.POST))
			{
				ByteBuf bb = request.content();

				NetMessage message = (NetMessage) bindingSerializer.unmarshal(new ByteBufInputStream(bb));

				ChannelStats.newHttpMessageReceived();

				ValidationResult validationResult = AccessControl.validate(message, new Session(channel));

				if (!validationResult.accessGranted)
				{
					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
					response.content().writeBytes(ACCESS_DENIED_REQUEST_RESPONSE.duplicate());
					return;
				}

				if ((message.getAction().getActionType() != NetAction.ActionType.PUBLISH) || (message.getAction().getPublishMessage() == null))
				{
					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
					response.content().writeBytes(INVALID_MESSAGE_TYPE_RESPONSE.duplicate());
					return;
				}

				if (message.getAction().getPublishMessage().getDestinationType() == NetAction.DestinationType.TOPIC)
				{
					_http_broker.publishMessage(message.getAction().getPublishMessage(), MQ.requestSource(message));
				}
				else if (message.getAction().getPublishMessage().getDestinationType() == NetAction.DestinationType.QUEUE)
				{
					_http_broker.enqueueMessage(message.getAction().getPublishMessage(), MQ.requestSource(message));
				}

				response.setStatus(HttpResponseStatus.ACCEPTED);
			}
			else
			{
				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR); // Internal
				// server
				// error?...
				response.content().writeBytes(BAD_REQUEST_RESPONSE.duplicate());
			}
		}
		catch (Throwable e)
		{
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			fault(null, e, response);
			if (log.isErrorEnabled())
			{
				log.error("HTTP Service error, cause: '{}'. Client address: '{}'", e.getMessage(), channel.remoteAddress());
			}
		}
	}

	public void fault(String faultCode, Throwable cause, FullHttpResponse response)
	{
		try
		{
			ByteBuf bbf = Unpooled.buffer();
			OutputStream out = new ByteBufOutputStream(bbf);

			SoapEnvelope ex_msg = ErrorHandler.buildSoapFault(faultCode, cause).Message;
			SoapSerializer.ToXml(ex_msg, out);
			out.flush();
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, content_type);
			response.content().writeBytes(bbf);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}
}