package pt.com.gcs.net.ssl;

import io.netty.handler.ssl.SslContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import pt.com.gcs.conf.GcsInfo;

/**
 * Factory for generating SSLContext objects.
 * 
 * TODO: Complete the documentation.
 * */
public final class SslContextFactory
{

	public static final SSLContext getServerContext() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException
	{
		KeyStore keyStore = KeyStore.getInstance("JKS");
		String keyStoreLocation = GcsInfo.getKeystoreLocation();
		// URL keystoreUrl = BrokerSslContextFactory.class.getClass().getClassLoader().getResource(keyStoreLocation);
		String keyStorePasswordStr = GcsInfo.getKeystorePassword();
		String keyPasswordStr = GcsInfo.getKeyPassword();
		SSLContext context = SSLContext.getInstance("TLSv1");
		char[] KEYSTOREPW = keyStorePasswordStr.toCharArray();
		char[] KEYPW = keyPasswordStr != null ? keyPasswordStr.toCharArray() : null;
		File ks = new File(keyStoreLocation);
		keyStore.load(new FileInputStream(ks), KEYSTOREPW);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(keyStore, KEYPW);
		context.init(kmf.getKeyManagers(), null, null);
		return context;
	}

	public static final SslContext getServerSslContext(String certPath, String keyPath, String keyPasswd) throws SSLException
	{
		// return SslContext.newServerContext(new File("/home/bruno/Work/Broker/sapo-broker/agent/src/main/resources/conf/broker.pem"), new File("/home/bruno/Work/Broker/sapo-broker/agent/src/main/resources/conf/broker_key.pkcs8"), "e16babcf9b79bbb3bdc3b7721f0171523669f");
		return SslContext.newServerContext(new File(certPath), new File(keyPath), keyPasswd);
	}

	// TODO: Instantiate the trustmanager from the provided keystore
	public static final SslContext getClientSslContext() throws SSLException, NoSuchAlgorithmException, KeyStoreException
	{
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init((KeyStore) null);
		return SslContext.newClientContext(tmf);
	}

}
