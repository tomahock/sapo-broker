package pt.com.gcs.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GlobalConfig;

/**
 * GlobalConfigMonitor is an runnable type responsible for triggering the validation of exiting peers and connect to new ones based on the global configuration file.  
 *
 */

public class GlobalConfigMonitor implements Runnable
{

	private static final Logger log = LoggerFactory.getLogger(GlobalConfigMonitor.class);

	@Override
	public void run()
	{
		log.debug("Checking world map file for modifications.");

		if (GlobalConfig.reload())
		{
			Gcs.reloadWorldMap();
		}
	}

}
