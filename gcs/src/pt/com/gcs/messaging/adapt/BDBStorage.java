package pt.com.gcs.messaging.adapt;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.messaging.InternalMessage;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;

public class BDBStorage
{
	private static Logger log = LoggerFactory.getLogger(BDBStorage.class);

	private Environment env;

	private Database messageDb;

	private String primaryDbName;

	private QueueProcessor queueProcessor;

	private final AtomicBoolean isMarkedForDeletion = new AtomicBoolean(false);

	private Object mutex = new Object();

	private Object dbLock = new Object();

	private Queue<Message> _syncConsumerQueue = new ConcurrentLinkedQueue<Message>();

	private static final int RETRY_THRESHOLD = 2 * 60 * 1000; // 2minutes

	private long redelivery_time = System.currentTimeMillis() + RETRY_THRESHOLD;

	public BDBStorage(QueueProcessor qp, Environment environment)
	{
		try
		{
			if (isMarkedForDeletion.get())
				return;
			queueProcessor = qp;
			primaryDbName = queueProcessor.getDestinationName();

			env = environment;

			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setTransactional(false);
			dbConfig.setAllowCreate(true);
			dbConfig.setSortedDuplicates(false);
			dbConfig.setBtreeComparator(BDBMessageComparator.class);
			messageDb = env.openDatabase(null, primaryDbName, dbConfig);

			log.info("Storage for queue '{}' is ready.", queueProcessor.getDestinationName());
		}
		catch (Throwable t)
		{
			dealWithError(t, false);
			Shutdown.now();
		}
	}

	private DatabaseEntry buildDatabaseEntry(BDBMessage bdbm) throws IOException
	{
		DatabaseEntry data = new DatabaseEntry();

		UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		bdbm.writeExternal(oout);
		oout.flush();

		ByteArrayBinding bab = new ByteArrayBinding();
		byte[] bdata = bout.toByteArray();

		bab.objectToEntry(bdata, data);

		return data;
	}

	private void closeDatabase(Database db)
	{
		try
		{
			BDBEnviroment.sync(env);
			String dbName = db.getDatabaseName();
			log.info("Try to close db '{}'", dbName);
			db.close();
			log.info("Closed db '{}'", dbName);
		}
		catch (Throwable t)
		{
			dealWithError(t, false);
		}
	}

	private void closeDbCursor(Cursor msg_cursor)
	{
		if (msg_cursor != null)
		{
			try
			{
				msg_cursor.close();
			}
			catch (Throwable t)
			{
				dealWithError(t, false);
			}
		}
	}

	private void cursorDelete(Cursor msg_cursor) throws DatabaseException
	{
		msg_cursor.delete();
		queueProcessor.decrementQueuedMessagesCount();
	}

	private void dealWithError(Throwable t, boolean rethrow)
	{
		Throwable rt = ErrorAnalyser.findRootCause(t);
		log.error(rt.getMessage(), rt);
		ErrorAnalyser.exitIfOOM(rt);
		if (rethrow)
		{
			throw new RuntimeException(rt);
		}
	}

