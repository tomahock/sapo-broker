package pt.com.gcs.messaging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.cryto.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.CriticalErrors;
import pt.com.gcs.conf.GcsInfo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * BDBEnviroment class is responsible for managing the database enviroment witch includes localization and synchronization. It also makes queue names accessible.
 * 
 */

public class BDBEnviroment
{
	private static Logger log = LoggerFactory.getLogger(BDBEnviroment.class);

	private Environment env;

	private String dbFile;

	// private String dbName;

	private String dbDir;

	private static final BDBEnviroment instance = new BDBEnviroment();

	private BDBEnviroment()
	{
		try
		{
			// dbFile = GcsInfo.getBasePersistentDirectory().concat("/");
			// dbName = MD5.getHashString(GcsInfo.getAgentName());

			// dbDir = dbFile.concat(dbName);

			dbFile = GcsInfo.getBasePersistentDirectory();
			dbDir = dbFile.concat(File.separator);
			(new File(dbDir)).mkdirs();

			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			envConfig.setTransactional(true);
			// envConfig.setTxnWriteNoSync(true);
			// envConfig.setTxnNoSync(true);
			envConfig.setCachePercent(40);
			env = new Environment(new File(dbDir), envConfig);
		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			log.error(rt.getMessage(), rt);
			Shutdown.now();
		}
	}

	public static Environment get()
	{
		return instance.env;
	}

	public static void sync()
	{
		try
		{
			instance.env.sync();
			System.out.println("Sync was successful");
		}
		catch (DatabaseException e)
		{
			log.error(e.getMessage(), e);
		}
	}

	public static String[] getQueueNames()
	{
		try
		{
			List<String> in_lst;

			in_lst = instance.env.getDatabaseNames();

			List<String> out_lst = new ArrayList<String>();
			String nonQueue = MD5.getHashString("VirtualQueueStorage");

			for (String dbName : in_lst)
			{
				if (!dbName.equals(nonQueue))
				{
					out_lst.add(dbName);
				}
			}
			return out_lst.toArray(new String[out_lst.size()]);
		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			CriticalErrors.exitIfCritical(rt);
			log.error(rt.getMessage(), rt);
			return new String[0];
		}
	}
}
