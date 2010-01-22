package pt.com.gcs.messaging;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.GlobalConfig;
import pt.com.gcs.messaging.GlobalConfigMonitor.GlobalConfigModifiedListener;
import pt.com.gcs.net.IoSessionHelper;
import pt.com.gcs.net.Peer;

/**
 * GcsAcceptorProtocolHandler is an MINA IoHandlerAdapter. It handles remote subscription messages and acknowledges from other agents.
 * 
 */

class GcsAcceptorProtocolHandler extends IoHandlerAdapter
{
	private static Logger log = LoggerFactory.getLogger(GcsAcceptorProtocolHandler.class);

	private static List<InetSocketAddress> peersAddressList;

	static
	{
		createPeersList();
		GlobalConfigMonitor.addGlobalConfigModifiedListener(new GlobalConfigModifiedListener()
		{

			@Override
			public void globalConfigModified()
			{
				globalConfigReloaded();
			}

		});
	}

	private static void createPeersList()
	{
		List<Peer> peerList = GlobalConfig.getPeerList();
		peersAddressList = new ArrayList<InetSocketAddress>(peerList.size());
		for (Peer peer : peerList)
		{
			InetSocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
			peersAddressList.add(addr);
		}
	}

	public static void globalConfigReloaded()
	{
		createPeersList();
	}

	@Override
	public void exceptionCaught(IoSession iosession, Throwable cause) throws Exception
	{
		Throwable rootCause = ErrorAnalyser.findRootCause(cause);
		CriticalErrors.exitIfCritical(rootCause);
		log.error("Exception Caught:'{}', '{}'", IoSessionHelper.getRemoteAddress(iosession), rootCause.getMessage());
		if (iosession.isConnected() && !iosession.isClosing())
		{
			log.error("STACKTRACE", rootCause);
		}
	}

	@Override
	public void messageReceived(IoSession iosession, Object message) throws Exception
	{
		final InternalMessage m = (InternalMessage) message;

		NetBrokerMessage brkMsg = m.getContent();

		String msgContent = new String(brkMsg.getPayload(), "UTF-8");

		if (log.isDebugEnabled())
		{
			log.debug("Message Received from: '{}', Type: '{}'", IoSessionHelper.getRemoteAddress(iosession), m.getType());
		}

		if (m.getType() == MessageType.ACK)
		{

			Gcs.ackMessage(m.getDestination(), m.getMessageId());

			return;
		}
		else if (m.getType() == (MessageType.HELLO))
		{
			validatePeer(iosession, msgContent);
			boolean isValid = ((Boolean) iosession.getAttribute("GcsAcceptorProtocolHandler.ISVALID")).booleanValue();
			if (!isValid)
			{
				String paddr = String.valueOf(iosession.getAttribute("GcsAcceptorProtocolHandler.PEER_ADDRESS"));
				log.warn("A peer from \"{}\" tried to connect but it does not appear in the world map.", paddr);
				iosession.close();
			}
			else
			{
				log.debug("Peer is valid!");
				return;
			}
			return;
		}
		else if ((m.getType() == MessageType.SYSTEM_TOPIC) || (m.getType() == MessageType.SYSTEM_QUEUE))
		{

			final String action = extract(msgContent, "<action>", "</action>");
			final String src_name = extract(msgContent, "<source-name>", "</source-name>");
			// final String src_ip = extract(payload, "<source-ip>",
			// "</source-ip>");
			final String destinationName = extract(msgContent, "<destination>", "</destination>");

			if (log.isInfoEnabled())
			{
				String lmsg = String.format("Action: '%s' Consumer; Destination: '%s'; Source: '%s'", action, destinationName, src_name);
				log.info(lmsg);
			}

			if (m.getType() == MessageType.SYSTEM_TOPIC)
			{
				if (action.equals("CREATE"))
				{
					RemoteTopicConsumers.add(m.getDestination(), iosession);
				}
				else if (action.equals("DELETE"))
				{
					RemoteTopicConsumers.remove(m.getDestination(), iosession);
				}
			}
			else if (m.getType() == MessageType.SYSTEM_QUEUE)
			{
				if (action.equals("CREATE"))
				{
					RemoteQueueConsumers.add(m.getDestination(), iosession);
					QueueProcessorList.get(destinationName);
				}
				else if (action.equals("DELETE"))
				{
					RemoteQueueConsumers.remove(m.getDestination(), iosession);
				}
			}
			acknowledgeSystemMessage(m, iosession);
		}
		else
		{
			log.warn("Unkwown message type. Don't know how to handle message");
		}
	}

