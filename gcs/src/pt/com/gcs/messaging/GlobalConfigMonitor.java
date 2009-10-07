package pt.com.gcs.messaging;

import java.util.LinkedList;
import java.util.List;

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

	private static List<GlobalConfigModifiedListener> listeners = new LinkedList<GlobalConfigModifiedListener>();

	public interface GlobalConfigModifiedListener
	{
		void globalConfigModified();
	}

	@Override
	public void run()
	{

		log.debug("Checking world map file for modifications.");

		if (GlobalConfig.reload())
		{
			Gcs.reloadWorldMap();
			fireGlobalConfigModified();
		}
	}

	public synchronized static void addGlobalConfigModifiedListener(GlobalConfigModifiedListener listener)
	{
		listeners.add(listener);
	}

	public synchronized static void removeGlobalConfigModifiedListener(GlobalConfigModifiedListener listener)
	{
		listeners.remove(listener);
	}

	private synchronized static void fireGlobalConfigModified()
	{
		for (GlobalConfigModifiedListener listener : listeners)
		{
			try
			{
				listener.globalConfigModified();
			}
			catch (Throwable t)
			{
				log.error("A GlobalConfigModifiedListner failed.", t);
			}
		}
	}
}
