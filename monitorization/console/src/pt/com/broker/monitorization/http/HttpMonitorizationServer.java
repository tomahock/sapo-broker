package pt.com.broker.monitorization.http;

import org.caudexorigo.Shutdown;
import org.caudexorigo.cli.ArgumentValidationException;
import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.http.netty.CliArgs;
import org.caudexorigo.http.netty.NettyHttpServer;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.configuration.ConfigurationInfo;

public class HttpMonitorizationServer
{

	private static final Logger log = LoggerFactory.getLogger(HttpMonitorizationServer.class);

	public static void main(String[] args)
	{
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		CliArgs cargs = null;
		try
		{
			cargs = CliFactory.parseArguments(CliArgs.class, args);
		}
		catch (ArgumentValidationException t)
		{
			Shutdown.now(t);
		}

		log.info("Starting Sapo-Broker HTTP Monitorization Server...");

		ConfigurationInfo.init();

		NettyHttpServer server = new NettyHttpServer(cargs.getRootDirectory());
		server.setPort(cargs.getPort());
		server.setHost(cargs.getHost());

		server.setRouter(new ActionRouter());

		server.start();
		log.info("Monitorization Console is accessible at 'http://localhost:{}/main.html'", cargs.getPort() + "");
	}
}
