package pt.com.gcs.messaging.adapt;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.messaging.adapt.BDBEnviroment.DBQueue;

public class QueueProcessor
{
	private static Logger log = LoggerFactory.getLogger(QueueProcessor.class);

	private final String _destinationName;

	private final AtomicLong _sequence;

	private final AtomicBoolean isWorking = new AtomicBoolean(false);

	private final AtomicLong _deliverSequence = new AtomicLong(0L);

	protected final AtomicBoolean emptyQueueInfoDisplay = new AtomicBoolean(false);

	protected final AtomicLong _counter = new AtomicLong(0);

	private final BDBStorage storage;

	public QueueProcessor(DBQueue dbQueue)
	{
		if (StringUtils.isBlank(dbQueue.queueName))
		{
			throw new IllegalArgumentException(String.format("'%s' is not a valid Queue name", dbQueue.queueName));
		}

		_destinationName = dbQueue.queueName;

		storage = new BDBStorage(this, dbQueue.env);

		long cnt = getStorage().count();

		if (cnt == 0)
		{
			_sequence = new AtomicLong(0L);
			_counter.set(0);
		}
		else
		{
			_sequence = new AtomicLong(getStorage().getLastSequenceValue());
			_counter.set(cnt);
		}

		if (StringUtils.contains(dbQueue.queueName, "@"))
		{
			DispatcherList.create(dbQueue.queueName);
		}
	}

	public synchronized void clearStorage()
	{
		getStorage().deleteQueue();
	}

	public long decrementQueuedMessagesCount()
	{
		return _counter.decrementAndGet();
	}

	public long getQueuedMessagesCount()
	{
		return _counter.get();
	}

	protected void ack(final String msgId)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Ack message . MsgId: '{}'.", msgId);
		}

		if (getStorage().deleteMessage(msgId))
		{
			_counter.decrementAndGet();
		}
	}

	protected boolean forward(Message message, boolean preferLocalConsumer) throws IllegalStateException
	{
		return true;
	}

	protected String getDestinationName()
	{
		return _destinationName;
	}

	protected boolean hasRecipient()
	{
		if (size() > 0)
			return true;
		else
			return false;
	}

	protected Message poll()
	{
		return getStorage().poll();
	}

	protected int size()
	{
		return 42;
	}

	protected void store(final Message msg)
	{
		store(msg, false);
	}

	protected void store(final Message msg, boolean localConsumersOnly)
	{
		try
		{
			long seq_nr = _sequence.incrementAndGet();
			getStorage().insert(msg, seq_nr, 0, localConsumersOnly);
			_counter.incrementAndGet();
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	public final void wakeup()
	{
		long cnt = getQueuedMessagesCount();
		if (cnt > 0)
		{
			try
			{
				log.debug("Wakeup OLD queue '{}'", _destinationName);
				getStorage().recoverMessages();
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);
				throw new RuntimeException(t);
			}
			finally
			{
				isWorking.set(false);
			}
		}
	}

	public BDBStorage getStorage()
	{
		return storage;
	}

	public void setSequenceNumber(long seqNumber)
	{
		_deliverSequence.set(seqNumber);
	}
}
