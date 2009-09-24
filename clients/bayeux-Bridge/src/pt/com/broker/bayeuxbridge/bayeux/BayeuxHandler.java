/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package pt.com.broker.bayeuxbridge.bayeux;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.HashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.bayeux.BayeuxConnection;
import org.jboss.netty.handler.codec.bayeux.BayeuxExt;
import org.jboss.netty.handler.codec.bayeux.BayeuxMessage;
import org.jboss.netty.handler.codec.bayeux.DisconnectRequest;
import org.jboss.netty.handler.codec.bayeux.HandshakeRequest;
import org.jboss.netty.handler.codec.bayeux.PublishRequest;
import org.jboss.netty.handler.codec.bayeux.PublishResponse;
import org.jboss.netty.handler.codec.bayeux.SubscribeRequest;
import org.jboss.netty.handler.codec.bayeux.UnsubscribeRequest;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import pt.com.broker.bayeuxbridge.CommunicationManager;

/**
 * 
 * @author daijun
 */
@ChannelPipelineCoverage("one")
public class BayeuxHandler extends SimpleChannelUpstreamHandler
{

	private volatile HttpRequest request;
	private volatile boolean readingChunks;
	private final StringBuilder responseContent = new StringBuilder();
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(BayeuxHandler.class.getName());
	private String root;

	public BayeuxHandler()
	{
	}

