package pt.com.broker.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.WriteTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.AuthInfoValidator;
import pt.com.broker.auth.AuthInfoVerifierFactory;
import pt.com.broker.auth.AuthValidationResult;
import pt.com.broker.auth.Session;
import pt.com.broker.auth.SessionProperties;
import pt.com.broker.codec.xml.FaultCode;
import pt.com.broker.codec.xml.SoapEnvelope;
import pt.com.broker.codec.xml.SoapSerializer;
import pt.com.broker.core.ErrorHandler;
import pt.com.broker.messaging.BrokerConsumer;
import pt.com.broker.messaging.BrokerProducer;
import pt.com.broker.messaging.BrokerSyncConsumer;
import pt.com.broker.messaging.MQ;
import pt.com.broker.messaging.QueueSessionListenerList;
import pt.com.broker.messaging.SynchronousMessageListener;
import pt.com.broker.messaging.TopicSubscriberList;
import pt.com.broker.types.ChannelAttributes;
import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPing;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetUnsubscribe;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.gcs.conf.GcsInfo;

/**
 * * BrokerProtocolHandler is an Netty ChannelHandler. It handles messages from clients.
 * 
 */

@Sharable
public class BrokerProtocolHandler extends SimpleChannelHandler
{

	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private static final BrokerProducer _brokerProducer = BrokerProducer.getInstance();

	private static final BrokerConsumer _brokerConsumer = BrokerConsumer.getInstance();
	
	private static final BrokerProtocolHandler instance;
	
	private static final String ACK_REQUIRED = "ACK_REQUIRED";

	static
	{
		instance = new BrokerProtocolHandler();
	}

	public static BrokerProtocolHandler getInstance()
	{
		return instance;
	}

	private BrokerProtocolHandler()
	{
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		// Get the SslHandler in the current pipeline.
		// We added it in SecureChatPipelineFactory.
		final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);

		// Get notified when SSL handshake is done.
		if (sslHandler != null)
		{
			ChannelFuture handshakeFuture = sslHandler.handshake();
			handshakeFuture.addListener(new ChannelFutureListener()
			{
				@Override
				public void operationComplete(ChannelFuture cf) throws Exception
				{
					log.info("BrokerProtocolHandler.channelConnected() - handshake complete. Success: " + cf.isSuccess());
				}
			});
		}
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelClosed(ctx, e);
		Channel channel = ctx.getChannel();

		if (log.isDebugEnabled())
		{
			log.debug("channel created: '%s'", channel.getRemoteAddress().toString());
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelClosed(ctx, e);
		Channel channel = ctx.getChannel();
		try
		{
			String remoteClient = channel.getRemoteAddress().toString();
			QueueSessionListenerList.removeSession(channel);
			TopicSubscriberList.removeSession(channel);
			SynchronousMessageListener.removeSession(ctx);

			ChannelAttributes.remove(ctx);
			
			log.info("channel closed: " + remoteClient);
		}
		catch (Throwable t)
		{
			exceptionCaught(channel, t, null);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
	{
		exceptionCaught(ctx.getChannel(), e.getCause(), null);
	}

	public void exceptionCaught(Channel channel, Throwable cause, String actionId)
	{
		try
		{
			Throwable rootCause = ErrorAnalyser.findRootCause(cause);

			String client = channel.getRemoteAddress() != null ? channel.getRemoteAddress().toString() : "Client unknown";

			log.error("Exception caught. Client: {} ", client, rootCause);

			CriticalErrors.exitIfCritical(rootCause);

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
					if (channel.isConnected())
					{
						channel.write(ex_msg).addListener(ChannelFutureListener.CLOSE);
					}
				}
				catch (Throwable t)
				{
					log.error("Failed to write error message to client '{}'", client, t);
				}
			}
			else
			{
				log.info("Closing channel.");
				channel.close();
			}
			
			ErrorHandler.WTF wtf = ErrorHandler.buildSoapFault(cause);
			SoapEnvelope soap_ex_msg = wtf.Message;

			if (actionId != null)
			{
				soap_ex_msg.body.fault.faultCode.subcode = new FaultCode();
				soap_ex_msg.body.fault.faultCode.subcode.value = "action-id:" + actionId;
			}

			publishFault(soap_ex_msg);

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
			log.error("Error processing caught exception", t);
		}
	}

