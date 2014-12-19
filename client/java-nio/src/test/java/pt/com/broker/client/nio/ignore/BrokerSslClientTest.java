package pt.com.broker.client.nio.ignore;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.ProviderInfo;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 05-05-2014.
 */
public class BrokerSslClientTest {

	private static final Logger log = LoggerFactory
			.getLogger(BrokerSslClientTest.class);

	protected SslBrokerClient createClient() throws Throwable {

		SslBrokerClient bk = new SslBrokerClient("127.0.0.1", 3390,
				NetProtocolType.JSON);

		// Load CAs from an InputStream
		// (could be from a resource or ByteArrayInputStream or ...)
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		// InputStream caInput = new BufferedInputStream(new
		// FileInputStream("/home/luissantos/Develop/sapo-broker-maven-edition/client/java-nio/teste.crt"));
		InputStream caInput = new BufferedInputStream(getClass()
				.getClassLoader().getResourceAsStream("myservercert.cer"));
		Certificate ca;
		try {
			ca = cf.generateCertificate(caInput);
			System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
		} finally {
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
	public void testPingPong() throws Throwable {
		SslBrokerClient bk = createClient();
		Future f = bk.connectAsync();
		f.get();
		bk.checkStatus(new PongListenerAdapter() {
			@Override
			public void onMessage(NetPong message, HostInfo host) {
				log.debug("Got pong message");
			}
		});
		Thread.sleep(10000);

	}

}
