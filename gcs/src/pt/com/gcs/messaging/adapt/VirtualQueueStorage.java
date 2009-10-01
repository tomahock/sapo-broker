package pt.com.gcs.messaging.adapt;

import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.cryto.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

public class VirtualQueueStorage
{
	private static Logger log = LoggerFactory.getLogger(BDBStorage.class);

	private static final VirtualQueueStorage instance = new VirtualQueueStorage();

	private List<Environment> envs;
	private List<Database> vqStorages = new ArrayList<Database>();
	
	//private Database vqStorage;

	private String dbName;

	private VirtualQueueStorage()
	{
		try
		{
			envs= BDBEnviroment.getAllEnv();
			dbName = MD5.getHashString("VirtualQueueStorage");

			for(Environment env : envs)
			{
				DatabaseConfig dbConfig = new DatabaseConfig();
				dbConfig.setTransactional(true);
				dbConfig.setAllowCreate(true);
				dbConfig.setSortedDuplicates(false);
				Database vqStorage = env.openDatabase(null, dbName, dbConfig);
				vqStorages.add(vqStorage);
			}

			log.info("VirtualQueueStorage is ready.");
		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			log.error(rt.getMessage(), rt);
			Shutdown.now();
		}
	}

//	private void i_saveVirtualQueue(String queueName)
//	{
//		try
//		{
//			DatabaseEntry key = new DatabaseEntry();
//			DatabaseEntry data = new DatabaseEntry();
//
//			StringBinding.stringToEntry(queueName, key);
//			StringBinding.stringToEntry(queueName, data);
//
//			Transaction txn = env.beginTransaction(null, null);
//			vqStorage.put(txn, key, data);
//			txn.commitSync();
//		}
//		catch (Throwable t)
//		{
//			Throwable rt = ErrorAnalyser.findRootCause(t);
//			log.error(rt.getMessage(), rt);
//		}
//
//	}

	private void i_deleteVirtualQueue(String queueName)
	{
		List<QueueInfo> vqInfo = iGetVirtualQueue(queueName);
		
		try
		{
			for(QueueInfo qInfo : vqInfo){
			
				DatabaseEntry key = new DatabaseEntry();
		
				StringBinding.stringToEntry(queueName, key);
		
				Transaction txn = qInfo.env.beginTransaction(null, null);
				qInfo.storage.delete(txn, key);
				txn.commitSync();
			}
			
			
		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			log.error(rt.getMessage(), rt);
		}
	}

//	public synchronized static void saveVirtualQueue(String queueName)
//	{
//		instance.i_saveVirtualQueue(queueName);
//	}

	public static void deleteVirtualQueue(String queueName)
	{
		instance.i_deleteVirtualQueue(queueName);
	}
	
	public static class QueueInfo
	{
		Environment env;
		Database storage;
		QueueInfo(Environment env, Database storage)
		{
			this.env = env;
			this.storage = storage;			
		}
	}
	
	public List<QueueInfo> iGetVirtualQueue(String queueName)
	{
		List<QueueInfo> info = new ArrayList<QueueInfo>(); 
		
		Cursor cursor = null;
		try
		{
			int index = 0;
			
			for(Database vqStor : vqStorages)
			{
				cursor = vqStor.openCursor(null, null);
	
				DatabaseEntry key = new DatabaseEntry();
				DatabaseEntry data = new DatabaseEntry();
	
				while (cursor.getNext(key, data, null) == OperationStatus.SUCCESS)
				{
					String qname = StringBinding.entryToString(data);
					if( qname.equals(queueName) )
					{
						info.add(new QueueInfo(envs.get(index), vqStor));
					}
				}
				cursor.close();
				++index;
			}
		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			log.error(rt.getMessage(), rt);
			
			if (cursor != null)
			{
				try
				{
					cursor.close();
				}
				catch (Throwable t2)
				{
					rt = ErrorAnalyser.findRootCause(t2);
					log.error(rt.getMessage(), rt);
				}
			}
			
			return info;
		}

		return info;
	}

	public String[] i_getVirtualQueueNames()
	{
		Cursor cursor = null;
		ArrayList<String> lst = new ArrayList<String>();
		try
		{
			for(Database vqStor : vqStorages)
			{
			
				cursor = vqStor.openCursor(null, null);
	
				DatabaseEntry key = new DatabaseEntry();
				DatabaseEntry data = new DatabaseEntry();
	
				while (cursor.getNext(key, data, null) == OperationStatus.SUCCESS)
				{
	
					String qname = StringBinding.entryToString(data);
					lst.add(qname);
				}
				cursor.close();
			}

			
		}
		catch (Throwable t)
		{
			Throwable rt = ErrorAnalyser.findRootCause(t);
			log.error(rt.getMessage(), rt);
			return new String[0];
		}
		
		return lst.toArray(new String[lst.size()]);
	}

	public static String[] getVirtualQueueNames()
	{
		return instance.i_getVirtualQueueNames();
	}
}
