package pt.com.broker.monitorization.http;

import org.caudexorigo.concurrent.Sleep;
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

		ConfigurationInfo.init();
		CollectorManager.init();

		
		Sleep.time(Long.MAX_VALUE);

	}
}
