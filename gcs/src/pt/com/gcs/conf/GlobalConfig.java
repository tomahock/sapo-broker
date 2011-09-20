package pt.com.gcs.conf;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.caudexorigo.Shutdown;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import pt.com.broker.auth.ProviderInfo;
import pt.com.gcs.conf.global.BrokerSecurityPolicy;
import pt.com.gcs.net.Peer;

public class GlobalConfig
{
	private static final Logger log = LoggerFactory.getLogger(GlobalConfig.class);

	private final List<Peer> peerList = new ArrayList<Peer>();

	private final Set<InetSocketAddress> peerSet = Collections.newSetFromMap(new ConcurrentHashMap<InetSocketAddress, Boolean>());

	private static final GlobalConfig instance = new GlobalConfig();

	private AtomicLong last_modified = new AtomicLong(0L);

	private BrokerSecurityPolicy secPolicy;

	private Map<String, ProviderInfo> credentialValidatiorProviders = new TreeMap<String, ProviderInfo>();

	// Messages info
	private int msgMaxSize;
	private int maxQueues;
	private long maxStoreTime;
	private int maxDistinctSubscriptions;
	private boolean preferLocalConsumers = true;
	private boolean supportVirtualQueues = true;

	private GlobalConfig()
	{
		String globalConfigPath = GcsInfo.getGlobalConfigFilePath();
		Source schemaLocation = new StreamSource(GlobalConfig.class.getResourceAsStream("/pt/com/gcs/etc/global_config.xsd"));
		File xmlFile = new File(globalConfigPath);

		XsdValidationResult result = SchemaValidator.validate(schemaLocation, xmlFile);

		if (!result.isValid())
		{
			log.error("Invalid world map, aborting startup.");
			log.error(result.getMessage());
			Shutdown.now();
		}
		Document doc = parseXmlFile(globalConfigPath, false);

		init(doc);
	}

	private void init(Document doc)
	{
		populateWorldMap(doc);
		extractSecurityPolicies(doc);
		extractMessageConfiguration(doc);
		synchronized (credentialValidatiorProviders)
		{
			credentialValidatiorProviders.clear();
			loadCredentialValidatiorProviders(doc);
		}
	}

	private void extractMessageConfiguration(Document doc)
	{
		String maxSizeStr = extractElementInfo(doc, "max-msg-size")[0];
		msgMaxSize = Integer.parseInt(maxSizeStr);

		String maxQueueSize = extractElementInfo(doc, "max-queues")[0];
		maxQueues = Integer.parseInt(maxQueueSize);

		String[] plcElementInfo = extractElementInfo(doc, "prefer-local-consumers");
		if ((plcElementInfo != null) && (plcElementInfo.length != 0))
		{
			String preferLocalConsumersStr = plcElementInfo[0];
			if (StringUtils.isNotBlank(preferLocalConsumersStr))
			{
				preferLocalConsumers = preferLocalConsumersStr.toLowerCase().equals("true");
			}
		}

		String[] svqElementInfo = extractElementInfo(doc, "support-virtual-queues");
		if ((svqElementInfo != null) && (svqElementInfo.length != 0))
		{
			String supportVirtualQueuesStr = svqElementInfo[0];
			if (StringUtils.isNotBlank(supportVirtualQueuesStr))
			{
				supportVirtualQueues = supportVirtualQueuesStr.toLowerCase().equals("true");
			}
		}

		String maxDistinctSubscriptionsStr = extractElementInfo(doc, "max-distinct-subscriptions")[0];
		maxDistinctSubscriptions = Integer.parseInt(maxDistinctSubscriptionsStr);

		String maxStoreTimeStr = extractElementInfo(doc, "store-time")[0];
		maxStoreTime = Long.parseLong(maxStoreTimeStr);

	}

	private synchronized void populateWorldMap(Document doc)
	{
		String selfName = GcsInfo.getAgentName();
		String selfHost = GcsInfo.getAgentHost();
		int selfPort = GcsInfo.getAgentPort();

		// Get a list of all elements in the document

		int npeers = doc.getElementsByTagName("peer").getLength();

		String[] hosts = extractElementInfo(doc, "ip");
		String[] ports = extractElementInfo(doc, "port");

		boolean isSelfPeerInWorldMap = false;

		peerList.clear();
		peerSet.clear();

		for (int i = 0; i < npeers; i++)
		{
			String agentName = GcsInfo.constructAgentName(hosts[i], Integer.parseInt(ports[i]));
			if (selfName.equalsIgnoreCase(agentName))
			{
				if (selfHost.equalsIgnoreCase(hosts[i]))
				{
					if (selfPort == Integer.parseInt(ports[i]))
					{
						isSelfPeerInWorldMap = true;
					}
				}
			}
			else
			{
				Peer p = new Peer(agentName, hosts[i], Integer.parseInt(ports[i]));
				InetSocketAddress inet = new InetSocketAddress(p.getHost(), p.getPort());
				peerList.add(p);
				peerSet.add(inet);
			}
		}

		if (!isSelfPeerInWorldMap)
		{
			System.err.println("This peer it's not in the world map.");
			Shutdown.now();
		}
	}

