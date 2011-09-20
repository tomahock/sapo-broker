package pt.com.broker.net;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.ssl.SslHandler;

import pt.com.broker.codec.BrokerDecoderRouter;
import pt.com.broker.codec.BrokerEncoderRouter;
import pt.com.gcs.conf.GcsInfo;

public class BrokerSslPipelineFactory implements ChannelPipelineFactory
{
	public ChannelPipeline getPipeline() throws Exception
	{
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		SSLContext sslContext = getSSLContext();
		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		SslHandler sslHandler = new SslHandler(sslEngine);

		pipeline.addLast("ssl", sslHandler);

		pipeline.addLast("broker-encoder", new BrokerEncoderRouter());

		pipeline.addLast("broker-decoder", new BrokerDecoderRouter(GcsInfo.getMessageMaxSize()));

		if (GcsInfo.useAccessControl())
		{
			pipeline.addLast("broker-auth-filter", new AuthorizationFilter());
		}

		pipeline.addLast("broker-handler", BrokerProtocolHandler.getInstance());
		return pipeline;
	}

	private javax.net.ssl.SSLContext getSSLContext() throws Exception
	{

		KeyStore keyStore = KeyStore.getInstance("JKS");

		String keyStoreLocation = GcsInfo.getKeystoreLocation();
		if (keyStoreLocation == null)
		{
			// Deal with this gracefully
			return null;
		}

		String keyStorePasswordStr = GcsInfo.getKeystorePassword();
		if (keyStorePasswordStr == null)
		{
			// Deal with this gracefully
			return null;
		}
		String keyPasswordStr = GcsInfo.getKeyPassword();
		if (keyPasswordStr == null)
		{
			// Deal with this gracefully
			return null;
		}

		char[] KEYSTOREPW = keyStorePasswordStr.toCharArray();
		char[] KEYPW = keyPasswordStr.toCharArray();

		keyStore.load(new FileInputStream(keyStoreLocation), KEYSTOREPW);

		javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance("SunX509");

		kmf.init(keyStore, KEYPW);

		javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("SSLv3");

		sslContext.init(kmf.getKeyManagers(), null, null);

		return sslContext;
	}
}