	private void publishFault(SoapEnvelope faultMessage)
	{
		try
		{
			UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream();
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

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	{
		try
		{
			if (e.getMessage() == null)
			{
				return;
			}
		}
		catch (Throwable t)
		{
			return;
		}

		if (!(e.getMessage() instanceof NetMessage))
		{
			log.error("Uknown message type");
			return;
		}

		final NetMessage request = (NetMessage) e.getMessage();
		handleMessage(ctx, request);
	}

	private void handleMessage(ChannelHandlerContext ctx, final NetMessage request)
	{
		String actionId = null;
		Channel channel = ctx.getChannel();

		try
		{
			switch (request.getAction().getActionType())
			{
			case PUBLISH:
				handlePublishMessage(ctx, request);
				break;
			case POLL:
				handlePollMessage(ctx, request);
				break;
			case ACKNOWLEDGE:
				handleAcknowledeMessage(ctx, request);
				break;
			case UNSUBSCRIBE:
				handleUnsubscribeMessage(ctx, request);
				break;
			case SUBSCRIBE:
				handleSubscribeMessage(ctx, request);
				break;
			case PING:
				handlePingMessage(ctx, request);
				break;
			case AUTH:
				handleAuthMessage(ctx, request);
				break;
			default:
				handleUnexpectedMessageType(ctx, request);
			}
		}
		catch (Throwable t)
		{
			exceptionCaught(channel, t, actionId);
		}
	}

	private void handleUnexpectedMessageType(ChannelHandlerContext ctx, NetMessage request)
	{

		Channel channel = ctx.getChannel();
		String actionId = null;
		switch (request.getAction().getActionType())
		{
		case FAULT:
			actionId = request.getAction().getFaultMessage().getActionId();
			break;
		case ACCEPTED:
			actionId = request.getAction().getAcceptedMessage().getActionId();
			break;
		}
		if (actionId == null)
		{
			channel.write(NetFault.UnexpectedMessageTypeErrorMessage);
		}
		else
		{
			channel.write(NetFault.getMessageFaultWithActionId(NetFault.UnexpectedMessageTypeErrorMessage, actionId));
		}
	}

	private void handlePublishMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		final String messageSource = MQ.requestSource(request);
		Channel channel = ctx.getChannel();
		NetPublish publish = request.getAction().getPublishMessage();

		String actionId = publish.getActionId();

		if (StringUtils.contains(publish.getDestination(), "@"))
		{
			if (publish.getActionId() == null)
			{
				channel.write(NetFault.InvalidDestinationNameErrorMessage);
			}
			else
			{
				channel.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidDestinationNameErrorMessage, publish.getActionId()));
			}
			return;
		}

