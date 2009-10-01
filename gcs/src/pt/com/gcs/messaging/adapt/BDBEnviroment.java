package pt.com.gcs.messaging.adapt;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.cryto.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GcsInfo;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class BDBEnviroment
{
	private static Logger log = LoggerFactory.getLogger(BDBEnviroment.class);

	private List<Environment> databaseDirs = new ArrayList<Environment>();
	private Environment env;
	
	private long lastChangedDate = 0;
	private Environment lastChangedDatabaseEnv = null;

	private String dbFile;

	private String dbName;

	private String dbDir;

	private static final BDBEnviroment instance = new BDBEnviroment();

	private BDBEnviroment()
	{
		try
		{
			dbFile = GcsInfo.getBasePersistentDirectory().concat(File.separator);
		
			File persistentDir = new File(dbFile);
			File[] dirs = persistentDir.listFiles(new FileFilter(){

				@Override
				public boolean accept(File file)
				{
					return file.isDirectory();
				}
				
			} );
			
			for(File databaseDir : dirs)
			{
				
				File databaseFile = new File(databaseDir, "00000000.jdb");
				long modified = databaseFile.lastModified();
				if( databaseFile.exists() )
				{
					EnvironmentConfig envConfig = new EnvironmentConfig();
					envConfig.setAllowCreate(true);
					envConfig.setTransactional(true);
					envConfig.setTxnWriteNoSync(true);
					envConfig.setTxnNoSync(true);
					Environment environment = new Environment(databaseDir, envConfig);
					
					databaseDirs.add( environment  );
					
					if( modified >  lastChangedDate)
					{
						lastChangedDate = modified;
						lastChangedDatabaseEnv = environment;
					}
				}
				
			}
			
//			dbName = MD5.getHashString(GcsInfo.getAgentName());
//
//			dbDir = dbFile.concat(dbName);
//			(new File(dbDir)).mkdirs();
//
//			EnvironmentConfig envConfig = new EnvironmentConfig();
//			envConfig.setAllowCreate(true);
//			envConfig.setTransactional(true);
//			envConfig.setTxnWriteNoSync(true);
//			envConfig.setTxnNoSync(true);
//			env = new Environment(new File(dbDir), envConfig);
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
		return instance.lastChangedDatabaseEnv;
	}
	
	public static List<Environment> getAllEnv()
	{
		return instance.databaseDirs;
	}

	public static void sync(Environment env2)
	{
		try
		{
			env2.sync();
			log.debug("Sync was successful");
		}
		catch (DatabaseException e)
		{
			log.error(e.getMessage(), e);
		}
	}

	public final static class DBQueue
	{
		public Environment env;
		public String queueName;
		
		public DBQueue(Environment env, String queueName)
		{
			this.env = env;
			this.queueName = queueName;			
		}
	}
	
	public static DBQueue[] getQueues()
	{
		try
		{
			List<String> in_lst;
			List<DBQueue> out_lst = new ArrayList<DBQueue>();

			for(Environment environment : instance.databaseDirs){
				in_lst = environment.getDatabaseNames();

				String nonQueue = MD5.getHashString("VirtualQueueStorage");

				for (String dbName : in_lst)
				{
					if (!dbName.equals(nonQueue))
					{
						out_lst.add(new DBQueue(environment, dbName));
					}
				}

			}
			return out_lst.toArray(new DBQueue[out_lst.size()]);

		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			log.error(rt.getMessage(), rt);
			return new DBQueue[0];
		}
	}
	
	public static String[] getQueueNames()
	{
		if (instance.lastChangedDatabaseEnv == null)
			return new String[0];
		try
		{
			List<String> in_lst;
			List<String> out_lst = new ArrayList<String>();

			in_lst = instance.lastChangedDatabaseEnv.getDatabaseNames();
			
			System.out.println();

			String nonQueue = MD5.getHashString("VirtualQueueStorage");

			for (String dbName : in_lst)
			{
				if (!dbName.equals(nonQueue) )
				{
					out_lst.add(dbName);
				}
			}

			return out_lst.toArray(new String[out_lst.size()]);

		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			log.error(rt.getMessage(), rt);
			return new String[0];
		}

	}
	
	public static Environment getLastChangedDatabaseEnv()
	{
		return instance.lastChangedDatabaseEnv;
	}
}
