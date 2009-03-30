package pt.com.gcs.conf;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.caudexorigo.Shutdown;
import org.caudexorigo.io.FilenameUtils;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GcsInfo
{
	private static Logger log = LoggerFactory.getLogger(GcsInfo.class);

	public static final String VERSION = "@gcsVersion@";

	private static final GcsInfo instance = new GcsInfo();

	public static String getAgentHost()
	{
		String prop = instance.conf.getNet().getIp();
		if (StringUtils.isBlank(prop))
		{
			log.error("Fatal error: Must define valid host.");
			Shutdown.now();
		}
		return prop;
	}

	public static String getAgentName()
	{
		String prop = instance.conf.getName();
		if (StringUtils.isBlank(prop))
		{
			log.error("Fatal error: Must define an Agent name.");
			Shutdown.now();
		}
		return prop;
	}

	public static int getAgentPort()
	{
		int iprop = instance.conf.getNet().getPort();
		return iprop;
	}

	public static String getBasePersistentDirectory()
	{
		String prop = instance.conf.getPersistency().getDirectory();
		String defaultDir = FilenameUtils.normalizeNoEndSeparator(System.getProperty("user.dir")) + File.separator + "persistent";
		if (StringUtils.isBlank(prop))
		{
			log.warn("No directory for persistent storage. Using default: {}", defaultDir);
			return defaultDir;
		}
		else
		{
			return FilenameUtils.normalizeNoEndSeparator(prop);
		}
	}

	public static int getInitialDelay()
	{
		int iprop = instance.conf.getNet().getDiscoveryDelay();
		return iprop;
	}

	public static String getStatisticsTopic()
	{
		String prop = instance.conf.getStatistics().getTopic();
		return prop;
	}

	public static String getConfigVersion()
	{
		String prop = instance.conf.getConfigVersion();
		return prop;
	}

	public static String getWorldMapPath()
	{
		String prop = instance.conf.getNet().getFileRef();
		if (StringUtils.isBlank(prop))
		{
			log.error("Fatal error: Must define a valid path for the world map file.");
			Shutdown.now();
		}
		return prop;
	}

	private Config conf;

	private GcsInfo()
	{
		String filePath = System.getProperty("config-path");
		if (StringUtils.isBlank(filePath))
		{
			log.error("Fatal error: No configuration file defined. Please set the enviroment variable 'config-path' to valid path for the configuration file");
			Shutdown.now();
		}
		try
		{
			JAXBContext jc = JAXBContext.newInstance("pt.com.gcs.conf");
			Unmarshaller u = jc.createUnmarshaller();

			File f = new File(filePath);
			boolean b = f.exists();
			conf = (Config) u.unmarshal(f);
		}
		catch (JAXBException e)
		{
			// Throwable t = ErrorAnalyser.findRootCause(e);
			log.error("Fatal error: {}", e.getMessage());
			Shutdown.now(e);
		}
	}

	public static int getBrokerUdpPort()
	{
		int iprop = instance.conf.getNet().getBrokerUdpPort();
		return iprop;
	}

	public static int getBrokerHttpPort()
	{
		int iprop = instance.conf.getNet().getBrokerHttpPort();
		return iprop;
	}

	public static int getBrokerPort()
	{
		int iprop = instance.conf.getNet().getBrokerPort();
		return iprop;
	}

	public static int getBrokerSSLPort()
	{
		int sslPort = instance.conf.getNet().getBrokerSslPort();
		return sslPort;
	}

	public static int getBrokerLegacyPort()
	{
		int iprop = instance.conf.getNet().getBrokerLegacyPort();
		return iprop;
	}

	public static boolean isDropboxEnabled()
	{
		return instance.conf.getMessaging().getDropbox().isEnabled();
	}

	public static String getDropBoxDir()
	{
		return instance.conf.getMessaging().getDropbox().getDir();
	}

	public static int getDropBoxCheckInterval()
	{
		return instance.conf.getMessaging().getDropbox().getCheckInterval();
	}

	public static BrokerSecurityPolicy getSecurityPolicy()
	{
		return instance.conf.getSecurityPolicies();
	}
	
	// SSL related properties
	
	public static String getKeystoreLocation()
	{
		if( instance.conf.ssl != null)
			return instance.conf.ssl.getKeystoreLocation();
		return null;
	}

	public static String getKeystorePassword()
	{
		if( instance.conf.ssl != null)
			return instance.conf.ssl.getKeystorePassword();
		return null;
	}
	
	public static String getKeyPassword()
	{
		if( instance.conf.ssl != null)
			return instance.conf.ssl.getKeyPassword();
		return null;
	}
	
	// STS related properties
	
	public static String getSTSLocation()
	{
		if( instance.conf.sts != null)
			return instance.conf.sts.getSTSLocation();
		return null;
	}
	public static String getSTSUsername()
	{
		if( instance.conf.sts != null)
			return instance.conf.sts.getSTSUsername();
		return null;
	}
	public static String getSTSPassword()
	{
		if( instance.conf.sts != null)
			return instance.conf.sts.getSTSPassword();
		return null;
	}
	
}