	private String[] extractElementInfo(Document doc, String tag)
	{
		NodeList nList = doc.getElementsByTagName(tag);
		String[] value = new String[nList.getLength()];

		for (int i = 0; i < nList.getLength(); i++)
		{
			Element name = (Element) nList.item(i);
			value[i] = name.getTextContent();
		}

		return value;
	}

	public static List<Peer> getPeerList()
	{
		return Collections.unmodifiableList(instance.peerList);
	}

	public static boolean contains(InetSocketAddress inet)
	{
		return instance.peerSet.contains(inet);
	}

	public static boolean preferLocalConsumers()
	{
		return instance.preferLocalConsumers;
	}

	public static boolean supportVirtualQueues()
	{
		return instance.supportVirtualQueues;
	}

	private Document parseXmlFile(String filename, boolean validating)
	{
		try
		{
			// Create a builder factory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(validating);
			File xmlFile = new File(filename);
			last_modified.set(xmlFile.lastModified());

			// Create the builder and parse the file
			Document doc = factory.newDocumentBuilder().parse(xmlFile);
			return doc;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	private void extractSecurityPolicies(Document doc)
	{
		NodeList secPolicies = doc.getElementsByTagName("security-policies");
		if (secPolicies.getLength() != 0)
		{
			try
			{
				JAXBContext jaxbContext = JAXBContext.newInstance(BrokerSecurityPolicy.class);

				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				JAXBElement<BrokerSecurityPolicy> element = unmarshaller.unmarshal(secPolicies.item(0), BrokerSecurityPolicy.class);
				BrokerSecurityPolicy brokerSecPolicies = element.getValue();
				secPolicy = brokerSecPolicies;
			}
			catch (JAXBException e)
			{
				log.error("Error parsing Broker security policies", e);
				secPolicy = null;
			}
		}
		else
		{
			secPolicy = null;
		}
	}

	public static Map<String, ProviderInfo> getCredentialValidatorProviders()
	{
		return instance.credentialValidatiorProviders;
	}

	private void loadCredentialValidatiorProviders(Document doc)
	{
		try
		{
			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList nodes = (NodeList) xpath.evaluate("/global-config/credential-validators/credential-validator", doc, XPathConstants.NODESET);

			for (int i = 0; i != nodes.getLength(); ++i)
			{
				try
				{
					if (!(nodes.item(i) instanceof Element))
						continue;

					String provName = null;
					String provPath = null;
					Element provParams = null;

					Element elem = (Element) nodes.item(i);

					provName = elem.getAttribute("provider-name");
					provPath = elem.getElementsByTagName("class").item(0).getTextContent();

					NodeList paramsNodeList = elem.getElementsByTagName("provider-params");
					if (paramsNodeList.getLength() != 0)
					{
						provParams = (Element) paramsNodeList.item(0);
					}

					ProviderInfo provInfo = new ProviderInfo(provName, provPath, provParams);
					credentialValidatiorProviders.put(provName, provInfo);
				}
				catch (Exception e)
				{
					log.error("Error parsing an credential-validator", e);
				}
			}
		}
		catch (Exception e)
		{
			log.error("Error parsing credential-validators", e);
		}
	}

	private synchronized boolean i_reload()
	{
		boolean wasReloaded = false;
		String globalConfigPath = GcsInfo.getGlobalConfigFilePath();
		File xmlFile = new File(globalConfigPath);
		long modified = xmlFile.lastModified();
		long lmod = last_modified.getAndSet(modified);
		if (modified != lmod)
		{
			log.info("New world map detected");

			Document doc = parseXmlFile(globalConfigPath, false);

			init(doc);
			wasReloaded = true;
		}
		return wasReloaded;
	}

	public static boolean reload()
	{
		return instance.i_reload();
	}

	public static BrokerSecurityPolicy getSecurityPolicies()
	{
		return instance.secPolicy;
	}

	public static int getMsgMaxSize()
	{
		return instance.msgMaxSize;
	}

	public static int getMaxQueues()
	{
		return instance.maxQueues;
	}

	public static long getMaxStoreTime()
	{
		return instance.maxStoreTime;
	}

	public static int getMaxDistinctSubscriptions()
	{
		return instance.maxDistinctSubscriptions;
	}

}
