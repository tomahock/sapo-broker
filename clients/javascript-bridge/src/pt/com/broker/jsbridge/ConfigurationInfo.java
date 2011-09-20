package pt.com.broker.jsbridge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.jsbridge.configuration.BridgeConfiguration;

public class ConfigurationInfo
{
	private static final Logger log = LoggerFactory.getLogger(ConfigurationInfo.class);

	private static final ConfigurationInfo instance = new ConfigurationInfo();

	private BridgeConfiguration configuration;
	private ConcurrentMap<String, BridgeChannel> channelMappings = new ConcurrentHashMap<String, BridgeChannel>();
	private ConcurrentMap<String, BridgeChannel> topicMappings = new ConcurrentHashMap<String, BridgeChannel>();
	private List<BridgeChannel> bridgeChanels = new ArrayList<BridgeChannel>();

	private ConfigurationInfo()
	{
		JAXBContext jc;
		Unmarshaller u = null;
		try
		{
			jc = JAXBContext.newInstance("pt.com.broker.jsbridge.configuration");
			u = jc.createUnmarshaller();

			// configuration = (BridgeConfiguration) u.unmarshal(ConfigurationInfo.class.getResourceAsStream("configuration.xml"));

			File f = new File("./conf/configuration.xml");
			configuration = (BridgeConfiguration) u.unmarshal(f);

			buildMappings();
		}
		catch (Throwable e)
		{
			configuration = null;
			log.error("Configuration initialization failed.", e);
		}
	}

	private void buildMappings()
	{
		List<BridgeConfiguration.Channels.Channel> channels = configuration.getChannels().getChannel();

		for (BridgeConfiguration.Channels.Channel channel : channels)
		{
			String name = channel.getName();
			String topic = channel.getTopic();
			boolean allowSubscription = channel.isAllowSubscription();
			boolean allowPublication = channel.isAllowPublication();
			String brokerHost = StringUtils.isBlank(channel.getBrokerHost()) ? getDefaultBrokerHost() : channel.getBrokerHost();
			int brokerPort = channel.getBrokerPort() == null ? getDefaultBrokerPort() : channel.getBrokerPort().intValue();
			MessageTransformer downstreamFilter = null;
			MessageTransformer upstreamFilter = null;
			try
			{
				downstreamFilter = StringUtils.isBlank(channel.getDownstreamFilter()) ? null : (MessageTransformer) Class.forName(channel.getDownstreamFilter()).newInstance();
				upstreamFilter = StringUtils.isBlank(channel.getUpstreamFilter()) ? null : (MessageTransformer) Class.forName(channel.getUpstreamFilter()).newInstance();
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);
			}

			BridgeChannel bc = new BridgeChannel(name, topic, allowSubscription, allowPublication, brokerHost, brokerPort, downstreamFilter, upstreamFilter);

			bridgeChanels.add(bc);
			channelMappings.put(name, bc);
			topicMappings.put(topic, bc);
		}
	}

	public static BridgeChannel getBridgeChannelByName(String channelName)
	{
		return instance.channelMappings.get(channelName);
	}

	public static int getPort()
	{
		return instance.configuration.getSettings().getBridge().getPort();
	}

	public static String getRootDirectory()
	{
		return instance.configuration.getSettings().getBridge().getRootDir();
	}

	public static String getDefaultBrokerHost()
	{
		return instance.configuration.getSettings().getDefaultBroker().getHost();
	}

	public static int getDefaultBrokerPort()
	{
		return instance.configuration.getSettings().getDefaultBroker().getPort();
	}

	public static void init()
	{
		log.info("ConfigurationInfo initialized");
	}

	public static List<BridgeChannel> getBridgeChannels()
	{
		return instance.bridgeChanels;
	}

}
