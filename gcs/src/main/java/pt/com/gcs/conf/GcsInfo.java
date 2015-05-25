package pt.com.gcs.conf;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.caudexorigo.Shutdown;
import org.caudexorigo.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.ProviderInfo;
import pt.com.gcs.conf.agent.AgentConfig;
import pt.com.gcs.conf.agent.AgentConfig.Ssl;
import pt.com.gcs.conf.global.BrokerSecurityPolicy;

/**
 * GcsInfo contains several information about the agent.
 * 
 */
public class GcsInfo
{

	private final static Logger log = LoggerFactory.getLogger(GcsInfo.class);

	public static final int UNLIMITED_QUEUES_VALUE = -1;

	private static final GcsInfo instance = new GcsInfo();

	// public static final String VERSION = "3.0";

	public static String constructAgentName(String ip, int port)
	{
		return ip + ":" + port;
	}

	/**
	 * Should the SSL port be open? Determined by the existence of the respective configuration element.
	 * 
	 * @return <code>true</code> if SSL is to be used <code>false</code> otherwise
	 */
	public static boolean createSSLInterface()
	{
		return instance.conf.getSsl() != null;
	}

	/**
	 * Agent's IP
	 * 
	 * @return A String with agent's IP.
	 */
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

	/**
	 * Agent's name
	 * 
	 * @return Agent's name.
	 */
	public static String getAgentName()
	{
		return instance.agentName;
	}

	/**
	 * Agent's TCP port used for inter-agent communication.
	 * 
	 * @return TCP port
	 */
	public static int getAgentPort()
	{
		int iprop = instance.conf.getNet().getPort();
		return iprop;
	}

	/**
	 * Base directory to store messages persistently.
	 * 
	 * @return Base directory location
	 */
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

	/**
	 * HTTP port.
	 * 
	 * @return HTTP port
	 */
	public static int getBrokerHttpPort()
	{
		int iprop = instance.conf.getNet().getBrokerHttpPort();
		return iprop;
	}

	/**
	 * HTTP port.
	 *
	 * @return HTTP port
	 */
	public static int getBrokerWsPort()
	{
		int iprop = instance.conf.getNet().getBrokerWsPort();
		return iprop;
	}

	/**
	 * Broker legacy TCP port (used by clients). Used for backward compatibility.
	 * 
	 * @return A TCP port
	 */
	public static int getBrokerLegacyPort()
	{
		int iprop = instance.conf.getNet().getBrokerLegacyPort();
		return iprop;
	}

	/**
	 * Broker TCP port (used by clients).
	 * 
	 * @return A TCP port
	 */
	public static int getBrokerPort()
	{
		int iprop = instance.conf.getNet().getBrokerPort();
		return iprop;
	}

	/**
	 * SSL Port
	 * 
	 * @return A TCP port
	 */
	public static int getBrokerSSLPort()
	{
		Ssl ssl = instance.conf.getSsl();
		if (ssl == null)
		{
			return -1;
		}

		int sslPort = ssl.getBrokerSslPort();
		return sslPort;
	}

	/**
	 * UDP port.
	 * 
	 * @return UDP port
	 */
	public static int getBrokerUdpPort()
	{
		int iprop = instance.conf.getNet().getBrokerUdpPort();
		return iprop;
	}

	/**
	 * Configuration version.
	 * 
	 * @return Configuration version
	 */
	public static String getConfigVersion()
	{
		String prop = instance.conf.getConfigVersion();
		return prop;
	}

	/**
	 * Credential validator providers.
	 * 
	 * @return A map with credential validator providers
	 */
	public static Map<String, ProviderInfo> getCredentialValidatorProviders()
	{
		return GlobalConfig.getCredentialValidatorProviders();
	}

	/**
	 * Interval to check for new messages.
	 * 
	 * @return An interval in seconds
	 */
	public static int getDropBoxCheckInterval()
	{
		return instance.conf.getMessaging().getDropbox().getCheckInterval();
	}

	/**
	 * Dropbox directory.
	 * 
	 * @return Dropbox directory
	 */
	public static String getDropBoxDir()
	{
		return instance.conf.getMessaging().getDropbox().getDir();
	}

	/**
	 * Global configuration file location.
	 * 
	 * @return Global configuration file location
	 */
	public static String getGlobalConfigFilePath()
	{
		String prop = System.getProperty("broker-global-config-path");
		if (StringUtils.isBlank(prop))
		{
			log.error("Fatal error: No global configuration file defined. Please set the enviroment variable 'broker-global-config-path' to valid path for the global configuration file");
			Shutdown.now();
		}
		return prop;
	}

	// Access Control related methods

	/**
	 * A delay time (in milliseconds) that allows broker peers to detect new producers and make sure that they are ready to receive messages. Valid values: Positive integer Default value: 100
	 * 
	 * @return A delay time in milliseconds
	 */
	public static int getInitialDelay()
	{
		int iprop = instance.conf.getNet().getDiscoveryDelay();
		return iprop;
	}

	/**
	 * Private key password.
	 * 
	 * @return Private key password
	 */
	public static String getKeyPassword()
	{
		if (instance.conf.getSsl() != null)
			return instance.conf.getSsl().getKeyPassword();
		return null;
	}

