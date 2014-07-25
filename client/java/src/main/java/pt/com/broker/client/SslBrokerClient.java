package pt.com.broker.client;

import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.client.messaging.PendingAcceptRequestsManager;
import pt.com.broker.client.utils.BlockingMessageAcceptedListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

public final class SslBrokerClient extends BaseBrokerClient
{
	private static final Logger log = LoggerFactory.getLogger(SslBrokerClient.class);

	private volatile boolean requiresAuthentication = false;
	protected AuthInfo userCredentials;
	private final SSLContext sslContext;

	private CredentialsProvider credentialsProvider;

	public SslBrokerClient(String hostname, int portNumber) throws Throwable
	{
		this(hostname, portNumber, "BrokerClient", NetProtocolType.PROTOCOL_BUFFER);
	}

	public SslBrokerClient(String hostname, int portNumber, String appName, NetProtocolType ptype) throws Throwable
	{
		this(hostname, portNumber, appName, HostInfo.DEFAULT_CONNECT_TIMEOUT, HostInfo.DEFAULT_READ_TIMEOUT, ptype, getDefaultSslContext());
	}

	public SslBrokerClient(String hostname, int portNumber, String appName, NetProtocolType ptype, SSLContext sslContext) throws Throwable
	{
		this(hostname, portNumber, appName, HostInfo.DEFAULT_CONNECT_TIMEOUT, HostInfo.DEFAULT_READ_TIMEOUT, ptype, sslContext);
	}

	public SslBrokerClient(String hostname, int portNumber, String appName, int connectTimeout, int readTimeout, NetProtocolType ptype, SSLContext sslContext) throws Throwable
	{
		super(hostname, portNumber, connectTimeout, readTimeout, appName, ptype);
		this.sslContext = sslContext;
		init();
	}

	public SslBrokerClient(Collection<HostInfo> hosts, String appName, NetProtocolType ptype) throws Throwable
	{
		this(hosts, appName, ptype, getDefaultSslContext());
	}

	public SslBrokerClient(Collection<HostInfo> hosts, String appName, NetProtocolType ptype, SSLContext sslContext) throws Throwable
	{
		super(hosts, appName, ptype);
		this.sslContext = sslContext;
		init();
	}

	public void setCredentialsProvider(CredentialsProvider credentialsProvider)
	{
		this.credentialsProvider = credentialsProvider;
	}

	public boolean authenticateClient() throws Throwable
	{
		if (this.credentialsProvider == null)
		{
			throw new IllegalStateException("Mandatory Credential Provider missing.");
		}

		this.requiresAuthentication = true;

		setState(BrokerClientState.AUTH);

		this.userCredentials = credentialsProvider.getCredentials();

		NetAuthentication clientAuth = new NetAuthentication(userCredentials.getToken(), userCredentials.getUserAuthenticationType());
		if (userCredentials.getRoles() != null && userCredentials.getRoles().size() != 0)
			clientAuth.setRoles(userCredentials.getRoles());

		if (userCredentials.getUserId() != null)
			clientAuth.setUserId(userCredentials.getUserId());

		NetAction action = new NetAction(ActionType.AUTH);
		action.setAuthenticationMessage(clientAuth);

		NetMessage msg = new NetMessage(action);

		Object syncObj = new Object();
		BlockingMessageAcceptedListener acceptedListener = new BlockingMessageAcceptedListener(syncObj);
		String actionId = RandomStringUtils.randomAlphabetic(25);
		clientAuth.setActionId(actionId);

		AcceptRequest acceptRequest = new AcceptRequest(actionId, acceptedListener, 10000);
		PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);

		getNetHandler().sendMessage(msg);
		synchronized (syncObj)
		{
			syncObj.wait();
		}

		if (acceptedListener.wasFailure())
		{
			log.error(String.format("Authentication failed: %s", acceptedListener.getFault().getMessage()));
			setState(BrokerClientState.OK);
			PendingAcceptRequestsManager.removeRequest(actionId);
			return false;
		}
		else if (acceptedListener.wasTimeout())
		{
			log.warn("Authentication failed by timeout.");
			PendingAcceptRequestsManager.removeRequest(actionId);
			setState(BrokerClientState.OK);
			return false;
		}
		setState(BrokerClientState.OK);
		return true;
	}

	public boolean isAuthenticationRequired()
	{
		return requiresAuthentication;
	}

	public SSLSession getSSLSession()
	{
		return ((SslNetworkConnector) this.getNetHandler().getConnector()).getSSLSession();
	}

	@Override
	protected BrokerProtocolHandler getBrokerProtocolHandler() throws Throwable
	{
		BrokerProtocolHandler brokerProtocolHandler;

		SslNetworkConnector networkConnector = new SslNetworkConnector(getHostInfo(), sslContext);
		brokerProtocolHandler = new BrokerProtocolHandler(this, getProtocolType(), networkConnector, this.isOldFramming());
		networkConnector.setProtocolHandler(brokerProtocolHandler);

		return brokerProtocolHandler;
	}

	private static SSLContext getDefaultSslContext()
	{
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers()
			{
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
			{
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
			{

			}
		} };
		try
		{
			SSLContext sc = SSLContext.getInstance("SSLv3");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			return sc;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
}