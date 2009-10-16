package pt.com.broker.monitorization.collectors;

import java.util.regex.Pattern;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.types.NetNotification;

public class DropboxCollector extends Collector<DropboxListener>
{
	// Destination sample: /system/stats/dropbox/#127.0.0.1#
	// Payload sample: 127.0.0.1#./dropbox#2#1

	private static final String agentNameSizeRegEx = "#";
	Pattern agentNameSizePattern;

	private static final String SUBSCRIPTION = "/system/stats/dropbox/.*"; // TODO: optimize this

	public DropboxCollector(BaseBrokerClient agent) throws Throwable
	{
		super("Dropbox Information Collector", SUBSCRIPTION, agent);
		agentNameSizePattern = Pattern.compile(agentNameSizeRegEx);
	}

	@Override
	protected void messageReceived(NetNotification notification)
	{
		String agent = null;
		String droboxLocation = null;
		int messages = 0;
		int goodMessages = 0;

		String[] tokens = agentNameSizePattern.split(new String(notification.getMessage().getPayload()));

		agent = tokens[0];
		droboxLocation = tokens[1];
		try
		{
			messages = Integer.parseInt(tokens[2]);
			goodMessages = Integer.parseInt(tokens[3]);
		} catch (Throwable t)
		{
			return;
		}
		
		synchronized (listeners)
		{
			for (DropboxListener handler : listeners)
			{
				try
				{
					handler.onUpdate(agent, droboxLocation, messages, goodMessages);
				}
				catch (Exception e)
				{
					// TODO: log exception
				}
			}
		}
	}
}
