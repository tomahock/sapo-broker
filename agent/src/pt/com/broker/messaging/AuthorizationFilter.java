package pt.com.broker.messaging;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.security.Session;
import pt.com.broker.security.SessionProperties;
import pt.com.broker.security.authorization.AccessControl;
import pt.com.broker.security.authorization.AccessControl.ValidationResult;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.global.ChannelType;

public class AuthorizationFilter extends IoFilterAdapter
{

	private static final int MAX_WRITE_BUFFER_SIZE = 5000;

	private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

	private static AuthorizationFilter instance = new AuthorizationFilter();

	public static AuthorizationFilter getInstance()
	{
		return instance;
	}

	@Override
	public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception
	{
		Session sessionProps;

		if (((InetSocketAddress) session.getLocalAddress()).getPort() == GcsInfo.getBrokerSSLPort())
		{
			List<ChannelType> channelTypeList = new ArrayList<ChannelType>(3);
			channelTypeList.add(ChannelType.AUTHENTICATION);
			channelTypeList.add(ChannelType.CONFIDENTIALITY);
			channelTypeList.add(ChannelType.INTEGRITY);

			SessionProperties sp = new SessionProperties(session);
			sp.setChannelTypes(channelTypeList);

			sessionProps = new Session(session, sp);
		}
		else
		{
			sessionProps = new Session(session);
		}

		session.setAttribute("BROKER_SESSION_PROPERTIES", sessionProps);

		// TODO Auto-generated method stub
		super.sessionCreated(nextFilter, session);
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception
	{
		Object _session = session.getAttribute("BROKER_SESSION_PROPERTIES");
		NetMessage netMessage = (NetMessage) message;
		Session sessionProps = null;

		if (_session != null)
		{
			sessionProps = (Session) _session;
		}

		ValidationResult result = AccessControl.validate(netMessage, sessionProps);
		if (!result.accessGranted)
		{
			System.out.println("AuthorizationFilter.messageReceived() -- message refused " + +System.currentTimeMillis());
			messageRefused(session, netMessage, result.reasonForRejection);
			return;
		}

		// OK to proceed
		super.messageReceived(nextFilter, session, message);
	}

	private void messageRefused(final IoSession session, NetMessage message, String reason)
	{
		if (reason == null)
		{
			session.write(NetFault.AccessDeniedErrorMessage);
		}
		else
		{
			session.write(NetFault.getMessageFaultWithDetail(NetFault.AccessDeniedErrorMessage, reason));
		}
	}
}
