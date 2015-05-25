package pt.com.broker.functests.samples;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.nio.AcceptRequest;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.client.nio.events.AcceptResponseListener;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetSubscribeAction;

/**
 * Created by luissantos on 05-05-2014.
 */
@Ignore()
public class BrokerSslClientTest
{

	private static final Logger log = LoggerFactory.getLogger(BrokerSslClientTest.class);

	protected SslBrokerClient createClient() throws Throwable
	{

		SslBrokerClient bk = new SslBrokerClient("192.168.100.10", 3390, NetProtocolType.JSON);

		// Load CAs from an InputStream
		// (could be from a resource or ByteArrayInputStream or ...)
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		InputStream caInput = new BufferedInputStream(new FileInputStream("/home/luissantos/Develop/sapo-broker-maven-edition/client/java-nio/teste.crt"));
		Certificate ca;
		try
		{
			ca = cf.generateCertificate(caInput);
			System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
		}
		finally
		{
			caInput.close();
		}

		// Create a KeyStore containing our trusted CAs
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		keyStore.setCertificateEntry("ca", ca);

		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);

		// Create an SSLContext that uses our TrustManager
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, tmf.getTrustManagers(), null);

		bk.setContext(context);

		return bk;

	}

	@Test
	public void testPingPong() throws Throwable
	{

		SslBrokerClient bk = createClient();

		Future f = bk.connectAsync();

		f.get();

		bk.checkStatus(new PongListenerAdapter()
		{
			@Override
			public void onMessage(NetPong message, HostInfo host)
			{

				log.debug("Got pong message");

			}

		});

		Thread.sleep(10000);

	}

	@Test
	public void testAuth() throws Throwable
	{

		SslBrokerClient bk = createClient();

		Future f = bk.connectAsync();

		f.get();

		SapoSTSProvider stsProvider = new SapoSTSProvider("username", "password", "https://pre-release.services.bk.sapo.pt/STS/");

		bk.setCredentialsProvider(stsProvider);

		Assert.assertTrue(bk.authenticateClient());

	}

	@Test
	public void testAuthAuhtorization() throws Throwable
	{

		SslBrokerClient bk = createClient();

		Future f = bk.connectAsync();

		f.get();

		SapoSTSProvider stsProvider = new SapoSTSProvider("username", "password", "https://pre-release.services.bk.sapo.pt/STS/");

		bk.setCredentialsProvider(stsProvider);

		Assert.assertTrue(bk.authenticateClient());

		NetSubscribeAction action = new NetSubscribe("/secret/.*", NetAction.DestinationType.QUEUE);

		String actionId = UUID.randomUUID().toString();

		AcceptRequest request = new AcceptRequest(actionId, new AcceptResponseListener()
		{
			@Override
			public void onMessage(NetAccepted message, HostInfo host)
			{
				System.out.println("Accepeted");
			}

			@Override
			public void onFault(NetFault fault, HostInfo host)
			{
				System.out.println("Fault");
			}

			@Override
			public void onTimeout(String actionID)
			{
				System.out.println("Timeout");
			}
		}, 10000);

		Future<HostInfo> future = bk.subscribe(action, new BrokerListener()
		{
			@Override
			public void deliverMessage(NetMessage message, HostInfo host) throws Throwable
			{

				System.out.println("Message");

			}

		}, request);

		/*
		 * bk.setFaultListener(new BrokerListener() {
		 * 
		 * @Override public void deliverMessage(NetMessage message, HostInfo host) throws Throwable {
		 * 
		 * 
		 * System.out.println(message.getAction().getFaultMessage().getMessage()); System.out.println(message.getAction().getFaultMessage().getDetail()); System.out.println(message.getAction().getFaultMessage().getCode());
		 * 
		 * } });
		 */

		future.get();

		Thread.sleep(10000);

	}
}
