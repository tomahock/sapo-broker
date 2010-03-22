package pt.com.gcs.messaging;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.gcs.conf.GcsInfo;

/**
 * QueueProcessor provides several queue related features, representing each instance a distinct queue.
 */

public class QueueProcessor
{
	private static Logger log = LoggerFactory.getLogger(QueueProcessor.class);
	private static final ForwardResult failed = new ForwardResult(Result.FAILED);

	protected final AtomicLong counter = new AtomicLong(0);

	protected final AtomicBoolean emptyQueueInfoDisplay = new AtomicBoolean(false);

	private final AtomicBoolean isWorking = new AtomicBoolean(false);

	private final CopyOnWriteArraySet<MessageListener> localQueueListeners = new CopyOnWriteArraySet<MessageListener>();

	private final String queueName;

	private final Set<MessageListener> remoteQueueListeners = new CopyOnWriteArraySet<MessageListener>();

	private final AtomicLong sequence;

	private final BDBStorage storage;

	private TopicToQueueDispatcher topicFwd;

	private int current_idx = 0;

	protected QueueProcessor(String queueName)
	{
		if (StringUtils.isBlank(queueName))
		{
			throw new IllegalArgumentException("Queue names can not be blank");
		}

		this.queueName = queueName;

		storage = new BDBStorage(this);

		long cnt = storage.count();

		if (cnt == 0)
		{
			sequence = new AtomicLong(0L);
			counter.set(0);
		}
		else
		{
			sequence = new AtomicLong(storage.getLastSequenceValue());
			counter.set(cnt);
		}

		createDispatcher();

		log.info("Create Queue Processor for '{}'.", queueName);
		log.info("Queue '{}' has {} message(s).", queueName, getQueuedMessagesCount());
	}

