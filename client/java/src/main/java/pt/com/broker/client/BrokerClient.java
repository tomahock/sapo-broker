package pt.com.broker.client;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetProtocolType;

/**
 * BrokerClient represents a connection between a client and an agent. Through it clients can produce and consume broker messages. <br/>
 * The communication is made in clear.
 * 
 */

public class BrokerClient extends BaseBrokerClient
{
	private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);

	public BrokerClient(String host, int portNumber) throws Throwable
	{
		super(host, portNumber, HostInfo.DEFAULT_CONNECT_TIMEOUT, HostInfo.DEFAULT_READ_TIMEOUT);
		init();
	}

	public BrokerClient(String host, int portNumber, int retriest) throws Throwable
	{
		super(host, portNumber, HostInfo.DEFAULT_CONNECT_TIMEOUT, HostInfo.DEFAULT_READ_TIMEOUT);
		this.setNumberOfTries(retriest);
		init();
	}

	public BrokerClient(String host, int portNumber, int retries, int connectTimeout, int readTimeout) throws Throwable
	{
		super(host, portNumber, connectTimeout, readTimeout);
		this.setNumberOfTries(retries);
		init();
	}

	public BrokerClient(String host, int portNumber, String appName, int connectTimeout, int readTimeout) throws Throwable
	{
		super(host, portNumber, connectTimeout, readTimeout, appName);
		init();
	}

	public BrokerClient(String host, int portNumber, String appName) throws Throwable
	{
		super(host, portNumber, HostInfo.DEFAULT_CONNECT_TIMEOUT, HostInfo.DEFAULT_READ_TIMEOUT, appName);
		init();
	}

	public BrokerClient(String host, int portNumber, String appName, NetProtocolType ptype, int connectTimeout, int readTimeout) throws Throwable
	{
		super(host, portNumber, connectTimeout, readTimeout, appName, ptype);
		init();
	}

	public BrokerClient(String host, int portNumber, String appName, NetProtocolType ptype) throws Throwable
	{
		super(host, portNumber, HostInfo.DEFAULT_CONNECT_TIMEOUT, HostInfo.DEFAULT_READ_TIMEOUT, appName, ptype);
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
		brokerProtocolHandler = new BrokerProtocolHandler(this, getProtocolType(), networkConnector, this.isOldFramming());
		networkConnector.setProtocolHandler(brokerProtocolHandler);

		return brokerProtocolHandler;
	}

	@Override
	public String toString()
	{
		return String.format("BrokerClient [HostInfo:%s]", getHostInfo());
	}



}
