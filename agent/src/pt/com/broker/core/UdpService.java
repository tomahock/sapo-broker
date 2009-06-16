package pt.com.broker.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerCodecRouter;
import pt.com.broker.messaging.BrokerProducer;
import pt.com.broker.messaging.MQ;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.SimpleFramingDecoderV2;
import pt.com.gcs.conf.GcsInfo;

/**
 * UdpService is responsible for initializing client's UDP interface.
 *
 */

public class UdpService
{
	private static final Logger log = LoggerFactory.getLogger(UdpService.class);

	private static final ExecutorService exec = CustomExecutors.newThreadPool(4, "BrokerUdp");

	private static final BrokerProducer _brokerProducer = BrokerProducer.getInstance();

	public UdpService()
	{
	}

	public void start()
	{
		DatagramSocket socket;
		try
		{
			InetAddress inet = InetAddress.getByName("0.0.0.0");
			int port = GcsInfo.getBrokerUdpPort();
			socket = new DatagramSocket(port, inet);
			socket.setReceiveBufferSize(4 * 1024 * 1024);

			log.info("Starting UdpService. Listen Port: {}. ReceiveBufferSize: {}", port, socket.getReceiveBufferSize());
		}
		catch (Exception error)
		{
			log.error("Error creating UDP Endpoint", error);
			return;
		}

		int mSize = (int) Math.pow(2, 16);

		DatagramPacket packet = new DatagramPacket(new byte[mSize], mSize);
		log.info("UdpService started and listening for packets.");

		while (true)
		{
			try
			{
				socket.receive(packet);
				
				NetMessage message = decodePacket(packet); 
				
				exec.execute(new UdpPacketProcessor(message));
			}
			catch (Throwable error)
			{
				log.error(error.getMessage(), error);
			}
		}
	}

	private NetMessage decodePacket(DatagramPacket packet)
	{
		byte[] receivedData = packet.getData();
		int len = packet.getLength();
		
		final int HEADER_SIZE = 8;
		
		byte[] headerData = new byte[HEADER_SIZE];
		System.arraycopy(receivedData, 0, headerData, 0, HEADER_SIZE);
		
		DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(headerData));
		short protoType;
		short protoVersion;
		try
		{
			protoType = dataInputStream.readShort();
			protoVersion = dataInputStream.readShort();
			
		}
		catch (IOException ioEx)
		{
			throw new RuntimeException(ioEx);
		}
		
		ProtocolCodecFactory codec = BrokerCodecRouter.getProcolCodec(new Short(protoType));
		if (codec == null)
		{
			throw new RuntimeException("Invalid protocol type: " + protoType);
		}
		SimpleFramingDecoderV2 decoder;
		try
		{
			decoder = (SimpleFramingDecoderV2) codec.getDecoder(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Invalid protocol type decoder implementation: " + protoType, e);
		}
		
		byte[] messageData = new byte[len - HEADER_SIZE];
		System.arraycopy(receivedData, HEADER_SIZE, messageData, 0, len - HEADER_SIZE);
		
		return (NetMessage) decoder.processBody(messageData, protoType, protoVersion);
	}

	final static class UdpPacketProcessor implements Runnable
	{
		final NetMessage message;

		UdpPacketProcessor(NetMessage message)
		{
			this.message = message;
		}

		@Override
		public void run()
		{
			
			NetPublish publish = message.getAction().getPublishMessage();
			if (StringUtils.contains(publish.getDestination(), "@"))
			{
				return;
			}
			
			final String messageSource = MQ.requestSource(message);

			switch (publish.getDestinationType())
			{
			case TOPIC:
				_brokerProducer.publishMessage(publish, messageSource);
				break;
			case QUEUE:
				if(!_brokerProducer.enqueueMessage(publish, messageSource))
				{
					return;
				}
				break;
			}

		}
	}

}
