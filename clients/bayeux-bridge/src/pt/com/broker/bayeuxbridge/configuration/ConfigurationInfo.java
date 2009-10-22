package pt.com.broker.bayeuxbridge.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import pt.com.broker.bayeuxbridge.configuration.Mappings.MappingSet;

public class ConfigurationInfo
{
	private static final Logger log = LoggerFactory.getLogger(ConfigurationInfo.class);
	
	private static BridgeConfiguration configuration;
	private static Map<String, List<Mappings.MappingSet.Mapping> > mappingSet = new HashMap<String, List<Mappings.MappingSet.Mapping> >();
	
	private static List<String> publicationChannels = new ArrayList<String>();
	private static List<String> subscriptionChannels = new ArrayList<String>();
	
	static{
		JAXBContext jc;
		Unmarshaller u = null;
		try
		{
			jc = JAXBContext.newInstance("pt.com.broker.bayeuxbridge.configuration");
			u = jc.createUnmarshaller();
			String filename = "./conf/configuration.xml";
			File f = new File(filename);
			boolean b = f.exists();
			if(!b)
			{
				log.error("Configuration file (" + filename +  ") was not found.");
			}
			configuration = (BridgeConfiguration) u.unmarshal(f);
		}
		catch (Throwable e)
		{
			configuration = null;
			log.error("Configuration initialization failed.", e);
		}
	}

	public static BridgeConfiguration getConfiguration()
	{
		return configuration;
	}

	public static void init()
	{
		buildMappings();
	}
	
	public static List<Mappings.MappingSet.Mapping> getMappingSet(String name)
	{
		return mappingSet.get(name);
	}

	private static void buildMappings()
	{
		Map<String, String> deferedInclusion = new HashMap<String, String>(getConfiguration().getMappings().getMappingSet().size());
				
		for(MappingSet mappSet : getConfiguration().getMappings().getMappingSet() )
		{
			mappingSet.put(mappSet.getSetName(), mappSet.getMapping());
			if(mappSet.getIncludeSet() != null)
			{
				deferedInclusion.put(mappSet.getSetName(), mappSet.getIncludeSet());
			}
		}
		
		for(String mappingName : deferedInclusion.keySet())
		{
			if( ! mappingSet.containsKey(deferedInclusion.get(mappingName) ) )
			{
				log.error("Mapping-set {} is supost to include mapping-set, but this one is missing.", mappingName, deferedInclusion.get(mappingName) );
				return;
			}
			List<Mappings.MappingSet.Mapping> mappings = mappingSet.get( mappingName );
			for(Mappings.MappingSet.Mapping mapping : mappingSet.get(deferedInclusion.get(mappingName) ))
			{
				mappings.add(mapping);
			}
		}
	}
	
	public static void addPublicationChannel(String channel)
	{
		if(publicationChannels.contains(channel) )
			publicationChannels.add(channel);
	}
	
	public static List<String> getPublicationChannels()
	{
		return publicationChannels;
	}
	
	public static void addSubscriptionChannel(String channel)
	{
		if(subscriptionChannels.contains(channel) )
			subscriptionChannels.add(channel);
	}
	
	public static List<String> getSubscriptionChannels()
	{
		return subscriptionChannels;
	}
}
