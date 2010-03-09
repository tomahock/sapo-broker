package pt.com.broker.jsbridge;

import org.caudexorigo.http.netty.NettyHttpServer;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSBridgeServer
{
	private static final Logger log = LoggerFactory.getLogger(JSBridgeServer.class);

	public static void main(String[] args)
	{
		log.info(" Javascript-Bridge Starting");

		// Configuration initialization
		ConfigurationInfo.init();

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		int port = ConfigurationInfo.getPort();
		String root_directory = ConfigurationInfo.getRootDirectory();

		NettyHttpServer server = new NettyHttpServer(root_directory, true);
		server.setPort(port);
		server.setRouter(new JSBridgeRouter(root_directory));
		server.setWebSocketHandler(new JSBridgeHandler());
		server.start();
	}
}
