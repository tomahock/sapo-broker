package pt.com.broker.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

import io.netty.channel.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.AuthInfoValidator;
import pt.com.broker.auth.AuthInfoVerifierFactory;
import pt.com.broker.auth.AuthValidationResult;
import pt.com.broker.auth.Session;
import pt.com.broker.auth.SessionProperties;
import pt.com.broker.codec.xml.soap.FaultCode;
import pt.com.broker.codec.xml.soap.SoapEnvelope;
import pt.com.broker.codec.xml.SoapSerializer;
import pt.com.broker.core.ErrorHandler;
import pt.com.broker.messaging.BrokerConsumer;
import pt.com.broker.messaging.BrokerProducer;
import pt.com.broker.messaging.BrokerSyncConsumer;
import pt.com.broker.messaging.MQ;
import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.Headers;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAcknowledge;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPing;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetUnsubscribe;
import pt.com.broker.types.channels.ChannelAttributes;
import pt.com.broker.types.channels.ListenerChannelFactory;
import pt.com.broker.types.stats.MiscStats;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.GlobalConfig;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.QueueProcessorList;
import pt.com.gcs.messaging.TopicProcessorList;

/**
 * * BrokerProtocolHandler is an Netty ChannelHandler. It handles messages from clients.
 */

@ChannelHandler.Sharable
public class BrokerProtocolHandler extends SimpleChannelInboundHandler<NetMessage>
{

	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);

	private static final BrokerProducer _brokerProducer = BrokerProducer.getInstance();

	private static final BrokerConsumer _brokerConsumer = BrokerConsumer.getInstance();

	private static final BrokerProtocolHandler instance;

	private static final Set<String> open_channels = java.util.Collections.newSetFromMap(new ConcurrentHashMapV8<String, Boolean>());

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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        Channel channel = ctx.channel();

        try
        {
            if ((channel != null) && (channel.remoteAddress() != null))
            {
                String remoteClient = channel.remoteAddress().toString();
                log.info("channel open: {}", remoteClient);

                if (open_channels.contains(remoteClient))
                {
                    log.warn("There is already a registred ip:port bundle with the value '{}'. Check your network settings", remoteClient);
                }
                else
                {
                    open_channels.add(remoteClient);
                }
            }
        }
        catch (Throwable t)
        {
            exceptionCaught(ctx, t, null);
        }

        if (log.isDebugEnabled() && channel.remoteAddress() != null)
        {
            log.debug("channel created: '{}'", channel.remoteAddress().toString());
        }

        // Get the SslHandler in the current pipeline.
        // We added it in SecureChatPipelineFactory.
        final SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);

        // Get notified when SSL handshake is done.
        if (sslHandler != null)
        {
            Future handshakeFuture = sslHandler.handshakeFuture();
            handshakeFuture.addListener(new ChannelFutureListener()
            {
                @Override
                public void operationComplete(ChannelFuture cf) throws Exception
                {
                    Channel channel = cf.channel();

                    if (cf.isSuccess())
                    {
                        log.info("SSL handshake complete.");
                    }
                    else
                    {


                        if (channel == null)
                        {
                            log.info("SSL handshake failled.");
                            return;
                        }

                        try
                        {
                            String remoteClient = channel.remoteAddress() != null ? channel.remoteAddress().toString() : "(unknown client)";
                            log.info("SSL handshake failled, client: '{}'", remoteClient);

                            // String client = channel.

                            // Publish fault message
                            NetFault fault = new NetFault("CODE:99999", "SSL handshake failled");
                            fault.setActionId("99999");

                            NetAction action = new NetAction(ActionType.FAULT);
                            action.setFaultMessage(fault);

                            NetMessage ex_msg = new NetMessage(action, null);

                            if (channel.isActive())
                            {
                                channel.writeAndFlush(ex_msg).addListener(ChannelFutureListener.CLOSE);
                            }
                        }
                        catch (Throwable t)
                        {
                            log.error("Error writing 'SSL handshake failled' message");
                        }
                    }
                }
            });
            MiscStats.newSslConnection();
        }
        else
        {
            int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
            if (port == GcsInfo.getBrokerPort())
            {
                MiscStats.newTcpConnection();
            }
            else if (port == GcsInfo.getBrokerLegacyPort())
            {
                MiscStats.newTcpLegacyConnection();
            }
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        handleChannelClosed(ctx);
    }


	private void handleChannelClosed(ChannelHandlerContext ctx)
	{
		Channel channel = ctx.channel();
		try
		{
			String remoteClient = channel.remoteAddress().toString();
			QueueProcessorList.removeSession(ctx);
			TopicProcessorList.removeSession(ctx);
			BrokerSyncConsumer.removeSession(ctx);

			ListenerChannelFactory.channelClosed(channel);

			final SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
			if (sslHandler == null)
			{
				int port = ((InetSocketAddress) channel.localAddress()).getPort();
				if (port == GcsInfo.getBrokerPort())
				{
					MiscStats.tcpConnectionClosed();
				}
				else if (port == GcsInfo.getBrokerLegacyPort())
				{
					MiscStats.tcpLegacyConnectionClosed();
				}
			}
			else
			{
				MiscStats.sslConnectionClosed();
			}

			ChannelAttributes.remove(ChannelAttributes.getChannelId(ctx));

			log.info("channel closed: " + remoteClient);
			open_channels.remove(remoteClient);
		}
		catch (Throwable t)
		{
			exceptionCaught(ctx, t, null);
		}
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        exceptionCaught(ctx, cause, null);
    }


	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause, String actionId)
	{
		try
		{
			Channel channel = ctx.channel();

			Throwable rootCause = ErrorAnalyser.findRootCause(cause);

			String client = channel.remoteAddress() != null ? channel.remoteAddress().toString() : "Client unknown";

			log.error("Exception caught. Client: {} ", client, rootCause);

			CriticalErrors.exitIfCritical(rootCause);

			if (rootCause instanceof java.nio.channels.ClosedChannelException)
			{
				handleChannelClosed(ctx);
			}

			// Publish fault message
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
					if (channel.isActive())
					{
						channel.writeAndFlush(ex_msg).addListener(ChannelFutureListener.CLOSE);
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

			String msg = String.format("Client: '%s'. Message: %s", client, wtf.Cause.getMessage());

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

			MiscStats.newFault();
		}
		catch (Throwable t)
		{
			log.error(t.getMessage(), t);
		}
	}

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NetMessage netMessage) throws Exception {


        if (!(netMessage instanceof NetMessage))
        {
            log.error("Unknown message type,  Channel: '{}'", channelHandlerContext.channel().remoteAddress().toString());
            return;
        }


        handleMessage(channelHandlerContext, netMessage);
    }



	private void handleMessage(ChannelHandlerContext ctx, final NetMessage request)
	{
		String actionId = null;

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
			exceptionCaught(ctx, t, actionId);
		}
	}

	private void handleUnexpectedMessageType(ChannelHandlerContext ctx, NetMessage request)
	{
		Channel channel = ctx.channel();

		log.error("Unexpected message type. Channel: '{}'", channel.remoteAddress().toString());

		channel.writeAndFlush(NetFault.UnexpectedMessageTypeErrorMessage).addListener(ChannelFutureListener.CLOSE);
	}

	private void handlePublishMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		final String messageSource = MQ.requestSource(request);
		Channel channel = ctx.channel();
		NetPublish publish = request.getAction().getPublishMessage();

		String actionId = publish.getActionId();
		String destination = publish.getDestination();

		if (!isValidDestination(destination))
		{
			writeInvalidDestinationFault(channel, actionId, destination);
			return;
		}

		if (StringUtils.contains(destination, "@"))
		{
			writeInvalidDestinationFault(channel, actionId, destination);
			return;
		}

		if (request.getHeaders() != null)
		{
			publish.getMessage().addAllHeaders(request.getHeaders());
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
					channel.writeAndFlush(NetFault.MaximumNrQueuesReachedErrorMessage).addListener(ChannelFutureListener.CLOSE);
				}
				else
				{
					channel.writeAndFlush(NetFault.getMessageFaultWithActionId(NetFault.MaximumNrQueuesReachedErrorMessage, actionId)).addListener(ChannelFutureListener.CLOSE);
				}
				return;
			}
			break;
		default:
			if (actionId == null)
			{
				channel.writeAndFlush(NetFault.InvalidMessageDestinationTypeErrorMessage).addListener(ChannelFutureListener.CLOSE);
			}
			else
			{
				channel.writeAndFlush(NetFault.getMessageFaultWithActionId(NetFault.InvalidMessageDestinationTypeErrorMessage, actionId)).addListener(ChannelFutureListener.CLOSE);
			}
			return;
		}
		sendAccepted(ctx, actionId);
	}

	private void handlePollMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		NetPoll pollMsg = request.getAction().getPollMessage();
		String actionId = pollMsg.getActionId();
		String destination = pollMsg.getDestination();

		if (!isValidDestination(destination))
		{
			writeInvalidDestinationFault(ctx.channel(), actionId, destination);
			return;
		}

		if (!GlobalConfig.supportVirtualQueues() && StringUtils.contains(destination, "@"))
		{
			writeNoVirtualQueueSupportFault(ctx.channel(), actionId, destination);
			return;
		}

		String value = null;
		if (request.getHeaders() != null)
		{
			value = request.getHeaders().get(Headers.RESERVE_TIME);
		}

		sendAccepted(ctx, actionId);
		BrokerSyncConsumer.poll(pollMsg, ctx, value);
	}

	private void handleAcknowledeMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		NetAcknowledge ackReq = request.getAction().getAcknowledgeMessage();

		String actionId = ackReq.getActionId();
		String destination = ackReq.getDestination();

		if (!isValidDestination(destination))
		{
			writeInvalidDestinationFault(ctx.channel(), actionId, destination);
			return;
		}

		Gcs.ackMessage(destination, ackReq.getMessageId());
	}

	private void handleUnsubscribeMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		NetUnsubscribe unsubMsg = request.getAction().getUnsbuscribeMessage();

		String actionId = unsubMsg.getActionId();
		String destination = unsubMsg.getDestination();

		try
		{
			log.info("Unsubscribe '{}' from client '{}'", destination, ctx.channel().remoteAddress().toString());
		}
		catch (Throwable t)
		{
			log.error("Got error when logging unsubscribe request");
		}

		if (!isValidDestination(destination))
		{
			writeInvalidDestinationFault(ctx.channel(), actionId, destination);
			return;
		}

		_brokerConsumer.unsubscribe(unsubMsg, ctx);
		sendAccepted(ctx, actionId);
	}

	private void handleSubscribeMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		Channel channel = ctx.channel();
		NetSubscribe subscritption = request.getAction().getSubscribeMessage();

		String actionId = subscritption.getActionId();
		String destination = subscritption.getDestination();

		if (!isValidDestination(destination))
		{
			writeInvalidDestinationFault(channel, actionId, destination);
			return;
		}

		boolean ackRequired = true;

		if (request.getHeaders() != null)
		{
			String value = request.getHeaders().get(Headers.ACK_REQUIRED);
			ackRequired = (value == null) ? true : !value.equalsIgnoreCase("false");
		}

		switch (subscritption.getDestinationType())
		{
		case QUEUE:
			_brokerConsumer.listen(subscritption, ctx, ackRequired);
			break;
		case TOPIC:
			if (!_brokerConsumer.subscribe(subscritption, ctx))
			{
				if (subscritption.getActionId() == null)
				{
					channel.writeAndFlush(NetFault.MaximumDistinctSubscriptionsReachedErrorMessage).addListener(ChannelFutureListener.CLOSE);
				}
				else
				{
					channel.writeAndFlush(NetFault.getMessageFaultWithActionId(NetFault.MaximumDistinctSubscriptionsReachedErrorMessage, subscritption.getActionId())).addListener(ChannelFutureListener.CLOSE);
				}
				return;
			}
			break;
		case VIRTUAL_QUEUE:
			if (!GlobalConfig.supportVirtualQueues())
			{
				writeNoVirtualQueueSupportFault(channel, actionId, destination);
				return;
			}
			if (StringUtils.contains(subscritption.getDestination(), "@"))
			{
				_brokerConsumer.listen(subscritption, ctx, ackRequired);
			}
			else
			{
				writeInvalidDestinationFault(channel, actionId, destination);
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
		Channel channel = ctx.channel();
		NetPing netPing = request.getAction().getPingMessage();

		NetPong pong = new NetPong(netPing.getActionId());
		NetAction action = new NetAction(ActionType.PONG);
		action.setPongMessage(pong);
		NetMessage message = new NetMessage(action, null);
		channel.writeAndFlush(message);
	}

	private void handleAuthMessage(ChannelHandlerContext ctx, NetMessage request)
	{
		Channel channel = ctx.channel();
		InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();

		if (localAddress.getPort() != GcsInfo.getBrokerSSLPort())
		{
			channel.writeAndFlush(NetFault.InvalidAuthenticationChannelType).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		NetAuthentication netAuthentication = request.getAction().getAuthenticationMessage();

		if (StringUtils.isBlank(netAuthentication.getAuthenticationType()))
		{
			log.error("Invalid  auth type: '{}'. Channel: '{}'", netAuthentication.getAuthenticationType(), channel.remoteAddress().toString());
			channel.writeAndFlush(NetFault.UnknownAuthenticationTypeMessage).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		// // Validate client credentials
		AuthInfo info = new AuthInfo(netAuthentication.getUserId(), netAuthentication.getRoles(), netAuthentication.getToken(), netAuthentication.getAuthenticationType());

		AuthInfoValidator validator = AuthInfoVerifierFactory.getValidator(info.getUserAuthenticationType());
		if (validator == null)
		{
			log.error("Failled to obtain validator for auth type: '{}',  Channel: '{}'", netAuthentication.getAuthenticationType(), channel.remoteAddress().toString());
			channel.writeAndFlush(NetFault.UnknownAuthenticationTypeMessage).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		AuthValidationResult validateResult = null;
		try
		{
			validateResult = validator.validate(info);
		}
		catch (Exception e)
		{
			channel.writeAndFlush(NetFault.getMessageFaultWithDetail(NetFault.AuthenticationFailedErrorMessage, "Internal Error")).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		if (!validateResult.areCredentialsValid())
		{
			channel.writeAndFlush(NetFault.getMessageFaultWithDetail(NetFault.AuthenticationFailedErrorMessage, validateResult.getReasonForFailure())).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		Session plainSession = (Session) ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), "BROKER_SESSION_PROPERTIES");

		SessionProperties plainSessionProps = plainSession.getSessionProperties();
		plainSessionProps.setRoles(validateResult.getRoles());
		plainSession.updateAcl();

		if (netAuthentication.getActionId() != null)
		{
			sendAccepted(ctx, netAuthentication.getActionId());
		}
	}

	private final boolean isValidDestination(String destination)
	{
		return StringUtils.isNotBlank(destination);
	}

	private void writeInvalidDestinationFault(Channel channel, String actionId, String destinationName)
	{
		writeSubscriptionFault(channel, actionId, destinationName, NetFault.InvalidDestinationNameErrorMessage);
	}

	private void writeNoVirtualQueueSupportFault(Channel channel, String actionId, String destinationName)
	{
		writeSubscriptionFault(channel, actionId, destinationName, NetFault.NoVirtualQueueSupportErrorMessage);
	}

	private void writeSubscriptionFault(Channel channel, String actionId, String destinationName, NetMessage netFault)
	{
		log.warn("Invalid destination name: '{}',  Channel: '{}'", destinationName, channel.remoteAddress().toString());
		log.warn(String.format("%s: '%s',  Channel: '%s'", netFault.getAction().getFaultMessage().getMessage(), destinationName, channel.remoteAddress().toString()));
		if (actionId == null)
		{
			channel.writeAndFlush(netFault).addListener(ChannelFutureListener.CLOSE);
		}
		else
		{
			channel.writeAndFlush(NetFault.getMessageFaultWithActionId(netFault, actionId)).addListener(ChannelFutureListener.CLOSE);
		}
	}

	private synchronized void sendAccepted(ChannelHandlerContext ctx, final String actionId)
	{
		Channel channel = ctx.channel();
		if (actionId != null)
		{
			NetAccepted accept = new NetAccepted(actionId);
			NetAction action = new NetAction(NetAction.ActionType.ACCEPTED);
			action.setAcceptedMessage(accept);
			NetMessage message = new NetMessage(action, null);
			channel.writeAndFlush(message);
		}
	}
}