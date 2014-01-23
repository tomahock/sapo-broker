package pt.com.broker.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;

public class UdpClient
{
	private static final Logger log = LoggerFactory.getLogger(UdpClient.class);

	private final BindingSerializer serializer;
	private final short proto_type = 1;
	private final String hostname;
	private final boolean isOldFramming;
	private final int udpPort;

	public UdpClient(String hostname, int udpPort)
	{
		this(hostname, udpPort, false);
	}

	public UdpClient(String hostname, int udpPort, boolean isOldFramming)
	{
		super();
		this.hostname = hostname;
		this.isOldFramming = isOldFramming;
		this.udpPort = udpPort;

		if (isOldFramming)
		{
			serializer = new SoapBindingSerializer();
		}
		else
		{
			serializer = new ProtoBufBindingSerializer();
		}
	}

	/**
	 * Publish a message over UDP. <br/>
	 * Messages published must not carry a message identifier.
	 * 
	 * @param message
	 *            A NetPublish message
	 */

	public void publish(NetPublish message) throws InvalidParameterException
	{
		if (message.getActionId() != null)
		{
			throw new InvalidParameterException("Messages published over UDP are not allowed to carry a message identifier.");
		}

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);

		NetAction action = new NetAction(ActionType.PUBLISH);
		action.setPublishMessage(message);

		NetMessage netMessage = new NetMessage(action);

		try
		{
			if (isOldFramming)
			{
				byte[] marshaledMessage = marshalMessage(netMessage);
				out.write(marshaledMessage);
			}
			else
			{
				encode(netMessage, out);
			}

			out.flush();
			out.close();

			InetAddress inet = InetAddress.getByName(hostname);

			// System.out.println("UdpClient.publishMessageOverUdp.inet: " + inet.toString());

			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(5000);

			byte[] msgData = stream.toByteArray();

			DatagramPacket packet = new DatagramPacket(msgData, msgData.length, inet, udpPort);

			// System.out.println("UdpClient.publishMessageOverUdp.packet: " + packet.toString());
			socket.send(packet);
			socket.close();
		}
		catch (Throwable t)
		{
			log.error("Error processing UDP message", t);
			throw new RuntimeException(t);
		}
	}

	public void encode(NetMessage message, DataOutputStream out) throws IOException
	{
		byte[] marshaledMsg = marshalMessage(message);

		if (!isOldFramming)
		{
			short protocolType = proto_type;
			short protocolVersion = (short) 0;
			out.writeShort(protocolType);
			out.writeShort(protocolVersion);
		}

		out.writeInt(marshaledMsg.length);

		out.write(marshaledMsg);
	}

	public byte[] marshalMessage(NetMessage message) throws IOException
	{
		byte[] marshaledMsg = serializer.marshal(message);
		return marshaledMsg;
	}
}