	protected void ack(final String msgId)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Ack message . MsgId: '{}'.", msgId);
		}

		if (storage.deleteMessage(msgId))
		{
			counter.decrementAndGet();
		}
	}

	public void add(MessageListener listener)
	{
		if (listener != null)
		{
			if (listener.getType() == MessageListener.Type.LOCAL)
			{
				addLocal(listener);
			}
			else if (listener.getType() == MessageListener.Type.REMOTE)
			{
				addRemote(listener);
			}
		}
		else
		{
			throw new IllegalArgumentException(String.format("Cannot add null listener to queue '%s'", queueName));
		}
	}

	private void addLocal(MessageListener listener)
	{
		synchronized (localQueueListeners)
		{
			if (localQueueListeners.add(listener))
			{
				broadCastNewQueueConsumer(listener);

				log.info("Add listener -> '{}'", listener.toString());
			}
		}
	}

	private void addRemote(MessageListener listener)
	{
		synchronized (remoteQueueListeners)
		{
			if (remoteQueueListeners.add(listener))
			{
				log.info("Add listener -> '{}'", listener.toString());
			}
		}
	}

	private void broadCastActionQueueConsumer(String action)
	{
		Set<Channel> sessions = Gcs.getManagedConnectorSessions();

		for (Channel channel : sessions)
		{
			try
			{
				broadCastQueueInfo(action, channel);
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);

				try
				{
					channel.close();
				}
				catch (Throwable ct)
				{
					log.error(ct.getMessage(), ct);
				}
			}
		}
	}

	private void broadCastNewQueueConsumer(MessageListener listener)
	{
		log.info("Tell all peers about new queue consumer for: '{}' in channel '{}'", queueName, listener.getChannel().getRemoteAddressAsString());
		broadCastActionQueueConsumer("CREATE");
	}

	protected void broadCastQueueInfo(String action, Channel channel)
	{
		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
		String payload = String.format(ptemplate, action, GcsInfo.getAgentName(), channel.getLocalAddress().toString(), queueName);

		InternalMessage m = new InternalMessage();
		NetBrokerMessage brkMsg;
		try
		{
			brkMsg = new NetBrokerMessage(payload.getBytes("UTF-8"));
			m.setType(MessageType.SYSTEM_QUEUE);

			m.setDestination(queueName);
			m.setContent(brkMsg);
		}
		catch (UnsupportedEncodingException e)
		{
			// This exception is never thrown because UTF-8 encoding is built-in
			// in every JVM
		}
		SystemMessagesPublisher.sendMessage(m, channel);
	}

	private void broadCastRemovedQueueConsumer(MessageListener listener)
	{
		log.info("Tell all peers about deleted queue consumer for: '{}' in channel '{}'", queueName, listener.getChannel().getRemoteAddressAsString());
		broadCastActionQueueConsumer("DELETE");
	}

	public synchronized void clearStorage()
	{
		removeDispatcher();
		storage.deleteQueue();
	}

	private void createDispatcher()
	{
		try
		{
			if (StringUtils.contains(queueName, "@"))
			{
				log.info("Get Dispatcher for: {}", queueName);

				String topicName = StringUtils.substringAfter(queueName, "@");
				topicFwd = new TopicToQueueDispatcher(null, topicName, queueName);

				VirtualQueueStorage.saveVirtualQueue(queueName);
				TopicProcessorList.get(topicName).add(topicFwd, false);
			}
		}
		catch (Throwable e)
		{
			topicFwd = null;
			throw new RuntimeException(e);
		}
	}

	public long decrementQueuedMessagesCount()
	{
		return counter.decrementAndGet();
	}

	public void deleteExpiredMessages()
	{
		if (!hasRecipient())
		{
			storage.deleteExpiredMessages();
		}

	}

	protected ForwardResult forward(InternalMessage message, boolean preferLocalConsumer) throws IllegalStateException
	{
		message.setType((MessageType.COM_QUEUE));
		boolean hasLocal = hasReadyListeners(localQueueListeners);
		boolean hasRemote = hasReadyListeners(remoteQueueListeners);
		boolean hasListeners = hasLocal || hasRemote;

		ForwardResult result = failed;

		if (!hasListeners)
		{
			return failed;
		}
		else
		{
			if (hasLocal && preferLocalConsumer)
			{
				result = notify(localQueueListeners, message);
			}
			else if (!hasLocal && hasRemote)
			{
				result = notify(remoteQueueListeners, message);
			}
			else if (hasLocal && !hasRemote)
			{
				result = notify(localQueueListeners, message);
			}
			else if (hasListeners)
			{
				long n = Math.abs(++current_idx % 2);
				if (n == 0)
					result = notify(localQueueListeners, message);
				else
					result = notify(remoteQueueListeners, message);
			}
		}

		if (log.isDebugEnabled())
		{
			log.debug("forward-> isDelivered: " + result.result.toString() + ", hasLocal: " + hasLocal + ", hasRemote: " + hasRemote + ", message.id: " + message.getMessageId());
		}
		return result;
	}

	public long getQueuedMessagesCount()
	{
		return counter.get();
	}

	public String getQueueName()
	{
		return queueName;
	}

	private boolean hasActiveListeners(Set<MessageListener> listeners)
	{
		for (MessageListener ml : listeners)
		{
			if (ml.isActive())
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasReadyListeners(Set<MessageListener> listeners)
	{
		for (MessageListener ml : listeners)
		{
			if (ml.isReady())
			{
				return true;
			}
		}

		return false;
	}

	protected boolean hasRecipient()
	{
		if (hasReadyListeners(localQueueListeners))
		{
			return true;
		}
		if (hasActiveListeners(localQueueListeners))
		{
			// it has sync consumers that are active but not ready. So, ignore remote consumers
			return false;
		}

		return hasReadyListeners(remoteQueueListeners);
	}

	public Set<MessageListener> localListeners()
	{
		return localQueueListeners;
	}

	private ForwardResult notify(Set<MessageListener> listeners, InternalMessage message)
	{

		int s = listeners.size();
		int n = Math.abs(++current_idx % s);
		NetMessage nmsg = null;
		
		try
		{
			int idx = 0;

			// first we cycle the collection and only notify the first ready listener with an "index" greater than "current_index"
			for (MessageListener ml : listeners)
			{
				++idx;
				if (idx > n)
				{
					if (ml.isReady())
					{
						if (ml.getType() == MessageListener.Type.LOCAL)
						{
							if (nmsg == null)
							{
								nmsg = Gcs.buildNotification(message, ml.getsubscriptionKey(), ml.getTargetDestinationType());
							}
							return ml.onMessage(nmsg);
						}
						else
						{
							return ml.onMessage(message);
						}
					}
				}
			}
			
			idx = 0;

			// if no ready listener was found we cycle through the collection again and try the listeners with an "index" lower than the "current index"
			for (MessageListener ml : listeners)
			{			
				++idx;
				if (idx <= n)
				{
					if (ml.isReady())
					{
						if (ml.getType() == MessageListener.Type.LOCAL)
						{
							if (nmsg == null)
							{
								nmsg = Gcs.buildNotification(message, ml.getsubscriptionKey(), ml.getTargetDestinationType());
							}
							return ml.onMessage(nmsg);
						}
						else
						{
							return ml.onMessage(message);
						}
					}
				}
				else
				{
					break;
				}
			}

			// oh well ... we tried
			return failed;
		}
		catch (Throwable t)
		{
			return failed;
		}
	}

	public Set<MessageListener> remoteListeners()
	{
		return remoteQueueListeners;
	}
	
	
	public void remove(MessageListener listener)
	{
		if (listener != null)
		{
			if (listener.getType() == MessageListener.Type.LOCAL)
			{
				synchronized (localQueueListeners)
				{
					if (localQueueListeners.remove(listener))
					{
						log.info("Removed listener -> '{}'", listener.toString());

						if (localQueueListeners.size() == 0)
						{
							broadCastRemovedQueueConsumer(listener);
						}
					}
				}
			}
			if (listener.getType() == MessageListener.Type.REMOTE)
			{
				synchronized (remoteQueueListeners)
				{
					if (remoteQueueListeners.remove(listener))
					{
						log.info("Removed listener -> '{}'", listener.toString());
					}
				}
			}
		}
		else
		{
			log.error(String.format("Cannot add null listener to queue '%s'", queueName));
		}
	}
	
	private void removeDispatcher()
	{
		try
		{
			if (topicFwd != null)
			{
				Gcs.removeAsyncConsumer(topicFwd);
				VirtualQueueStorage.deleteVirtualQueue(getQueueName());
			}
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setSequenceNumber(long seqNumber)
	{
		sequence.set(seqNumber);
	}

	public int size()
	{
		return localQueueListeners.size() + remoteQueueListeners.size();
	}

	public void store(final InternalMessage msg)
	{
		store(msg, false);
	}

	public void store(final InternalMessage msg, boolean preferLocalConsumer)
	{
		try
		{
			long seq_nr = sequence.incrementAndGet();
			storage.insert(msg, seq_nr, preferLocalConsumer);
			counter.incrementAndGet();
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	protected final void wakeup()
	{
		if (isWorking.getAndSet(true))
		{
			log.debug("Queue '{}' is running, skip wakeup", queueName);
			return;
		}
		long cnt = getQueuedMessagesCount();
		if (cnt > 0)
		{
			emptyQueueInfoDisplay.set(false);

			if (hasRecipient())
			{
				try
				{
					if (log.isDebugEnabled())
					{
						log.debug("Wakeup queue '{}'", queueName);
					}
					storage.recoverMessages();
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
			else
			{
				log.debug("Queue '{}' does not have asynchronous consumers", queueName);
			}
		}
		isWorking.set(false);
	}
}
