package pt.com.broker.net;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AccessControl;
import pt.com.broker.auth.AccessControl.ValidationResult;
import pt.com.broker.auth.Session;
import pt.com.broker.auth.SessionProperties;
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
@Sharable
public class AuthorizationFilter extends SimpleChannelUpstreamHandler
{
	private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelOpen(ctx, e);
		Session sessionProps;
		Channel channel = ctx.getChannel();

		if (((InetSocketAddress) channel.getLocalAddress()).getPort() == GcsInfo.getBrokerSSLPort())
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

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	{
		Channel channel = ctx.getChannel();
		Object _session = ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), "BROKER_SESSION_PROPERTIES");

		if (_session == null)
		{
			_session = new Session(channel);
		}

		NetMessage netMessage = (NetMessage) e.getMessage();
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

		ctx.sendUpstream(e);
	}

	private void messageRefused(Channel channel, NetMessage message, String reason)
	{
		if (reason == null)
		{
			channel.write(NetFault.AccessDeniedErrorMessage).addListener(ChannelFutureListener.CLOSE);
		}
		else
		{
			channel.write(NetFault.getMessageFaultWithDetail(NetFault.AccessDeniedErrorMessage, reason)).addListener(ChannelFutureListener.CLOSE);
		}
		MiscStats.newAccessDenied();
	}
}
