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
package pt.com.broker.monitorization.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.caudexorigo.text.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
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

import pt.com.broker.monitorization.actions.DeleteQueue;

@ChannelPipelineCoverage("one")
public class MonitorizationHandler extends SimpleChannelUpstreamHandler
{
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(MonitorizationHandler.class.getName());

	private static final String DATA_PREFIX = "/data/";
	private static final String ACTION_PREFIX = "/action/";

	private volatile HttpRequest request;
	private volatile boolean readingChunks;
	private final StringBuilder responseContent = new StringBuilder();
	private String root;

	public MonitorizationHandler()
	{
	}

	public MonitorizationHandler(String root)
	{
		this.root = root;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
	{
		request = (HttpRequest) e.getMessage();
		
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
		String path = queryStringDecoder.getPath();
		Map<String, List<String>> params = queryStringDecoder.getParameters();
		
		String lowerUri = request.getUri().toLowerCase();
		
		if (!readingChunks && e.getMessage() instanceof HttpRequest)
		{
			File file = new File(root + path);
			if (file.exists() && file.isFile())
			{
				FileReader reader = new FileReader(file);
				//InputStreamReader isr = new InputStreamReader(reader);
				
				BufferedReader bufread = new BufferedReader(reader);
				String read;
				while ((read = bufread.readLine()) != null)
				{
					responseContent.append(read + "\r\n");
				}
				writeResponse(e);
			}
			else if (lowerUri.startsWith(DATA_PREFIX))
			{
				path = path.substring(DATA_PREFIX.length()).toLowerCase();
				
				String data = DataFetcher.getData(path, params);
				responseContent.append(data);
				writeResponse(e, "application/json");
			}
			else if (lowerUri.startsWith(ACTION_PREFIX))
			{
				path = path.substring(ACTION_PREFIX.length()).toLowerCase();
				
				String data = ActionExecutor.execute(path, params);
				responseContent.append(data);
				writeResponse(e, "application/json");
			}
			else
			{
				responseContent.append("Sapo-Broker Monitorization HTTP Server<br/>");
				responseContent.append("======================================<br/>");
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

	private Pattern equalsPattern = Pattern.compile("=");
	private Map<String, String> extractParameters(String queryString)
	{
		Map<String, String> params = new HashMap<String, String>();
		String[] parts =  queryString.split("&");
		
		for(String part : parts)
		{
			String[] paramParts = equalsPattern.split(part);
			if(paramParts.length != 2 )
				continue;
			params.put(paramParts[0], paramParts[1]);
		}
		return params;
	}
	
	private void writeResponse(MessageEvent e)
	{
		writeResponse(e, "text/html");
	}

	private void writeResponse(MessageEvent e, String content_type)
	{
		// Convert the response content to a ChannelBuffer.
		ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent.toString(), "UTF-8");
		responseContent.setLength(0);

		// Decide whether to close the connection or not.
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.setContent(buf);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, content_type + "; charset=UTF-8");

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
}
