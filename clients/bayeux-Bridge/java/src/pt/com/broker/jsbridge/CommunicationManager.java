package pt.com.broker.jsbridge;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.HostInfo;
import pt.com.broker.jsbridge.configuration.Clouds;
import pt.com.broker.jsbridge.configuration.ConfigurationInfo;
import pt.com.broker.jsbridge.configuration.Clouds.Cloud;

public class CommunicationManager
{
	private static final Logger log = LoggerFactory.getLogger(CommunicationManager.class);
	
	
	private static List<CloudCommunication> clouds;
	
	public static synchronized boolean init()
	{
		for(Clouds _clouds : ConfigurationInfo.getConfiguration().getBrokerClouds() )
		{
			if(clouds == null)
				clouds = new ArrayList<CloudCommunication>(_clouds.getCloud().size());
			for(Cloud cloud : _clouds.getCloud())
			{
				List<HostInfo> hostInfo = new ArrayList<HostInfo>(cloud.getAgent().size());
				
				String cloudName = cloud.getCloudName();
				String mapping = cloud.getMapping();
				
				for(pt.com.broker.jsbridge.configuration.Clouds.Cloud.Agent agent : cloud.getAgent())
				{
					hostInfo.add(new HostInfo(agent.getHostname(), agent.getPort().intValue() ));
				}
				CloudCommunication cloudCommunicationManager = new CloudCommunication(hostInfo, cloudName, mapping);
				if( cloudCommunicationManager.init() )
					clouds.add(cloudCommunicationManager);
				else
					log.error("CloudCommunicationManager initialization failed. Cloud name: " + cloudCommunicationManager.getCloudName());
			}
		}
		return true;
	}
	
	public static boolean publish(String channel, String message)
	{
		boolean sucess = false; // it succeeds if at least one succeeds 
		for(CloudCommunication cloud : clouds)
		{
			if( cloud.publish(channel, message) )
				sucess = true;
		}
		return sucess;
	}
	
	public static boolean registerChannel(String channel, String clientId)
	{
		boolean sucess = false; // it succeeds if at least one succeeds 
		for(CloudCommunication cloud : clouds)
		{
			if( cloud.registerChannel(channel, clientId) )
				sucess = true;
		}
		return sucess;
	}
	
	public static void unregisterChannel(String channel, String clientId)
	{
		for(CloudCommunication cloud : clouds)
		{
			cloud.unregisterChannel(channel, clientId);
		}
	}
	
	public static void unregisterClient(String clientId)
	{
		for(CloudCommunication cloud : clouds)
		{
			cloud.unregisterClient(clientId);
		}
	}
}
