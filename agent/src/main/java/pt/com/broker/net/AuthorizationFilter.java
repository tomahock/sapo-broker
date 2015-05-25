package pt.com.broker.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AccessControl;
import pt.com.broker.auth.AccessControl.ValidationResult;
import pt.com.broker.auth.Session;
import pt.com.broker.auth.SessionProperties;
import pt.com.broker.types.ActionIdDecorator;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.channels.ChannelAttributes;
import pt.com.broker.types.stats.MiscStats;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.global.ChannelType;

/**
 * AuthorizationFilter is a Netty UpstreamHandler. Its purpose is to filter unauthorized publications and subscriptions.
 * 
 */
@ChannelHandler.Sharable
public class AuthorizationFilter extends SimpleChannelInboundHandler<NetMessage>
{
	private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception
	{

		Channel channel = ctx.channel();
		Object _session = ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), "BROKER_SESSION_PROPERTIES");

		if (_session == null)
		{
			_session = new Session(channel);
		}

		NetMessage netMessage = msg;
		Session sessionProps = null;

		if (_session != null)
		{
			sessionProps = (Session) _session;
		}

		ValidationResult result = AccessControl.validate(netMessage, sessionProps);
		if (!result.accessGranted)
		{
			log.info("Message refused: '{}'", result.reasonForRejection);
			messageRefused(channel, netMessage, result.reasonForRejection);
			return;
		}

		ctx.fireChannelRead(msg);

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		super.channelActive(ctx);

		Session sessionProps;
		Channel channel = ctx.channel();

		if (((InetSocketAddress) channel.localAddress()).getPort() == GcsInfo.getBrokerSSLPort())
		{
			List<ChannelType> channelTypeList = new ArrayList<ChannelType>(3);
			channelTypeList.add(ChannelType.AUTHENTICATION);
			channelTypeList.add(ChannelType.CONFIDENTIALITY);
			channelTypeList.add(ChannelType.INTEGRITY);

			SessionProperties sp = new SessionProperties(channel);
			sp.setChannelTypes(channelTypeList);

			sessionProps = new Session(channel, sp);
		}
		else
		{
			sessionProps = new Session(channel);
		}

		ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "BROKER_SESSION_PROPERTIES", sessionProps);
	}

	private void messageRefused(Channel channel, NetMessage message, String reason)
	{

		NetMessage AccessDeniedErrorMessage = NetFault.buildNetFaultMessage("3201", "Access denied");

		ActionIdDecorator decorator = new ActionIdDecorator(message);

		AccessDeniedErrorMessage.getAction().getFaultMessage().setActionId(decorator.getActionId());

		if (reason == null)
		{
			channel.writeAndFlush(AccessDeniedErrorMessage).addListener(ChannelFutureListener.CLOSE);
		}
		else
		{
			channel.writeAndFlush(NetFault.getMessageFaultWithDetail(AccessDeniedErrorMessage, reason)).addListener(ChannelFutureListener.CLOSE);
		}

		MiscStats.newAccessDenied();
	}
}