	public BayeuxHandler(String root)
	{
		this.root = root;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
	{
		if (e.getMessage() instanceof BayeuxConnection)
		{
			handleBayeuxMessage(ctx, (BayeuxConnection) e.getMessage(), e);
		}
		else if (!readingChunks && e.getMessage() instanceof HttpRequest)
		{
			request = (HttpRequest) e.getMessage();

			File file = new File(root + File.separator + request.getUri());
			if (file.exists() && file.isFile())
			{
				FileReader reader = new FileReader(file);
				BufferedReader bufread = new BufferedReader(reader);
				String read;
				while ((read = bufread.readLine()) != null)
				{
					responseContent.append(read + "\r\n");
				}
				writeResponse(e);
			}
			else
			{
				responseContent.append("Sapo-Broker Javascript Bridge<br/>");
				responseContent.append("===================================<br/>");
				responseContent.append("VERSION: " + request.getProtocolVersion().getText() + "<br/>");
				if (request.containsHeader(HttpHeaders.Names.HOST))
				{
					responseContent.append("HOSTNAME: " + request.getHeader(HttpHeaders.Names.HOST) + "<br/>");
				}
				responseContent.append("REQUEST_URI: " + request.getUri() + "<br/><br/>");
				if (!request.getHeaderNames().isEmpty())
				{
					for (String name : request.getHeaderNames())
					{
						for (String value : request.getHeaders(name))
						{
							responseContent.append("HEADER: " + name + " = " + value + "<br/>");
						}
					}
					responseContent.append("<br/>");
				}

				QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
				Map<String, List<String>> params = queryStringDecoder.getParameters();
				if (!params.isEmpty())
				{
					for (Entry<String, List<String>> p : params.entrySet())
					{
						String key = p.getKey();
						List<String> vals = p.getValue();
						for (String val : vals)
						{
							responseContent.append("PARAM: " + key + " = " + val + "<br/>");
						}
					}
					responseContent.append("<br/>");
				}

				if (request.isChunked())
				{
					readingChunks = true;
				}
				else
				{
					ChannelBuffer content = request.getContent();
					if (content.readable())
					{
						responseContent.append("CONTENT: " + content.toString("UTF-8") + "<br/>");
					}
					writeResponse(e);
				}
			}
		}
		else
		{
			HttpChunk chunk = (HttpChunk) e.getMessage();
			if (chunk.isLast())
			{
				readingChunks = false;
				responseContent.append("END OF CONTENT<\r\n>");
				writeResponse(e);
			}
			else
			{
				responseContent.append("CHUNK: " + chunk.getContent().toString("UTF-8") + "\r\n");
			}
		}
	}


	private void writeResponse(MessageEvent e)
	{
		// Convert the response content to a ChannelBuffer.
		ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent.toString(), "UTF-8");
		responseContent.setLength(0);

		// Decide whether to close the connection or not.
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.setContent(buf);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

		if (!close)
		{
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
		}

		String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
		if (cookieString != null)
		{
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if (!cookies.isEmpty())
			{
				// Reset the cookies if necessary.
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (Cookie cookie : cookies)
				{
					cookieEncoder.addCookie(cookie);
				}
				response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
			}
		}

		// Write the response.
		ChannelFuture future = e.getChannel().write(response);

		// Close the connection after the write operation is done if necessary.
		if (close)
		{
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
		e.getCause().printStackTrace();
		e.getChannel().close();
	}

	
	/****************/
	private void handleBayeuxMessage(ChannelHandlerContext ctx, BayeuxConnection connection, MessageEvent e)
	{
		List<BayeuxMessage> receivedMessages = new ArrayList<BayeuxMessage>();

		BayeuxMessage bayeuxMessage = connection.pollFromUpstream();

		while (bayeuxMessage != null)
		{
			if(bayeuxMessage instanceof DisconnectRequest)
			{
				if( handleDisconnectRequest((DisconnectRequest) bayeuxMessage, ctx, connection, e) )
					receivedMessages.add(bayeuxMessage);
			}
			else if (bayeuxMessage instanceof HandshakeRequest)
			{
				if( handleHandshakeRequest((HandshakeRequest) bayeuxMessage, ctx, connection, e) )
					receivedMessages.add(bayeuxMessage);
			}
			else if (bayeuxMessage instanceof SubscribeRequest)
			{
				if( handleSubscribeRequest((SubscribeRequest) bayeuxMessage, ctx, connection, e) )
					receivedMessages.add(bayeuxMessage);
			}
			else if (bayeuxMessage instanceof UnsubscribeRequest)
			{
				if(handleUnsubscribeRequest((UnsubscribeRequest) bayeuxMessage, ctx, connection, e))
					receivedMessages.add(bayeuxMessage);
			}
			else if (bayeuxMessage instanceof PublishRequest)
			{
				if( handlePublishRequest((PublishRequest) bayeuxMessage, ctx, connection, e) )
					receivedMessages.add(bayeuxMessage);
			}
			else
			{
				receivedMessages.add(bayeuxMessage);
			}
			
			bayeuxMessage = connection.pollFromUpstream();
		}

		connection.receiveToQueue(receivedMessages);

		ctx.getChannel().write(connection);
	}


	private boolean handleDisconnectRequest(DisconnectRequest bayeuxMessage, ChannelHandlerContext ctx, BayeuxConnection connection, MessageEvent e)
	{
		CommunicationManager.unregisterClient(connection.getClientId());
		return true;
	}

	private boolean handleHandshakeRequest(HandshakeRequest bayeuxMessage, ChannelHandlerContext ctx, BayeuxConnection connection, MessageEvent e)
	{
		/*
		// How to add information to ext field
		if(bayeuxMessage.getExt() == null)
			bayeuxMessage.setExt( new BayeuxExt(new HashMap() ) );
		
		System.out.println("#########Setting bayeux ext!!");
		bayeuxMessage.getExt().put("Teste", "a,b");
		*/
		
		return true;
	}
	
	private boolean handlePublishRequest(PublishRequest bayeuxMessage, ChannelHandlerContext ctx, BayeuxConnection connection, MessageEvent e)
	{
		//bayeuxMessage.setBypassPublish(true);
		System.out.println("########## Publish: " + bayeuxMessage.getData().get("subscription"));
		CommunicationManager.publish((String) bayeuxMessage.getData().get("subscription"), (String) bayeuxMessage.getData().get("data"));

		PublishResponse response=new PublishResponse(bayeuxMessage);
		response.setSuccessful(true);
		connection.sendToQueue(response);
		
		return false;
	}

	private boolean handleSubscribeRequest(SubscribeRequest bayeuxMessage, ChannelHandlerContext ctx, BayeuxConnection connection, MessageEvent e)
	{
		System.out.println("########## Subscription: " + bayeuxMessage.getSubscription());
		boolean sucess = CommunicationManager.registerChannel(bayeuxMessage.getSubscription(), connection.getClientId());
		
//		if(!sucess)
//		{
//			SubscribeResponse subscribeResponse = new SubscribeResponse(bayeuxMessage);
//			subscribeResponse.setSuccessful(false);
//			subscribeResponse.setError("403:"+ bayeuxMessage.getSubscription() + ":Permission Denied");
//			connection.sendToQueue(subscribeResponse);
//		}
			
		
		return sucess;
	}

	private boolean handleUnsubscribeRequest(UnsubscribeRequest bayeuxMessage, ChannelHandlerContext ctx, BayeuxConnection connection, MessageEvent e)
	{
		System.out.println("########## Unubscription: " + bayeuxMessage.getSubscription());
		CommunicationManager.unregisterChannel(bayeuxMessage.getSubscription(), connection.getClientId());
		
		return true;
	}

}
