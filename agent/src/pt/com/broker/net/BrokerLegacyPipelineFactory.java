package pt.com.broker.net;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import pt.com.broker.codec.xml.SoapDecoder;
import pt.com.broker.codec.xml.SoapEncoder;
import pt.com.broker.types.SimpleFramingDecoder;
import pt.com.broker.types.SimpleFramingEncoder;
import pt.com.gcs.conf.GcsInfo;

public class BrokerLegacyPipelineFactory implements ChannelPipelineFactory
{

	@Override
	public ChannelPipeline getPipeline() throws Exception
	{
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		// Uncomment the following line if you want HTTPS
		// SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
		// engine.setUseClientMode(false);
		// pipeline.addLast("ssl", new SslHandler(engine));

		pipeline.addLast("broker-legacy-framing-decoder", new SimpleFramingDecoder());
		pipeline.addLast("broker-legacy-xml-decoder", new SoapDecoder());

		if (GcsInfo.useAccessControl())
		{
			pipeline.addLast("broker-auth-filter", new AuthorizationFilter());
		}

		pipeline.addLast("broker-legacy-framing-encoder", new SimpleFramingEncoder());
		pipeline.addLast("broker-legacy-xml-encoder", new SoapEncoder());

		pipeline.addLast("broker-handler", BrokerProtocolHandler.getInstance());
		return pipeline;
	}

}
