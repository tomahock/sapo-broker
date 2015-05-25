package pt.com.broker;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.ServiceLoader;

import org.apache.commons.lang3.StringUtils;
import org.caudexorigo.Shutdown;
import org.caudexorigo.netty.DefaultNettyContext;
import org.caudexorigo.netty.NettyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.ProvidersLoader;
import pt.com.broker.core.BrokerInfo;
import pt.com.broker.core.BrokerSSLServer;
import pt.com.broker.core.BrokerServer;
import pt.com.broker.core.BrokerUdpServer;
import pt.com.broker.http.BrokerHttpService;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.plugins.AgentPlugin;

/**
 * Main class for Sapo-Broker agents.
 */
public class Start
{

	public static final String AGENT_CONFIG_PATH_PROPERTY = "agent-config-path";
	public static final String GLOBAL_CONFIG_PATH_PROPERTY = "broker-global-config-path";

	private static final Logger log = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) throws Exception
	{
		start();
	}

	public static void start()
	{
		System.setProperty("file.encoding", "UTF-8");

		try
		{
			// Verify if the Aalto parser is in the classpath
			Class.forName("com.fasterxml.aalto.stax.InputFactoryImpl").newInstance();
			Class.forName("com.fasterxml.aalto.stax.OutputFactoryImpl").newInstance();
			Class.forName("com.fasterxml.aalto.stax.EventFactoryImpl").newInstance();

			// If we made it here without errors set Aalto as our StaX parser
			System.setProperty("javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
			System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
			System.setProperty("javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
		}
		catch (Throwable t)
		{
			log.warn("Aalto was not found in the classpath, will fallback to use the native parser");
		}

		try
		{
			log.info("SAPO-BROKER starting - Version: {}", BrokerInfo.getVersion());

			InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

			log.info("Before GCS init");
			Gcs.init();
			Gcs gcs = Gcs.getInstance();
			log.info("After GCS init");

			log.info("Before ProvidersLoader init");
			ProvidersLoader.init();
			log.info("After ProvidersLoader init");

			int broker_port = GcsInfo.getBrokerPort();
			int broker_legacy_port = GcsInfo.getBrokerLegacyPort();

			NettyContext nettyCtx = DefaultNettyContext.get();

			BrokerServer broker_srv = new BrokerServer(broker_port, broker_legacy_port, nettyCtx);

			broker_srv.start();

			int http_port = GcsInfo.getBrokerHttpPort();
			BrokerHttpService http_srv = new BrokerHttpService(http_port, nettyCtx);
			http_srv.start();

			if (GcsInfo.createSSLInterface())
			{
				int ssl_port = GcsInfo.getBrokerSSLPort();
				BrokerSSLServer ssl_svr = new BrokerSSLServer(ssl_port, nettyCtx);
				ssl_svr.start();
			}

			int udp_legacy_port = GcsInfo.getBrokerUdpPort();
			int udp_bin_port = broker_port;
			BrokerUdpServer udp_srv = new BrokerUdpServer(udp_legacy_port, udp_bin_port, nettyCtx);
			udp_srv.start();

			ServiceLoader<AgentPlugin> implementation = ServiceLoader.load(AgentPlugin.class);

			for (AgentPlugin impl : implementation)
			{
				log.info("Loading plugin: " + impl);
				impl.start(gcs);
			}

			Thread sync_hook = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						Gcs.destroy();
						log.info("Shutdown hook thread ended!");
					}
					catch (Throwable te)
					{
						log.error(te.getMessage(), te);
					}
				}
			};

			Runtime.getRuntime().addShutdownHook(sync_hook);
		}
		catch (Throwable t)
		{
			log.debug("Error", t);
			Shutdown.now(t);
		}
	}

	private static ByteBufAllocator getAllocator()
	{
		String os_arch = System.getProperty("os.arch");

		boolean isARM = StringUtils.contains(os_arch, "arm");

		if (isARM)
		{
			return new UnpooledByteBufAllocator(false);
		}
		else
		{
			return new PooledByteBufAllocator(true);
		}
	}
}
