package pt.com.broker.net;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.caudexorigo.Shutdown;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerDecoderRouter;
import pt.com.broker.codec.BrokerEncoderRouter;
import pt.com.gcs.conf.GcsInfo;

public class BrokerSslPipelineFactory implements ChannelPipelineFactory
{
	private static final Logger log = LoggerFactory.getLogger(BrokerSslPipelineFactory.class);

	private javax.net.ssl.SSLContext sslContext;

	public BrokerSslPipelineFactory()
	{
		super();

		try
		{
			KeyStore keyStore = KeyStore.getInstance("JKS");

			String keyStoreLocation = GcsInfo.getKeystoreLocation();
			if (StringUtils.isBlank(keyStoreLocation))
			{
				log.error("keystore location is blank");
				return;
			}

			String keyStorePasswordStr = GcsInfo.getKeystorePassword();
			if (StringUtils.isBlank(keyStorePasswordStr))
			{
				log.error("keystore password is blank");
				return;
			}

			String keyPasswordStr = GcsInfo.getKeyPassword();

			if (StringUtils.isBlank(keyPasswordStr))
			{
				log.error("key password is blank");
				return;
			}

			char[] KEYSTOREPW = keyStorePasswordStr.toCharArray();
			char[] KEYPW = keyPasswordStr.toCharArray();

			File ks = new File(keyStoreLocation);

			if (!ks.exists())
			{
				log.warn("Keystore file not found");
				return;
			}

			keyStore.load(new FileInputStream(keyStoreLocation), KEYSTOREPW);

			javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance("SunX509");

			kmf.init(keyStore, KEYPW);

			sslContext = javax.net.ssl.SSLContext.getInstance("SSLv3");

			sslContext.init(kmf.getKeyManagers(), null, null);
		}
		catch (Throwable t)
		{
			Shutdown.now(t);
		}
	}

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
		if (sslContext != null)
		{
			return sslContext;
		}
		else
		{
			throw new IllegalStateException("");
		}
	}
}
