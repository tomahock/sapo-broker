package pt.com.broker.net;

import java.io.IOException;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteTimeoutException;
import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.io.UnsynchByteArrayOutputStream;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.ErrorHandler;
import pt.com.broker.messaging.BrokerConsumer;
import pt.com.broker.messaging.BrokerProducer;
import pt.com.broker.messaging.BrokerSyncConsumer;
import pt.com.broker.messaging.MQ;
import pt.com.broker.messaging.QueueSessionListenerList;
import pt.com.broker.messaging.TopicSubscriberList;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.net.IoSessionHelper;
import pt.com.types.NetAccepted;
import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetPing;
import pt.com.types.NetPong;
import pt.com.types.NetPublish;
import pt.com.types.NetSubscribe;
import pt.com.types.NetUnsubscribe;
import pt.com.types.NetAction.ActionType;
import pt.com.xml.FaultCode;
import pt.com.xml.SoapEnvelope;
import pt.com.xml.SoapSerializer;

public class BrokerProtocolHandler extends IoHandlerAdapter
{

	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private static final BrokerProducer _brokerProducer = BrokerProducer.getInstance();

	private static final BrokerConsumer _brokerConsumer = BrokerConsumer.getInstance();

	private static final int MAX_WRITE_BUFFER_SIZE = 5000;

	public BrokerProtocolHandler()
	{
	}

	@Override
	public void sessionCreated(IoSession iosession) throws Exception
	{
		IoSessionHelper.tagWithRemoteAddress(iosession);
		if (log.isDebugEnabled())
		{
			log.debug("Session created: " + IoSessionHelper.getRemoteAddress(iosession));
		}
	}

	@Override
	public void sessionClosed(IoSession iosession)
	{
		try
		{
			String remoteClient = IoSessionHelper.getRemoteAddress(iosession);
			log.info("Session closed: " + remoteClient);
			QueueSessionListenerList.removeSession(iosession);
			TopicSubscriberList.removeSession(iosession);
		}
		catch (Throwable e)
		{
			exceptionCaught(iosession, e);
		}
	}

	public void exceptionCaught(IoSession iosession, Throwable cause)
	{
		exceptionCaught(iosession, cause, null);
	}

	public void exceptionCaught(IoSession iosession, Throwable cause, String actionId)
	{
		Throwable rootCause = ErrorAnalyser.findRootCause(cause);

		NetFault fault = new NetFault("CODE:99999", rootCause.getMessage());
		fault.setActionId(actionId);
		fault.setDetail(ErrorHandler.buildStackTrace(rootCause));

		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(fault);

		NetMessage ex_msg = new NetMessage(action, null);

		if (!(cause instanceof IOException))
		{
			try
			{
				iosession.write(ex_msg);
			}
			catch (Throwable t)
			{
				log.error("The error information could not be delivered to the client", t);
			}
		}
		try
		{
			iosession.close(true);
		}
		catch (Throwable t)
		{
			log.error("Error closing client connection", t);
		}

		ErrorHandler.WTF wtf = ErrorHandler.buildSoapFault(cause);
		SoapEnvelope soap_ex_msg = wtf.Message;

		if (actionId != null)
		{
			soap_ex_msg.body.fault.faultCode.subcode = new FaultCode();
			soap_ex_msg.body.fault.faultCode.subcode.value = "action-id:" + actionId;

		}

		publishFault(soap_ex_msg);

		String client = IoSessionHelper.getRemoteAddress(iosession);

		if (!(wtf.Cause instanceof IOException))
		{
			try
			{
				iosession.write(ex_msg);
			}
			catch (Throwable t)
			{
				log.error("The error information could not be delivered to the client", t);
			}
		}

		try
		{
			iosession.close(true);
		}
		catch (Throwable t)
		{
			log.error("Error closing client connection", t);
		}

		try
		{
			String msg = "";

			if (wtf.Cause instanceof WriteTimeoutException)
			{
				String emsg = "Connection was closed because client was too slow! Slow queue consumers should use polling.";
				msg = "Client: " + client + ". Message: " + emsg;
			}
			else
			{
				String emsg = wtf.Cause.getMessage();
				msg = "Client: " + client + ". Message: " + emsg;
			}

			if (wtf.Cause instanceof IOException)
			{
				log.error(msg);
			}
			else
			{
				log.error(msg, wtf.Cause);
			}

		}
		catch (Throwable t)
		{
			log.error("Unspecified error", t);
		}
	}

