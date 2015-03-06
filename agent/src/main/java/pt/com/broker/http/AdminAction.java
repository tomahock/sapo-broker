package pt.com.broker.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.caudexorigo.Shutdown;
import org.caudexorigo.http.netty4.HttpAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.gcs.messaging.Gcs;

public class AdminAction extends HttpAction {
	
	private static final Logger log = LoggerFactory.getLogger(AdminAction.class);
	
	private ByteBuf BAD_REQUEST_RESPONSE;
	
	public AdminAction(){
		super();
			byte[] bad_arr;
			try {
				bad_arr = "<p>Only the POST verb is supported</p>".getBytes("UTF-8");
				BAD_REQUEST_RESPONSE = Unpooled.wrappedBuffer(bad_arr);
			} catch (UnsupportedEncodingException e) {
				log.error("Fatal JVM error!", e);
				Shutdown.now();
			}
	}

	@Override
	public void service(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		log.debug("AdminAction Called! Method used: {}", request.getMethod().toString());
		Channel channel = ctx.channel();
		if(request.getMethod().equals(HttpMethod.POST)){
			String action = request.content().toString(Charset.forName("UTF-8"));
			log.debug("Action received: {}", action);
			if(StringUtils.isBlank(action)){
				throw new IllegalArgumentException("No arguments supplied");
			}
			
			if(action.equals("SHUTDOWN")){
				Runnable kill = new Runnable()
				{
					public void run()
					{
						Shutdown.now();
					}
				};
				BrokerExecutor.schedule(kill, 1000, TimeUnit.MILLISECONDS);
			} else if (action.startsWith("QUEUE:")){
				String from = channel.remoteAddress().toString();
				String local = channel.localAddress().toString();
				String queueName = StringUtils.substringAfter(action, "QUEUE:");
				Gcs.deleteQueue(queueName);
				String message = String.format("[%s] Queue '%s' was deleted. Request from: '%s'%n", local, queueName, from);
				log.info(message);
				try {
					response.content().writeBytes(Unpooled.wrappedBuffer("Queue deleted".getBytes("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					log.error("Error retrieving bytes from UTF-8 string.", e);
				}
				response.setStatus(HttpResponseStatus.OK);
			}
		} else {
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			response.content().writeBytes(BAD_REQUEST_RESPONSE.duplicate());
		}
	}

}