	private void acknowledgeSystemMessage(InternalMessage message, IoSession ioSession)
	{
		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><message-id>%s</message-id></sysmessage>";
		String payload = String.format(ptemplate, "SYSTEM_ACKNOWLEDGE", GcsInfo.getAgentName(), ioSession.getLocalAddress().toString(), message.getMessageId());
		InternalMessage ackMsg = new InternalMessage();
		NetBrokerMessage brkMsg;
		try
		{
			brkMsg = new NetBrokerMessage(payload.getBytes("UTF-8"));
			ackMsg.setType(MessageType.SYSTEM_ACK);

			ackMsg.setContent(brkMsg);
		}
		catch (UnsupportedEncodingException e)
		{
			// This exception is never thrown because UTF-8 encoding is built-in
			// in every JVM
		}

		ioSession.write(ackMsg);
	}

	@Override
	public void messageSent(IoSession iosession, Object message) throws Exception
	{
		if (log.isDebugEnabled())
		{
			log.debug("Message Sent: '{}', '{}'", IoSessionHelper.getRemoteAddress(iosession), message.toString());
		}
	}

	@Override
	public void sessionClosed(IoSession iosession) throws Exception
	{
		log.info("Session Closed: '{}'", IoSessionHelper.getRemoteAddress(iosession));
		RemoteTopicConsumers.remove(iosession);
		RemoteQueueConsumers.remove(iosession);
	}

	@Override
	public void sessionCreated(IoSession iosession) throws Exception
	{
		if (!validPeerAddress(iosession))
		{
			iosession.close(true);
			log.warn("GCS: connection refused");
			return;
		}

		IoSessionHelper.tagWithRemoteAddress(iosession);
		if (log.isDebugEnabled())
		{
			log.debug("Session Created: '{}'", IoSessionHelper.getRemoteAddress(iosession));
		}
	}

	private boolean validPeerAddress(IoSession iosession)
	{
		InetSocketAddress remotePeer = (InetSocketAddress) iosession.getRemoteAddress();
		InetAddress address = remotePeer.getAddress();

		for (InetSocketAddress addr : peersAddressList)
		{
			if (address.equals(addr.getAddress()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void sessionIdle(IoSession iosession, IdleStatus status) throws Exception
	{
		if (log.isDebugEnabled())
		{
			log.debug("Session Idle:'{}'", IoSessionHelper.getRemoteAddress(iosession));
		}
	}

	@Override
	public void sessionOpened(IoSession iosession) throws Exception
	{
		log.info("Session Opened: '{}'", IoSessionHelper.getRemoteAddress(iosession));
	}

	private void validatePeer(IoSession iosession, String helloMessage)
	{
		log.debug("\"Hello\" message received: '{}'", helloMessage);
		try
		{
			String peerName = StringUtils.substringBefore(helloMessage, "@");
			String peerAddr = StringUtils.substringAfter(helloMessage, "@");
			String peerHost = StringUtils.substringBefore(peerAddr, ":");
			int peerPort = Integer.parseInt(StringUtils.substringAfter(peerAddr, ":"));
			iosession.setAttribute("GcsAcceptorProtocolHandler.PEER_ADDRESS", peerAddr);
			Peer peer = new Peer(peerName, peerHost, peerPort);
			if (Gcs.getPeerList().contains(peer))
			{
				log.debug("Peer '{}' exists in the world map'", peer.toString());
				iosession.setAttribute("GcsAcceptorProtocolHandler.ISVALID", true);
				return;
			}
		}
		catch (Throwable t)
		{
			iosession.setAttribute("GcsAcceptorProtocolHandler.PEER_ADDRESS", "Unknown address");
			log.error(t.getMessage(), t);
		}

		iosession.setAttribute("GcsAcceptorProtocolHandler.ISVALID", false);
	}

	private String extract(String ins, String prefix, String sufix)
	{
		if (StringUtils.isBlank(ins))
		{
			return "";
		}

		int s = ins.indexOf(prefix) + prefix.length();
		int e = ins.indexOf(sufix);
		return ins.substring(s, e);
	}
}