	private void publishFault(SoapEnvelope faultMessage)
	{
		try
		{
			UnsynchByteArrayOutputStream out = new UnsynchByteArrayOutputStream();
			SoapSerializer.ToXml(faultMessage, out);

			NetBrokerMessage xfaultMessage = new NetBrokerMessage(out.toByteArray());

			NetPublish p = new NetPublish((String.format("/system/faults/#%s#", GcsInfo.getAgentName())), NetAction.DestinationType.TOPIC, xfaultMessage);

			_brokerProducer.publishMessage(p, null);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}

	public void messageReceived(final IoSession session, Object message) throws Exception
	{
		if (!(message instanceof NetMessage))
		{
			return;
		}

		final NetMessage request = (NetMessage) message;
		handleMessage(session, request);
	}

	private void handleMessage(IoSession session, final NetMessage request)
	{
		String actionId = null;

		try
		{
			final String requestSource = MQ.requestSource(request);

			switch (request.getAction().getActionType())
			{
			case PUBLISH:
				handlePublishMessage(session, request, requestSource);
				break;
			case POLL:
				handlePollMessage(session, request);
				break;
			case ACKNOWLEDGE_MESSAGE:
				handleAcknowledeMessage(session, request);
				break;
			case UNSUBSCRIBE:
				handleUnsubscribeMessage(session, request);
				break;
			case SUBSCRIBE:
				handleSubscribeMessage(session, request);
				break;
			case PING:
				handlePingMessage(session, request);
				break;
			case AUTH:
				BrokerProtocolHandlerAuthenticationHelper.handleAuthMessage(session, request);
				break;
			default:
				handleUnexpectedMessageType(session, request);
			}
		}
		catch (Throwable t)
		{
			exceptionCaught(session, t, actionId);
		}
	}

	private void handleUnexpectedMessageType(IoSession session, NetMessage request)
	{
		String actionId = null;
		switch(request.getAction().getActionType())
		{
		case FAULT:
			actionId = request.getAction().getFaultMessage().getActionId();
			break;
		case ACCEPTED:
			actionId = request.getAction().getAcceptedMessage().getActionId();
			break;
		}
		if(actionId == null)
		{
			session.write(NetFault.UnexpectedMessageTypeErrorMessage);
		}
		else
		{
			session.write(NetFault.getMessageFaultWithActionId(NetFault.UnexpectedMessageTypeErrorMessage, actionId));
		}
	}

	private void handlePublishMessage(IoSession session, NetMessage request, String messageSource)
	{
		NetPublish publish = request.getAction().getPublishMessage();
		
		String actionId = publish.getActionId();
		
		if (StringUtils.contains(publish.getDestination(), "@"))
		{
			if(publish.getActionId() == null)
			{
				session.write(NetFault.InvalidDestinationNameErrorMessage);
			}
			else
			{
				session.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidDestinationNameErrorMessage, publish.getActionId()));
			}
			return;
		}
		
		switch (publish.getDestinationType())
		{
		case TOPIC:
			_brokerProducer.publishMessage(publish, messageSource);
			break;
		case QUEUE:
			_brokerProducer.enqueueMessage(publish, messageSource);
			break;
		default:
			if(actionId == null)
			{
				session.write(NetFault.InvalidMessageDestinationTypeErrorMessage);
			}
			else
			{
				session.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidMessageDestinationTypeErrorMessage, actionId));
			}
			return;
		}

		sendAccepted(session, actionId);

	}

	private void handlePollMessage(IoSession session, NetMessage request)
	{
		sendAccepted(session, request.getAction().getPollMessage().getActionId());

		BrokerSyncConsumer.poll(request.getAction().getPollMessage(), session);
	}

	private void handleAcknowledeMessage(IoSession session, NetMessage request)
	{
		_brokerProducer.acknowledge(request.getAction().getAcknowledgeMessage());
	}

	private void handleUnsubscribeMessage(IoSession session, NetMessage request)
	{
		NetUnsubscribe unsubMsg = request.getAction().getUnsbuscribeMessage();
		if (unsubMsg == null)
			throw new RuntimeException("Not a valid request - inexistent Unsubscribe message");

		_brokerConsumer.unsubscribe(unsubMsg, session);
		String actionId = unsubMsg.getActionId();
		sendAccepted(session, actionId);

	}

	private void handleSubscribeMessage(IoSession session, NetMessage request)
	{
		NetSubscribe subscritption = request.getAction().getSubscribeMessage();

		if (StringUtils.isBlank(subscritption.getDestination()))
		{
			if(subscritption.getActionId() == null)
			{
				session.write(NetFault.InvalidDestinationNameErrorMessage);
			}
			else
			{
				session.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidDestinationNameErrorMessage, subscritption.getActionId()));
			}
			return;
		}

		switch (subscritption.getDestinationType())
		{
		case QUEUE:
			_brokerConsumer.listen(subscritption, session);
			break;
		case TOPIC:
			_brokerConsumer.subscribe(subscritption, session);
			break;
		case VIRTUAL_QUEUE:
			if (StringUtils.contains(subscritption.getDestination(), "@"))
			{
				_brokerConsumer.listen(subscritption, session);
			}
			else
			{
				if(subscritption.getActionId() == null)
				{
					session.write(NetFault.InvalidDestinationNameErrorMessage);
				}
				else
				{
					session.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidDestinationNameErrorMessage, subscritption.getActionId()));
				}
				return;
			}
			break;
		default:
			throw new IllegalArgumentException("Invalid subscription destination type");
		}
		sendAccepted(session, subscritption.getActionId());
	}

	private void handlePingMessage(final IoSession ios, NetMessage request)
	{
		NetPing netPing = request.getAction().getPingMessage();
		
		NetPong pong = new NetPong(netPing.getActionId());
		NetAction action = new NetAction(ActionType.PONG);
		action.setPongMessage(pong);
		NetMessage message = new NetMessage(action, null);
		ios.write(message);
	}

	private synchronized void sendAccepted(final IoSession ios, final String actionId)
	{
		if (actionId != null)
		{
			NetAccepted accept = new NetAccepted(actionId);
			NetAction action = new NetAction(NetAction.ActionType.ACCEPTED);
			action.setAcceptedMessage(accept);
			NetMessage message = new NetMessage(action, null);
			ios.write(message);
		}
	}

}
