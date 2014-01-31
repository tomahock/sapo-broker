package pt.com.broker.monitorization;

import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collector.CollectorManager;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;

public class Collector
{

	private static final Logger log = LoggerFactory.getLogger(Collector.class);

	public static void main(String[] args)
	{

		ConfigurationInfo.init();
		CollectorManager.init();

		log.info("Starting Sapo-Broker Statistics collector");

		Sleep.time(Long.MAX_VALUE);

	}
}
