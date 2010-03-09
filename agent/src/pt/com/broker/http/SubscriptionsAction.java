package pt.com.broker.http;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.caudexorigo.http.netty.HttpAction;
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

import pt.com.broker.messaging.QueueSessionListener;
import pt.com.broker.messaging.QueueSessionListenerList;
import pt.com.broker.messaging.TopicSubscriber;
import pt.com.broker.messaging.TopicSubscriberList;
import pt.com.broker.messaging.TopicSubscriber.ChannelInfo;
import pt.com.broker.messaging.TopicSubscriberList.MaximumDistinctSubscriptionsReachedException;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.RemoteQueueConsumers;
import pt.com.gcs.messaging.RemoteTopicConsumers;

/**
 * StatusAction outputs agent status in XML.
 * 
 */

public class SubscriptionsAction extends HttpAction
{
	private static final Logger log = LoggerFactory.getLogger(SubscriptionsAction.class);

	private static final String NO_SUBSCRIPTIONS = "<p>No subscriptions</p>";
	
	private static final String template = "<html><head><title>Sapo-Broker Subscription Information</title></head><body>" +
			"<h1>Agent name: %s</h1>" +
			"<h2>Local topic subscriptions</h2>%s" +
			"<h2>Local queue subscriptions</h2>%s" +
			"<h2>Remote topic subscriptions</h2>%s" +
			"<h2>Remote queue subscriptions</h2>%s" +
			"</body></html>";

	public SubscriptionsAction()
	{
	}

	@Override
	public void writeResponse(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response)
	{
		ChannelBuffer bbo = ChannelBuffers.dynamicBuffer();
		OutputStream out = new ChannelBufferOutputStream(bbo);
		Channel channel = ctx.getChannel();

		try
		{
			String agentName = GcsInfo.constructAgentName(GcsInfo.getAgentHost(), GcsInfo.getAgentPort());
			
			String smessage = String.format(template,agentName,
					getLocalTopicConsumers(),
					getLocalQueueConsumers(),
					getRemoteTopicConsumers(),
					getRemoteQueueConsumers());
			byte[] bmessage = smessage.getBytes("UTF-8");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");

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
	
	
	
	private String getLocalTopicConsumers()
	{
		StringBuilder sb = new StringBuilder();
		
		for(String topicSubscription : TopicSubscriberList.getTopicSubscriptionNames() )
		{
			TopicSubscriber topicSubscriber;
			try
			{
				topicSubscriber = TopicSubscriberList.get(topicSubscription);
			}
			catch (MaximumDistinctSubscriptionsReachedException e)
			{
				continue;
			}
			Set<ChannelInfo> sessions = topicSubscriber.getSessions();
			
			ArrayList<String> clients = new ArrayList<String>(sessions.size());
			
			for(ChannelInfo client : sessions)
			{
				clients.add(client.channel.getRemoteAddress().toString());
			}
			
			sb.append(generateHtml(topicSubscription, clients));
		}
		
		return (sb.length() != 0 ) ? sb.toString() : NO_SUBSCRIPTIONS;
	}
	
	private String getRemoteTopicConsumers()
	{
		StringBuilder sb = new StringBuilder();
		
		for(String topicSubscription : RemoteTopicConsumers.getSubscriptionNames() )
		{
			CopyOnWriteArrayList<Channel> subscriptions;
			subscriptions = RemoteTopicConsumers.getSubscription(topicSubscription);
						
			ArrayList<String> clients = new ArrayList<String>(subscriptions.size());
			
			for(Channel client : subscriptions)
			{
				clients.add(client.getRemoteAddress().toString());
			}
			
			sb.append(generateHtml(topicSubscription, clients));
		}
		
		return (sb.length() != 0 ) ? sb.toString() : NO_SUBSCRIPTIONS;
	}
	
	private String getLocalQueueConsumers()
	{
		StringBuilder sb = new StringBuilder();
		
		for(String queueName : QueueSessionListenerList.getQueueNames() )
		{
			QueueSessionListener queueSessionListener = QueueSessionListenerList.get(queueName);
			
			List<pt.com.broker.messaging.QueueSessionListener.ChannelInfo> sessions = queueSessionListener.getSessions();
			
			ArrayList<String> clients = new ArrayList<String>(sessions.size());
			
			for(pt.com.broker.messaging.QueueSessionListener.ChannelInfo client : sessions)
			{
				clients.add(client.channel.getRemoteAddress().toString());
			}
			
			sb.append(generateHtml(queueName, clients));
		}
		
		return (sb.length() != 0 ) ? sb.toString() : NO_SUBSCRIPTIONS;
	}
	
	private String getRemoteQueueConsumers()
	{
		StringBuilder sb = new StringBuilder();
		
		for(String queueName : RemoteQueueConsumers.getQueueNames() )
		{
			CopyOnWriteArrayList<pt.com.gcs.messaging.RemoteQueueConsumers.ChannelInfo> subscriptions;
			subscriptions = RemoteQueueConsumers.getSessions(queueName);
						
			ArrayList<String> clients = new ArrayList<String>(subscriptions.size());
			
			for(pt.com.gcs.messaging.RemoteQueueConsumers.ChannelInfo client : subscriptions)
			{
				clients.add(client.channel.getRemoteAddress().toString());
			}
			
			sb.append(generateHtml(queueName, clients));
		}
		
		return (sb.length() != 0 ) ? sb.toString() : NO_SUBSCRIPTIONS;
	}
	
	private static String generateHtml(String title, Collection<String> elements)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<h3>");
		sb.append(title);
		sb.append(" (");
		sb.append(elements.size());
		sb.append(")");
		sb.append("</h3>");
		
		for(String element : elements)
		{
			sb.append("<p>");
			sb.append(element);
			sb.append("</p>");
		}
		
		return sb.toString();
	}
	
}
