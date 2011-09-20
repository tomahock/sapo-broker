package pt.com.broker.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import pt.com.broker.types.MessageListener;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InboundRemoteChannels;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;
import pt.com.gcs.messaging.TopicProcessor;
import pt.com.gcs.messaging.TopicProcessorList;

/**
 * StatusAction outputs agent status in XML.
 * 
 */

public class SubscriptionsAction extends HttpAction
{
	private static final Logger log = LoggerFactory.getLogger(SubscriptionsAction.class);

	private static final String NO_SUBSCRIPTIONS = "<p>No subscriptions</p>";
	private static final String NO_CONNECTIONS = "<p>No connections</p>";

	private static final String templateLocation = "/pt/com/broker/http/subscriptions.template";
	private static final String cssLocation = "/pt/com/broker/http/style.css";

	private static String template = null;
	private static String cssTemplate = null;

	static
	{
		try
		{
			template = IOUtils.toString(MiscInfoAction.class.getResourceAsStream(templateLocation));
			cssTemplate = IOUtils.toString(MiscInfoAction.class.getResourceAsStream(cssLocation));
		}
		catch (IOException e)
		{
			Shutdown.now(e);
		}
	}

	public SubscriptionsAction()
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
			String agentName = GcsInfo.constructAgentName(GcsInfo.getAgentHost(), GcsInfo.getAgentPort());

			if (template != null)
			{
				ConnectionInfo inboundConnections = getInboundConnections();
				ConnectionInfo outboundConnections = getOutboundConnections();

				String smessage = String.format(template, getCss(), agentName, getLocalQueueConsumers(), getRemoteQueueConsumers(), getLocalTopicConsumers(), getRemoteTopicConsumers(), inboundConnections.size, inboundConnections.display, outboundConnections.size, outboundConnections.display);
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

	private String getLocalTopicConsumers()
	{
		StringBuilder sb = new StringBuilder();

		for (TopicProcessor tp : TopicProcessorList.values())
		{
			ArrayList<String> clients = new ArrayList<String>();

			for (MessageListener l : tp.listeners())
			{
				if (l.getType() == MessageListener.Type.LOCAL)
				{
					clients.add(l.getChannel().getRemoteAddressAsString());
				}
				else if (l.getType() == MessageListener.Type.INTERNAL)
				{
					clients.add(l.toString());
				}
			}

			if (clients.size() > 0)
			{
				sb.append(generateHtml(tp.getSubscriptionName(), clients));
			}
		}

		return (sb.length() != 0) ? sb.toString() : NO_SUBSCRIPTIONS;
	}

	private String getRemoteTopicConsumers()
	{
		StringBuilder sb = new StringBuilder();

		for (TopicProcessor tp : TopicProcessorList.values())
		{
			ArrayList<String> clients = new ArrayList<String>();

			for (MessageListener l : tp.listeners())
			{
				if (l.getType() == MessageListener.Type.REMOTE)
				{
					clients.add(l.getChannel().getRemoteAddressAsString());
				}
			}

			if (clients.size() > 0)
			{
				sb.append(generateHtml(tp.getSubscriptionName(), clients));
			}
		}

		return (sb.length() != 0) ? sb.toString() : NO_SUBSCRIPTIONS;
	}

	private String getLocalQueueConsumers()
	{
		StringBuilder sb = new StringBuilder();

		for (QueueProcessor p : QueueProcessorList.values())
		{
			ArrayList<String> clients = new ArrayList<String>();

			if (p.localListeners().size() > 0)
			{
				for (MessageListener l : p.localListeners())
				{
					clients.add(l.getChannel().getRemoteAddressAsString());
				}

				sb.append(generateHtml(p.getQueueName(), clients));
			}
		}

		return (sb.length() != 0) ? sb.toString() : NO_SUBSCRIPTIONS;
	}

	private String getRemoteQueueConsumers()
	{
		StringBuilder sb = new StringBuilder();

		for (QueueProcessor p : QueueProcessorList.values())
		{
			ArrayList<String> clients = new ArrayList<String>();

			if (p.remoteListeners().size() > 0)
			{
				for (MessageListener l : p.remoteListeners())
				{
					clients.add(l.getChannel().getRemoteAddressAsString());
				}

				sb.append(generateHtml(p.getQueueName(), clients));
			}
		}

		return (sb.length() != 0) ? sb.toString() : NO_SUBSCRIPTIONS;
	}

	private static class ConnectionInfo
	{
		public final int size;
		public final String display;

		ConnectionInfo(int size, String display)
		{
			this.size = size;
			this.display = display;
		}
	}

	private ConnectionInfo getInboundConnections()
	{
		StringBuilder sb = new StringBuilder();

		class ConInfo
		{
			public final String agent;
			public final Channel channel;

			ConInfo(String agent, Channel channel)
			{
				this.agent = agent;
				this.channel = channel;
			}
		}
		;

		Map<String, ChannelHandlerContext> allRemoteAgents = InboundRemoteChannels.getAll();
		List<ConInfo> connections = new LinkedList<ConInfo>();

		for (String agent : allRemoteAgents.keySet())
		{
			connections.add(new ConInfo(agent, allRemoteAgents.get(agent).getChannel()));
		}

		Collections.sort(connections, new Comparator<ConInfo>()
		{
			public int compare(ConInfo o1, ConInfo o2)
			{
				return o1.agent.compareTo(o2.agent);
			};
		});

		for (ConInfo conInfo : connections)
		{
			sb.append("<p><b>");
			sb.append(conInfo.agent);
			sb.append("</b>: ");
			sb.append(conInfo.channel);
			sb.append("</p>");
		}

		String display = (sb.length() != 0) ? sb.toString() : NO_CONNECTIONS;

		return new ConnectionInfo(connections.size(), display);
	}

	private ConnectionInfo getOutboundConnections()
	{
		StringBuilder sb = new StringBuilder();

		List<Channel> sessions = new LinkedList<Channel>(Gcs.getManagedConnectorSessions());

		Collections.sort(sessions, new Comparator<Channel>()
		{
			public int compare(Channel c1, Channel c2)
			{
				InetSocketAddress c1RemoteAddress = (InetSocketAddress) c1.getRemoteAddress();
				InetSocketAddress c2RemoteAddress = (InetSocketAddress) c2.getRemoteAddress();

				return Arrays.toString(c2RemoteAddress.getAddress().getAddress()).compareTo(Arrays.toString(c1RemoteAddress.getAddress().getAddress()));
			};
		});

		for (Channel channel : sessions)
		{
			sb.append("<p>");
			sb.append(channel);
			sb.append("</p>");
		}

		String display = (sb.length() != 0) ? sb.toString() : NO_CONNECTIONS;

		return new ConnectionInfo(sessions.size(), display);
	}

	private static String generateHtml(String title, Collection<String> elements)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<h3>");
		sb.append(title);
		sb.append(" (");
		sb.append(elements.size());
		sb.append(")");
		sb.append("</h3>");

		for (String element : elements)
		{
			sb.append("<p>");
			sb.append(element);
			sb.append("</p>");
		}

		return sb.toString();
	}

	public static String getCss()
	{
		return cssTemplate;
	}
}