	// SSL related methods

	/**
	 * Keystore containing agent's key pair.
	 * 
	 * @return KeyStore file location
	 */
	public static String getKeystoreLocation()
	{
		if (instance.conf.getSsl() != null)
		{
			return instance.conf.getSsl().getKeystoreLocation();
		}
		return null;
	}

	/**
	 * Keystore password.
	 * 
	 * @return Keystore password
	 */
	public static String getKeystorePassword()
	{
		if (instance.conf.getSsl() != null)
			return instance.conf.getSsl().getKeystorePassword();
		return null;
	}

	/**
	 * Maximum distinct subscriptions.
	 * 
	 * @return Maximum distinct subscriptions
	 */
	public static int getMaxDistinctSubscriptions()
	{
		return GlobalConfig.getMaxDistinctSubscriptions();
	}

	/**
	 * Maximum number of queues.
	 * 
	 * @return Maximum number of queues
	 */
	public static int getMaxQueues()
	{
		return GlobalConfig.getMaxQueues();
	}

	public static long getQueueMaxStaleAge()
	{
		return GlobalConfig.getQueueMaxStaleAge();
	}

	// Message related properties
	/**
	 * Maximum message size.
	 * 
	 * @return Maximum number of bytes per message
	 */
	public static int getMessageMaxSize()
	{
		return GlobalConfig.getMsgMaxSize();
	}

	// Authentication validators

	/**
	 * Time during witch a message is stored.
	 * 
	 * @return Time in milliseconds
	 */
	public static long getMessageStorageTime()
	{
		return GlobalConfig.getMaxStoreTime();
	}

	/**
	 * Security policies.
	 * 
	 * @return Security policies
	 */
	public static BrokerSecurityPolicy getSecurityPolicies()
	{
		return GlobalConfig.getSecurityPolicies();
	}

	/**
	 * Is Dropbox enabled.
	 * 
	 * @return <code>true</code> if it is enabled <code>false</code> otherwise
	 */
	public static boolean isDropboxEnabled()
	{
		return instance.conf.getMessaging().getDropbox().isEnabled();
	}

	/**
	 * Should access control be used? Determined by the existence of security policies.
	 * 
	 * @return <code>true</code> if access control is to be used <code>false</code> otherwise
	 */
	public static boolean useAccessControl()
	{
		return getSecurityPolicies() != null;
	}

	/**
	 * Is agent communications forced to TLS.
	 * 
	 * @return <code>true</code> if it is enabled <code>false</code> otherwise
	 * */
	public static boolean isForceAgentSsl()
	{
		return instance.conf.getSsl().getForceAgentSsl();
	}

	public static String getSslProtocolWhiteList()
	{
		return instance.conf.getSsl().getSslProtocolWhiteList();
	}

	public static String getSslCipherSuiteWhitelist()
	{
		return instance.conf.getSsl().getSslCipherSuiteWhiteList();
	}

	/**
	 * Gets the path to the certificate file used for SSL comunications.
	 * 
	 * @return The path to the certificate file.
	 * */
	public static String getCertificateFile()
	{
		return instance.conf.getSsl().getCertFile();
	}

	/**
	 * Gets the path to the private key file used for SSL comunications.
	 * 
	 * @return The path to the private key file.
	 * */
	public static String getKeyFile()
	{
		return instance.conf.getSsl().getKeyFile();
	}

	private AgentConfig conf;

	private String agentName;

	private GcsInfo()
	{
		String filePath = System.getProperty("agent-config-path");
		if (StringUtils.isBlank(filePath))
		{
			log.error("Fatal error: No agent configuration file defined. Please set the enviroment variable 'agent-config-path' to valid path for the configuration file");
			Shutdown.now();
		}
		try
		{
			JAXBContext jc = JAXBContext.newInstance("pt.com.gcs.conf.agent");
			Unmarshaller u = jc.createUnmarshaller();

			log.debug("******************Current path: {}***********************", Paths.get(".").toAbsolutePath().normalize().toString());

			URL fileUrl = getClass().getClassLoader().getResource(filePath);
			File f = null;
			if (fileUrl != null)
			{
				f = new File(fileUrl.toURI());
			}
			else
			{
				f = new File(filePath);
				if (!f.exists())
				{
					f = new File(Paths.get(".").resolve(filePath).toAbsolutePath().normalize().toString());
				}
			}

			boolean b = f.exists();
			if (!b)
			{
				log.error("Agent configuration file missing - " + filePath);
				Shutdown.now();
			}
			conf = (AgentConfig) u.unmarshal(f);

			String prop = constructAgentName(conf.getNet().getIp(), conf.getNet().getPort());
			if (StringUtils.isBlank(prop))
			{
				log.error("Fatal error: Must define an Agent name.");
				Shutdown.now();
			}
			agentName = prop;
		}
		catch (JAXBException | URISyntaxException e)
		{
			// Throwable t = ErrorAnalyser.findRootCause(e);
			log.error("Fatal error: {}", e.getMessage());
			Shutdown.now(e);
		}
	}
}
