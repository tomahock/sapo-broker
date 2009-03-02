package pt.com.broker;

import org.apache.mina.util.ExceptionMonitor;
import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.broker.core.BrokerServer;
import pt.com.broker.core.ErrorHandler;
import pt.com.broker.core.FilePublisher;
import pt.com.broker.core.UdpService;
import pt.com.broker.http.BrokerHttpService;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;

public class Start
{
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
			Class.forName("org.codehaus.wool.stax.InputFactoryImpl").newInstance();
			Class.forName("org.codehaus.wool.stax.OutputFactoryImpl").newInstance();
			Class.forName("org.codehaus.wool.stax.EventFactoryImpl").newInstance();

			// If we made it here without errors set Aalto as our StaX parser
			System.setProperty("javax.xml.stream.XMLInputFactory", "org.codehaus.wool.stax.InputFactoryImpl");
			System.setProperty("javax.xml.stream.XMLOutputFactory", "org.codehaus.wool.stax.OutputFactoryImpl");
			System.setProperty("javax.xml.stream.XMLEventFactory", "org.codehaus.wool.stax.EventFactoryImpl");
		}
		catch (Throwable t)
		{
			log.warn("Aalto was not found in the classpath, will fallback to use the native parser");
		}

		ExceptionMonitor.setInstance(new ErrorHandler());

		try
		{
			Gcs.init();

			int broker_port = GcsInfo.getBrokerPort();
			int broker_legacy_port = GcsInfo.getBrokerLegacyPort();
			BrokerServer broker_srv = new BrokerServer(broker_port, broker_legacy_port);
			broker_srv.start();

			int http_port = GcsInfo.getBrokerHttpPort();
			BrokerHttpService http_srv = new BrokerHttpService(http_port);
			http_srv.start();

			FilePublisher.init();

			Runnable udp_srv_runner = new Runnable()
			{
				@Override
				public void run()
				{
					UdpService udp_srv = new UdpService();
					udp_srv.start();
				}
			};

			Thread sync_hook = new Thread()
			{
				public void run()
				{
					try
					{
						log.info("Disconnect broker socket acceptor");
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

			BrokerExecutor.execute(udp_srv_runner);

		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
			Shutdown.now();
		}

	}
}
