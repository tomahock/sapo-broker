package pt.com.gcs.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.ds.Cache;
import org.caudexorigo.ds.CacheFiller;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.MessageListener;
import pt.com.gcs.conf.GcsInfo;

public class TopicProcessorList
{
	private static final TopicProcessorList instance = new TopicProcessorList();
	private static final Logger log = LoggerFactory.getLogger(TopicProcessorList.class);

	// key: subscriptionKey
	private Cache<String, TopicProcessor> tpCache = new Cache<String, TopicProcessor>();

	
	public static class MaximumDistinctSubscriptionsReachedException extends RuntimeException
	{
		private static final long serialVersionUID = 8022131893392381671L;
		
		@Override
		public String getMessage()
		{
			return "Maximum distinct subscriptions reached";
		}
	}
	
	private static final CacheFiller<String, TopicProcessor> tp_cf = new CacheFiller<String, TopicProcessor>()
	{
		public TopicProcessor populate(String destinationName)
		{
			try
			{
				if (instance.tpCache.size() > GcsInfo.getMaxDistinctSubscriptions())
				{
					System.out.println("\n\n MaxDistinctSubscriptionsReached " + destinationName);
					throw new MaximumDistinctSubscriptionsReachedException();
					
				}
				log.info("Populate TopicProcessorList with topic: '{}'", destinationName);
				TopicProcessor qp = new TopicProcessor(destinationName);
				return qp;
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}
	};

	public static void broadcast(final String action, final Channel channel)
	{
		instance.i_broadcast(action, channel);
	}

	public static TopicProcessor get(String destinationName)
	{
		return instance.i_get(destinationName);
	}

	public static void notify(final InternalMessage message, boolean localOnly)
	{
		instance.i_notify(message, localOnly);
	}

	public static void removeListener(MessageListener listener)
	{
		instance.i_removeListener(listener);
	}

	public static void removeSession(Channel channel)
	{
		instance.i_removeSession(channel);
	}

	public static Collection<TopicProcessor> values()
	{
		return instance.i_values();
	}

	private TopicProcessorList()
	{
	}

	private void i_broadcast(final String action, final Channel channel)
	{
		try
		{
			for (TopicProcessor tp : tpCache.values())
			{
				if (tp.isBroadcastable() && tp.hasLocalConsumers())
				{
					tp.broadCastTopicInfo(action, channel);
				}
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	private TopicProcessor i_get(String destinationName)
	{
		log.debug("Get Topic for: {}", destinationName);

		try
		{
			return tpCache.get(destinationName, tp_cf);
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
		catch(MaximumDistinctSubscriptionsReachedException mdsre)
		{
			try
			{
				System.out.println("########### catch MaximumDistinctSubscriptionsReachedException");
				
				
				System.out.println("tpCache size: " + tpCache.size());
				tpCache.remove(destinationName);
				System.out.println("(after delete)tpCache size: " + tpCache.size());
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			Gcs.broadcastMaxDistinctSubscriptionsReached();
		}
		catch (Throwable t)
		{
			Throwable rootCause = ErrorAnalyser.findRootCause(t);
			CriticalErrors.exitIfCritical(rootCause);

			if(rootCause.getClass().isAssignableFrom(MaximumDistinctSubscriptionsReachedException.class))
			{
				try
				{
					tpCache.remove(destinationName);
				}
				catch (InterruptedException e)
				{
					log.error("Failed to removed topic processor entry that caused  MaxDistinctSubscriptionsReached. Reason: '{}'", e);
				}
				
				Gcs.broadcastMaxDistinctSubscriptionsReached();			
			}
			
			log.error(String.format("Failed to get TopicProcessor for topic '%s'. Message: %s", destinationName, rootCause.getMessage()), rootCause);
		}
		return null;
	}

	private void i_notify(final InternalMessage message, boolean localOnly)
	{
		try
		{
			for (TopicProcessor tp : tpCache.values())
			{
				tp.notify(message, localOnly);
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	private void i_removeListener(MessageListener listener)
	{
		try
		{
			for (TopicProcessor tp : tpCache.values())
			{
				if (tp.getSubscriptionName().equals(listener.getsubscriptionKey()))
				{
					tp.remove(listener);
					break;
				}
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	private void i_removeSession(Channel channel)
	{
		try
		{
			ListenerChannel lc = new ListenerChannel(channel);
			List<TopicProcessor> toDeleteProcessors = new ArrayList<TopicProcessor>();

			for (TopicProcessor tp : tpCache.values())
			{
				List<MessageListener> toDeleteListeners = new ArrayList<MessageListener>();

				for (MessageListener ml : tp.listeners())
				{
					if ((ml.getType() != MessageListener.Type.INTERNAL) && ml.getChannel().equals(lc))
					{
						toDeleteListeners.add(ml);
					}
				}

				for (MessageListener dml : toDeleteListeners)
				{
					tp.remove(dml);
				}

				toDeleteListeners.clear();

				if (tp.size() == 0)
				{
					toDeleteProcessors.add(tp);
				}
			}

			for (TopicProcessor tpd : toDeleteProcessors)
			{
				tpCache.remove(tpd.getSubscriptionName());
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	private Collection<TopicProcessor> i_values()
	{
		try
		{
			return tpCache.values();
		}
		catch (InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ie);
		}
	}
}
