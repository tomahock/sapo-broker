package pt.com.broker.monitorization.http;

import org.caudexorigo.http.netty.NettyHttpServer;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collector.CollectorManager;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;

public class HttpMonitorizationServer
{

	private static final Logger log = LoggerFactory.getLogger(HttpMonitorizationServer.class);

	public static void main(String[] args)
	{
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		log.info("Starting Sapo-Broker HTTP Monitorization Server...");

		ConfigurationInfo.init();
		CollectorManager.init();

		int port = ConfigurationInfo.getConsoleHttpPort();
		String host = "0.0.0.0";

		NettyHttpServer server = new NettyHttpServer("./wwwroot/");
		server.setPort(port);
		server.setHost(host);

		server.setRouter(new ActionRouter());

		server.start();
		log.info("Monitorization Console is accessible at 'http://localhost:{}/main.html'", port + "");
	}
}
