package pt.com.broker.client.nio.ignore;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Future;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.Certificate;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;

public class BrokerSslKeystoreClientTest {
	
	private static final Logger log = LoggerFactory.getLogger(BrokerSslKeystoreClientTest.class);
	
	private SslBrokerClient client;
	
	@Before
	public void setup() throws Exception {
		log.debug("Setting up SSL Settings.");
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(getClass().getClassLoader().getResourceAsStream("mykeystore.jks"), "password".toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(keyStore, "jordan".toCharArray());
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		client = new SslBrokerClient("127.0.0.1", 3390, NetProtocolType.JSON);
		client.setContext(sslContext);
		client.connect();
	}
	
	@Test
	public void testPingPong() throws Throwable{
        client.checkStatus(new PongListenerAdapter() {
            @Override
            public void onMessage(NetPong message, HostInfo host) {
                log.debug("Got pong message");
            }
        });
        Thread.sleep(10000);
	}

}
