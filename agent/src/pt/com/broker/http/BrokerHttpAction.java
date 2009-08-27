package pt.com.broker.http;

import java.io.OutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.http.HttpMethod;
import org.apache.mina.filter.codec.http.HttpRequest;
import org.apache.mina.filter.codec.http.HttpResponseStatus;
import org.apache.mina.filter.codec.http.MutableHttpResponse;
import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AccessControl;
import pt.com.broker.auth.Session;
import pt.com.broker.auth.AccessControl.ValidationResult;
import pt.com.broker.codec.BrokerCodecRouter;
import pt.com.broker.codec.xml.SoapEnvelope;
import pt.com.broker.codec.xml.SoapSerializer;
import pt.com.broker.core.ErrorHandler;
import pt.com.broker.messaging.BrokerProducer;
import pt.com.broker.messaging.MQ;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.SimpleFramingDecoderV2;
import pt.com.http.HttpAction;

/**
 * BrokerHttpAction is an HttpAction. It supports message publishing.
 * 
 */

public class BrokerHttpAction extends HttpAction
{
	private static final String content_type = "text/xml";

	private static final Logger log = LoggerFactory.getLogger(BrokerHttpAction.class);

	private static final long serialVersionUID = 9072384515868129239L;

	private static final BrokerProducer _http_broker = BrokerProducer.getInstance();

	private IoBuffer BAD_REQUEST_RESPONSE;
	private IoBuffer ACCESS_DENIED_REQUEST_RESPONSE;
	private IoBuffer INVALID_MESSAGE_TYPE_RESPONSE;

	public BrokerHttpAction()
	{
		super();
		try
		{
			byte[] bad_arr = "<p>Only the POST verb is supported</p>".getBytes("UTF-8");
			BAD_REQUEST_RESPONSE = IoBuffer.allocate(bad_arr.length);
			BAD_REQUEST_RESPONSE.put(bad_arr);
			BAD_REQUEST_RESPONSE.flip();

			bad_arr = "<p>Access denied</p>".getBytes("UTF-8");
			ACCESS_DENIED_REQUEST_RESPONSE = IoBuffer.allocate(bad_arr.length);
			ACCESS_DENIED_REQUEST_RESPONSE.put(bad_arr);
			ACCESS_DENIED_REQUEST_RESPONSE.flip();

			bad_arr = "<p>Invalid message type. Only publish is supported (topic and queue).</p>".getBytes("UTF-8");
			INVALID_MESSAGE_TYPE_RESPONSE = IoBuffer.allocate(bad_arr.length);
			INVALID_MESSAGE_TYPE_RESPONSE.put(bad_arr);
			INVALID_MESSAGE_TYPE_RESPONSE.flip();
		}
		catch (Throwable error)
		{
			log.error("Fatal JVM error!", error);
			Shutdown.now();
		}
	}

	@Override
	public void writeResponse(IoSession session, HttpRequest request, MutableHttpResponse response)
	{

		try
		{
			Session sessionProps = null;
			Object obj = session.getAttribute("BROKER_SESSION_PROPERTIES");
			if (obj != null)
			{
				sessionProps = (Session) obj;
			}
			else
			{
				sessionProps = new Session(session);
			}

			if (request.getMethod().equals(HttpMethod.POST))
			{
				IoBuffer bb = (IoBuffer) request.getContent();
				byte[] buf = new byte[bb.limit()];
				bb.position(0);
				bb.get(buf);

				SimpleFramingDecoderV2 xmlDecoder = (SimpleFramingDecoderV2) BrokerCodecRouter.getProcolCodec((short) 0).getDecoder(null);
				NetMessage message = (NetMessage) xmlDecoder.processBody(buf, (short) 0, (short) 0);

				ValidationResult validationResult = AccessControl.validate(message, sessionProps);

				if (!validationResult.accessGranted)
				{
					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR); // Internal
					// server
					// error?...
					response.setContent(ACCESS_DENIED_REQUEST_RESPONSE.duplicate());
					return;
				}

				if ((message.getAction().getActionType() != NetAction.ActionType.PUBLISH) || (message.getAction().getPublishMessage() == null))
				{
					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR); // Internal
					// server
					// error?...
					response.setContent(INVALID_MESSAGE_TYPE_RESPONSE.duplicate());
					return;
				}

				_http_broker.publishMessage(message.getAction().getPublishMessage(), MQ.requestSource(message));
				response.setStatus(HttpResponseStatus.ACCEPTED);
			}
			else
			{
				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR); // Internal
				// server
				// error?...
				response.setContent(BAD_REQUEST_RESPONSE.duplicate());
			}
		}
		catch (Throwable e)
		{
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			fault(null, e, response);
			if (log.isErrorEnabled())
			{
				log.error("HTTP Service error, cause:" + e.getMessage() + ". Client address:" + session.getRemoteAddress());
			}
		}
	}

	public void fault(String faultCode, Throwable cause, MutableHttpResponse response)
	{
		IoBuffer bbf = IoBuffer.allocate(1024);
		bbf.setAutoExpand(true);
		OutputStream out = bbf.asOutputStream();

		SoapEnvelope ex_msg = ErrorHandler.buildSoapFault(faultCode, cause).Message;
		SoapSerializer.ToXml(ex_msg, out);
		bbf.flip();
		response.setContentType(content_type);
		response.setContent(bbf);
	}

}