	private void dumpMessage(final Message msg)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Could not deliver message for '{}'. Dump: {}", queueProcessor.getDestinationName(), msg.toString());
		}
	}

	private void removeDatabase(String dbName)
	{
		int retryCount = 0;

		while (retryCount < 5)
		{
			try
			{
				BDBEnviroment.sync(env);
				log.info("Try to remove db '{}'", dbName);
				env.truncateDatabase(null, dbName, false);
				env.removeDatabase(null, dbName);
				BDBEnviroment.sync(env);
				log.info("Storage for queue '{}' was removed", queueProcessor.getDestinationName());

				break;
			}
			catch (Throwable t)
			{
				retryCount++;
				log.error(t.getMessage());
				Sleep.time(2500);
			}
		}
	}

	protected long count()
	{
		if (isMarkedForDeletion.get())
			return 0;

		try
		{
			return messageDb.count();
		}
		catch (DatabaseException e)
		{
			dealWithError(e, false);
			return 0;
		}
	}

	protected boolean deleteMessage(String msgId)
	{
		if (isMarkedForDeletion.get())
			return false;

		DatabaseEntry key = new DatabaseEntry();
		long k = Long.parseLong(msgId.substring(33));
		LongBinding.longToEntry(k, key);

		try
		{
			OperationStatus op = messageDb.delete(null, key);

			if (op.equals(OperationStatus.SUCCESS))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (DatabaseException e)
		{
			dealWithError(e, true);
			return false;
		}
	}

	public void deleteQueue()
	{
		isMarkedForDeletion.set(true);
		//Sleep.time(2500);
		closeDatabase(messageDb);
		removeDatabase(primaryDbName);
	}

	public long getLastSequenceValue()
	{
		if (isMarkedForDeletion.get())
			return 0L;

		Cursor msg_cursor = null;
		long seqValue = 0L;

		try
		{
			msg_cursor = messageDb.openCursor(null, null);

			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();

			msg_cursor.getLast(key, data, null);

			seqValue = LongBinding.entryToLong(key);
		}
		catch (Throwable t)
		{
			dealWithError(t, false);
		}
		finally
		{
			closeDbCursor(msg_cursor);
		}
		return seqValue;
	}

	protected void insert(Message msg, long sequence, int deliveryCount, boolean preferLocalConsumer)
	{
		if (isMarkedForDeletion.get())
			return;

		try
		{
			BDBMessage bdbm = new BDBMessage(msg, sequence, deliveryCount, preferLocalConsumer);

			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = buildDatabaseEntry(bdbm);

			LongBinding.longToEntry(sequence, key);

			messageDb.put(null, key, data);
		}
		catch (Throwable t)
		{
			dealWithError(t, true);
		}

	}

	protected Message poll()
	{
		if (isMarkedForDeletion.get())
			return null;

		synchronized (mutex)
		{
			if (!_syncConsumerQueue.isEmpty())
			{
				log.debug("Poll Memory Queue is not empty.");

				Message pmsg = _syncConsumerQueue.poll();

				if (pmsg.getExpiration() > System.currentTimeMillis())
				{
					return pmsg;
				}
				else
				{
					deleteMessage(pmsg.getMessageId());
					log.warn("Expired message: '{}' id: '{}'", pmsg.getDestination(), pmsg.getMessageId());
					dumpMessage(pmsg);
					return poll();
				}
			}
			else
			{
				log.debug("Poll Memory Queue is empty, fill from storage.");
				
				synchronized (dbLock)
				{
					Cursor msg_cursor = null;

					try
					{
						msg_cursor = messageDb.openCursor(null, null);

						DatabaseEntry key = new DatabaseEntry();
						DatabaseEntry data = new DatabaseEntry();

						int counter = 0;
						int counter_reserved = 0;
						
						while ((msg_cursor.getNext(key, data, null) == OperationStatus.SUCCESS) && counter < 250)
						{
							byte[] bdata = data.getData();
							BDBMessage bdbm = BDBMessage.fromByteArray(bdata);
							final Message msg = bdbm.getMessage();
							final int deliveryCount = bdbm.getDeliveryCount();
							final long expiration = msg.getExpiration();
							final long poll_reserve_timeout = bdbm.getPollReserveTimeout();
							final long now = System.currentTimeMillis();
							final boolean isReserved = (poll_reserve_timeout > now);
							final boolean safeForPolling = (!isReserved || (deliveryCount == 0));

							if (now > expiration)
							{
								cursorDelete(msg_cursor);
								log.warn("Expired message: '{}' id: '{}'", msg.getDestination(), msg.getMessageId());
								dumpMessage(msg);
							}
							else
							{
								if (safeForPolling)
								{
									long k = LongBinding.entryToLong(key);
									msg.setMessageId(Message.getBaseMessageId() + k);
									bdbm.setDeliveryCount(deliveryCount + 1);
									bdbm.setPollReserveTimeout(now + 900000); // 15 minutes
									msg_cursor.put(key, buildDatabaseEntry(bdbm));

									_syncConsumerQueue.offer(msg);
									counter++;
								}
								else
								{
									counter_reserved++;
								}
							}

							if (log.isDebugEnabled())
							{
								log.debug(String.format("poll_reserve_timeout: '%s'; now: '%s'; isReserved: '%s'; deliveryCount: '%s'; safeForPolling: '%s''", poll_reserve_timeout, now, isReserved, deliveryCount, safeForPolling));
							}
						}

						if (log.isDebugEnabled())
						{
							log.debug(String.format("counter: '%s; counter_reserved: '%s''", counter, counter_reserved));
						}
					}
					catch (Throwable t)
					{
						dealWithError(t, false);
					}
					finally
					{
						closeDbCursor(msg_cursor);
					}
				}

				return _syncConsumerQueue.poll();
			}
		}
	}

	protected void recoverMessages()
	{
		synchronized (dbLock)
		{
			Cursor msg_cursor = null;
		
			
			try
			{
				msg_cursor = messageDb.openCursor(null, null);

				DatabaseEntry key = new DatabaseEntry();
				DatabaseEntry data = new DatabaseEntry();
				
				while (msg_cursor.getNext(key, data, null) == OperationStatus.SUCCESS)
				{
					byte[] bdata = data.getData();
					BDBMessage bdbm = BDBMessage.fromByteArray(bdata);
					final Message msg = bdbm.getMessage();
					long k = LongBinding.entryToLong(key);
					msg.setMessageId(Message.getBaseMessageId() + k);
					
					InternalMessage internalMsg = MessageTranslator.translate(msg);
					
					pt.com.gcs.messaging.QueueProcessor newQueueProcessor = pt.com.gcs.messaging.QueueProcessorList.get(msg.getDestination());
					long reserveTimeout = 2 * 60 * 1000;
					if(bdbm.getPollReserveTimeout() != 0)
						reserveTimeout = bdbm.getPollReserveTimeout(); 
					
					if( env  == BDBEnviroment.getLastChangedDatabaseEnv())
					{
						newQueueProcessor.getStorage().insert(internalMsg, k, false, reserveTimeout);
					}
					else
					{
						// Storing message from "not current env"
						newQueueProcessor.store(internalMsg, false);
					}
				}
			}
			catch (Throwable t)
			{
				dealWithError(t, false);
			}
			finally
			{
				closeDbCursor(msg_cursor);
			}
		}

	}
}
