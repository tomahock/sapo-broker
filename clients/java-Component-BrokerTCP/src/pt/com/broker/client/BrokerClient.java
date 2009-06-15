package pt.com.broker.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidParameterException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetAction.ActionType;

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

	/**
	 * Publish a message over UDP. <br/>
	 * Messages published over UDP must be of type "Publication" and not carry a message identifier.
	 * 
	 * @param message
	 * @throws InvalidParameterException
	 */

	public void publishMessageOverUdp(NetMessage message) throws InvalidParameterException
	{
		if (!message.getAction().getActionType().equals(ActionType.PUBLISH))
		{
			throw new InvalidParameterException("Only Publish messages are allowed over UDP.");
		}

		if (message.getAction().getPublishMessage().getActionId() != null)
		{
			throw new InvalidParameterException("Messages published over are allowed to carry a message identifier.");
		}

		HostInfo hostInfo = hosts.peek();
		if (hostInfo.getUdpPort() == -1)
		{
			throw new InvalidParameterException("Active agent information (HostInfo) dosen't have a specified UDP port.");
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);

		try
		{
			super._netHandler.encode(message, out);

			out.flush();
			out.close();

			InetAddress inet = InetAddress.getByName(hostInfo.getHostname());
			DatagramSocket socket = new DatagramSocket(hostInfo.getUdpPort(), inet);
			socket.setSoTimeout(5000);

			byte[] msgData = stream.toByteArray();

			DatagramPacket packet = new DatagramPacket(msgData, msgData.length);
			socket.send(packet);
		}
		catch (Throwable t)
		{
			log.error("Error processing UDP message", t);
			throw new RuntimeException(t);
		}
	}

}