		switch (publish.getDestinationType())
		{
		case TOPIC:
			_brokerProducer.publishMessage(publish, messageSource);
			break;
		case QUEUE:
			if (!_brokerProducer.enqueueMessage(publish, messageSource))
			{
				if (actionId == null)
				{
					channel.write(NetFault.MaximumNrQueuesReachedErrorMessage);
				}
				else
				{
					channel.write(NetFault.getMessageFaultWithActionId(NetFault.MaximumNrQueuesReachedErrorMessage, actionId));
				}
				return;
			}
			break;
		default:
			if (actionId == null)
			{
				channel.write(NetFault.InvalidMessageDestinationTypeErrorMessage);
			}
			else
			{
				channel.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidMessageDestinationTypeErrorMessage, actionId));
			}
			return;
		}
		sendAccepted(ctx, actionId);
	}

	private void handlePollMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		sendAccepted(ctx, request.getAction().getPollMessage().getActionId());
		BrokerSyncConsumer.poll(request.getAction().getPollMessage(), ctx);
	}

	private void handleAcknowledeMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		Channel channel = ctx.getChannel();
		_brokerProducer.acknowledge(request.getAction().getAcknowledgeMessage(), channel);
	}

	private void handleUnsubscribeMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		Channel channel = ctx.getChannel();
		NetUnsubscribe unsubMsg = request.getAction().getUnsbuscribeMessage();
		if (unsubMsg == null)
			throw new RuntimeException("Not a valid request - inexistent Unsubscribe message");

		_brokerConsumer.unsubscribe(unsubMsg, channel);
		String actionId = unsubMsg.getActionId();
		sendAccepted(ctx, actionId);
	}

	private void handleSubscribeMessage(ChannelHandlerContext ctx, NetMessage request)
	{

		Channel channel = ctx.getChannel();
		NetSubscribe subscritption = request.getAction().getSubscribeMessage();

		if (StringUtils.isBlank(subscritption.getDestination()))
		{
			if (subscritption.getActionId() == null)
			{
				channel.write(NetFault.InvalidDestinationNameErrorMessage);
			}
			else
			{
				channel.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidDestinationNameErrorMessage, subscritption.getActionId()));
			}
			return;
		}

		boolean ackRequired = true;
		
		if( request.getHeaders() != null)
		{
			String value = request.getHeaders().get(ACK_REQUIRED);
			ackRequired = (value == null) ? true : !value.equalsIgnoreCase("false");
		}
		
		switch (subscritption.getDestinationType())
		{
		case QUEUE:
			_brokerConsumer.listen(subscritption, channel, ackRequired);
			break;
		case TOPIC:
			if (!_brokerConsumer.subscribe(subscritption, channel))
			{
				if (subscritption.getActionId() == null)
				{
					channel.write(NetFault.MaximumDistinctSubscriptionsReachedErrorMessage);
				}
				else
				{
					channel.write(NetFault.getMessageFaultWithActionId(NetFault.MaximumDistinctSubscriptionsReachedErrorMessage, subscritption.getActionId()));
				}
				return;
			}
			break;
		case VIRTUAL_QUEUE:
			if (StringUtils.contains(subscritption.getDestination(), "@"))
			{
				_brokerConsumer.listen(subscritption, channel, ackRequired);
			}
			else
			{
				if (subscritption.getActionId() == null)
				{
					channel.write(NetFault.InvalidDestinationNameErrorMessage);
				}
				else
				{
					channel.write(NetFault.getMessageFaultWithActionId(NetFault.InvalidDestinationNameErrorMessage, subscritption.getActionId()));
				}
				return;
			}
			break;
		default:
			throw new IllegalArgumentException("Invalid subscription destination type");
		}
		sendAccepted(ctx, subscritption.getActionId());
	}

	private void handlePingMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		Channel channel = ctx.getChannel();
		NetPing netPing = request.getAction().getPingMessage();

		NetPong pong = new NetPong(netPing.getActionId());
		NetAction action = new NetAction(ActionType.PONG);
		action.setPongMessage(pong);
		NetMessage message = new NetMessage(action, null);
		channel.write(message);
	}

	private void handleAuthMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		Channel channel = ctx.getChannel();
		InetSocketAddress localAddress = (InetSocketAddress) channel.getLocalAddress();

		if (localAddress.getPort() != GcsInfo.getBrokerSSLPort())
		{
			channel.write(NetFault.InvalidAuthenticationChannelType);
			return;
		}

		NetAuthentication netAuthentication = request.getAction().getAuthenticationMessage();

		// // Validate client credentials
		AuthInfo info = new AuthInfo(netAuthentication.getUserId(), netAuthentication.getRoles(), netAuthentication.getToken(), netAuthentication.getAuthenticationType());
		AuthInfoValidator validator = AuthInfoVerifierFactory.getValidator(info.getUserAuthenticationType());
		if (validator == null)
		{
			channel.write(NetFault.UnknownAuthenticationTypeMessage);
			return;
		}
		AuthValidationResult validateResult = null;
		try
		{
			validateResult = validator.validate(info);
		}
		catch (Exception e)
		{
			channel.write(NetFault.getMessageFaultWithDetail(NetFault.AuthenticationFailedErrorMessage, "Internal Error"));
			return;
		}

		if (!validateResult.areCredentialsValid())
		{
			channel.write(NetFault.getMessageFaultWithDetail(NetFault.AuthenticationFailedErrorMessage, validateResult.getReasonForFailure()));
			return;
		}

		Session plainSession = (Session) ChannelAttributes.get(ctx, "BROKER_SESSION_PROPERTIES");

		SessionProperties plainSessionProps = plainSession.getSessionProperties();
		plainSessionProps.setRoles(validateResult.getRoles());
		plainSession.updateAcl();

		if (netAuthentication.getActionId() != null)
		{
			sendAccepted(ctx, netAuthentication.getActionId());
		}
	}

	private synchronized void sendAccepted(ChannelHandlerContext ctx, final String actionId)
	{
		Channel channel = ctx.getChannel();
		if (actionId != null)
		{
			NetAccepted accept = new NetAccepted(actionId);
			NetAction action = new NetAction(NetAction.ActionType.ACCEPTED);
			action.setAcceptedMessage(accept);
			NetMessage message = new NetMessage(action, null);
			channel.write(message);
		}
	}
}
