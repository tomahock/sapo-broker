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

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;

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

	public BrokerClient(String host, int portNumber, int retries) throws Throwable
	{
		super(host, portNumber);
		this.setNumberOfTries(retries);
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
		brokerProtocolHandler = new BrokerProtocolHandler(this, getPortocolType(), networkConnector, this.isOldFramming());
		networkConnector.setProtocolHandler(brokerProtocolHandler);

		return brokerProtocolHandler;
	}

	/**
	 * Publish a message over UDP. <br/>
	 * Messages published must not carry a message identifier.
	 * 
	 * @param message
	 *            A NetPublish message
	 */

	public void publishMessageOverUdp(NetPublish message) throws InvalidParameterException
	{
		if (message.getActionId() != null)
		{
			throw new InvalidParameterException("Messages published over UDP are not allowed to carry a message identifier.");
		}

		HostInfo hostInfo = hosts.peek();
		if (hostInfo.getUdpPort() == -1)
		{
			throw new InvalidParameterException("Active agent information (HostInfo) doesn't have a specified UDP port.");
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);

		NetAction action = new NetAction(ActionType.PUBLISH);
		action.setPublishMessage(message);

		NetMessage netMessage = new NetMessage(action);

		try
		{
			if (this.isOldFramming())
			{
				byte[] marshaledMessage = super._netHandler.marshalMessage(netMessage);
				out.write(marshaledMessage);
			}
			else
			{
				super._netHandler.encode(netMessage, out);
			}

			out.flush();
			out.close();

			InetAddress inet = InetAddress.getByName(hostInfo.getHostname());
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(5000);

			byte[] msgData = stream.toByteArray();

			DatagramPacket packet = new DatagramPacket(msgData, msgData.length, inet, hostInfo.getUdpPort());
			socket.send(packet);
			socket.close();
		}
		catch (Throwable t)
		{
			log.error("Error processing UDP message", t);
			throw new RuntimeException(t);
		}
	}
}
