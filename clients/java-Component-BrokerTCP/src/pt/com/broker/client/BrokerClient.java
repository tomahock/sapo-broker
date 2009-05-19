package pt.com.broker.client;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetProtocolType;

public class BrokerClient extends BaseBrokerClient
{
	private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);

	public BrokerClient(String host, int portNumber) throws Throwable
	{
		super(host, portNumber);
		init();
	}

	public BrokerClient(String host, int portNumber, String appName) throws Throwable
	{
		super(host, portNumber, appName);
		init();
	}

	public BrokerClient(String host, int portNumber, String appName, NetProtocolType ptype) throws Throwable
	{
		super(host, portNumber, appName, ptype);
		init();
	}

	public BrokerClient(Collection<HostInfo> hosts) throws Throwable
	{
		super(hosts);
		init();
	}

	public BrokerClient(Collection<HostInfo> hosts, String appName) throws Throwable
	{
		super(hosts, appName);
		init();
	}

	public BrokerClient(Collection<HostInfo> hosts, String appName, NetProtocolType ptype) throws Throwable
	{
		super(hosts, appName, ptype);
		init();
	}

	@Override
	protected BrokerProtocolHandler getBrokerProtocolHandler() throws Throwable
	{
		BrokerProtocolHandler brokerProtocolHandler;

		NetworkConnector networkConnector = new NetworkConnector(getHostInfo());
		brokerProtocolHandler = new BrokerProtocolHandler(this, getPortocolType(), networkConnector);
		networkConnector.setProtocolHandler(brokerProtocolHandler);

		return brokerProtocolHandler;
	}

}
