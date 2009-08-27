package pt.com.broker.messaging;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.caudexorigo.ds.Cache;
import org.caudexorigo.ds.CacheFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.MessageType;

/**
 * QueueSessionListenerList contains a list of queue subscribers.
 * 
 */

public class QueueSessionListenerList
{
	// key: destinationName
	private static final Cache<String, QueueSessionListener> queueSessionListener = new Cache<String, QueueSessionListener>();
	private static Logger log = LoggerFactory.getLogger(QueueSessionListenerList.class);

	static
	{
		Runnable counter = new Runnable()
		{
			public void run()
			{
				try
				{
					Collection<QueueSessionListener> qsl = queueSessionListener.values();

					for (QueueSessionListener qs : qsl)
					{
						int ssize = qs.count();
						String ctName = String.format("/system/stats/queue-consumer-count/#%s#", qs.getDestinationName());
						String content = GcsInfo.getAgentName() + "#" + qs.getDestinationName() + "#" + ssize;

						NetBrokerMessage brkMessage = new NetBrokerMessage(content.getBytes("UTF-8"));

						InternalMessage intMsg = new InternalMessage();
						intMsg.setContent(brkMessage);
						intMsg.setDestination(ctName);
						intMsg.setPublishDestination(ctName);
						intMsg.setType(MessageType.COM_TOPIC);

						Gcs.publish(intMsg);
					}
				}
				catch (Throwable t)
				{
					log.error(t.getMessage(), t);
				}

			}
		};

		BrokerExecutor.scheduleWithFixedDelay(counter, 20, 20, TimeUnit.SECONDS);
	}

	private static final CacheFiller<String, QueueSessionListener> queue_listeners_cf = new CacheFiller<String, QueueSessionListener>()
	{
		public QueueSessionListener populate(String destinationName)
		{
			try
			{
				QueueSessionListener qsl = new QueueSessionListener(destinationName);
				Gcs.addAsyncConsumer(destinationName, qsl);
				return qsl;
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}
	};

	public static QueueSessionListener get(String destinationName)
	{
		try
		{
			return queueSessionListener.get(destinationName, queue_listeners_cf);
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
	}

	public static void removeValue(QueueSessionListener value)
	{
		try
		{
			queueSessionListener.removeValue(value);
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
		}
	}

	public static void remove(String queueName)
	{
		try
		{
			queueSessionListener.remove(queueName);
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
		}
	}

	public static void removeSession(IoSession iosession)
	{
		try
		{
			Collection<QueueSessionListener> list = queueSessionListener.values();
			for (QueueSessionListener queueSessionListener : list)
			{
				queueSessionListener.removeSessionConsumer(iosession);
			}

		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
		}
	}
}
