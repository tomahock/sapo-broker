package pt.com.gcs.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.IoEventQueueThrottle;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.GlobalConfig;
import pt.com.gcs.messaging.QueueProcessorList.MaximumQueuesAllowedReachedException;
import pt.com.gcs.messaging.adapt.QueueProcessor;
import pt.com.gcs.messaging.adapt.BDBEnviroment.DBQueue;
import pt.com.gcs.net.Peer;
import pt.com.gcs.net.codec.GcsCodec;

/**
 * Gcs is a facade for handling several message related functionality such as publish, acknowledge, etc.
 * 
 */

public class Gcs
{
	private static Logger log = LoggerFactory.getLogger(Gcs.class);

	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	private static final int IO_THREADS = NCPU + 1;

	private static final String SERVICE_NAME = "SAPO GCS";

	private static final Gcs instance = new Gcs();

	public static final int RECOVER_INTERVAL = 50;

	public static final int RECONNECT_INTERVAL = 5000;

	public static void ackMessage(String queueName, final String msgId)
	{
		instance.iackMessage(queueName, msgId);
	}

	public static void addAsyncConsumer(String destinationName, MessageListener listener)
	{
		if (listener.getSourceDestinationType() == DestinationType.TOPIC)
		{
			instance.iaddTopicConsumer(destinationName, listener);
		}
		else if (listener.getSourceDestinationType() == DestinationType.QUEUE)
		{
			instance.iaddQueueConsumer(destinationName, listener);
		}
	}

	protected static void connect(SocketAddress address)
	{
		if (GlobalConfig.contains((InetSocketAddress) address))
		{
			log.info("Connecting to '{}'.", address.toString());

			ConnectFuture cf = instance.connector.connect(address).awaitUninterruptibly();

			if (!cf.isConnected())
			{
				GcsExecutor.schedule(new Connect(address), RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
			}
		}
		else
		{
			log.info("Peer '{}' does not appear in the world map, it will be ignored.", address.toString());
		}

	}

	public static boolean enqueue(final InternalMessage message)
	{
		return instance.ienqueue(message, null);
	}

	public static boolean enqueue(InternalMessage message, String queueName)
	{
		return instance.ienqueue(message, queueName);
	}

	protected static void reloadWorldMap()
	{
		log.info("Reloading the world map");
		Set<IoSession> connectedSessions = getManagedConnectorSessions();
		for (IoSession ioSession : connectedSessions)
		{
			InetSocketAddress inet = (InetSocketAddress) ioSession.getRemoteAddress();

			// remove connections to agents that were removed from world map
			if (!GlobalConfig.contains(inet))
			{
				log.info("Remove peer '{}'", inet.toString());
				ioSession.close();
			}
		}
		List<Peer> peerList = GlobalConfig.getPeerList();
		
		
		
		List<InetSocketAddress> remoteSessions = new ArrayList<InetSocketAddress>(connectedSessions.size());
		for(IoSession session : connectedSessions)
		{
			remoteSessions.add( (InetSocketAddress)session.getRemoteAddress());
		}
		
		for (Peer peer : peerList)
		{
			SocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
			// Connect only if not already connected
			if(!remoteSessions.contains(addr))
			{
				connect(addr);
			}
		}

	}

	protected static Set<IoSession> getManagedConnectorSessions()
	{
		// return
		// Collections.unmodifiableSet(instance.connector.getManagedSessions());
		Set<IoSession> connectSessions = new HashSet<IoSession>();
		Map<Long, IoSession> mngSessions = instance.connector.getManagedSessions();

		Set<Long> keys = mngSessions.keySet();

		for (Long key : keys)
		{
			connectSessions.add(mngSessions.get(key));
		}
		return connectSessions;
	}

	protected static List<Peer> getPeerList()
	{
		return GlobalConfig.getPeerList();
	}

	public static void destroy()
	{
		instance.idestroy();
	}

	public static void init()
	{
		instance.iinit();
	}

	public static void publish(InternalMessage message)
	{
		instance.ipublish(message);
	}

	public static void removeAsyncConsumer(MessageListener listener)
	{
		if (listener.getSourceDestinationType() == DestinationType.TOPIC)
		{
			LocalTopicConsumers.remove(listener);
		}
		else if (listener.getSourceDestinationType() == DestinationType.QUEUE)
		{
			LocalQueueConsumers.remove(listener);
		}
			
	}

	private SocketAcceptor acceptor;

	private SocketConnector connector;

	private Gcs()
	{
		log.info("{} starting.", SERVICE_NAME);
		try
		{
			startAcceptor(GcsInfo.getAgentPort());
			startConnector();

			GcsExecutor.scheduleWithFixedDelay(new QueueAwaker(), RECOVER_INTERVAL, RECOVER_INTERVAL, TimeUnit.MILLISECONDS);
			GcsExecutor.scheduleWithFixedDelay(new QueueCounter(), 20, 20, TimeUnit.SECONDS);
			GcsExecutor.scheduleWithFixedDelay(new GlobalConfigMonitor(), 30, 30, TimeUnit.SECONDS);
		}
		catch (Throwable t)
		{
			Throwable rootCause = ErrorAnalyser.findRootCause(t);
			log.error(rootCause.getMessage(), rootCause);
			Shutdown.now();
		}
		Sleep.time(GcsInfo.getInitialDelay());

	}

	private void connectToAllPeers()
	{
		List<Peer> peerList = GlobalConfig.getPeerList();
		for (Peer peer : peerList)
		{
			SocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
			connect(addr);
		}
	}

	private void iackMessage(String queueName, final String msgId)
	{
		try
		{
			QueueProcessorList.get(queueName).ack(msgId);
		}
		catch (MaximumQueuesAllowedReachedException e)
		{
			// This never happens
		}
	}

	private void iaddQueueConsumer(String queueName, MessageListener listener)
	{
		try
		{
			QueueProcessorList.get(queueName);
		}
		catch (MaximumQueuesAllowedReachedException e)
		{
			// This never happens
		}

		if (listener != null)
		{
			LocalQueueConsumers.add(queueName, listener);
		}
	}

	private void iaddTopicConsumer(String topicName, MessageListener listener)
	{
		if (listener != null)
		{
			LocalTopicConsumers.add(topicName, listener, true);
		}
	}

	private boolean ienqueue(InternalMessage message, String queueName)
	{
		try
		{
			QueueProcessorList.get((queueName != null) ? queueName : message.getDestination()).store(message, true);
			return true;
		}
		catch (MaximumQueuesAllowedReachedException e)
		{
			log.error("Tried to create a new queue ('{}'). Not allowed because the limit was reached", queueName);
		}
		return false;
	}

	private void iinit()
	{
		String[] virtual_queues = VirtualQueueStorage.getVirtualQueueNames();

		for (String vqueue : virtual_queues)
		{
			log.debug("Add VirtualQueue '{}' from storage", vqueue);
			iaddQueueConsumer(vqueue, null);
		}

		String[] queues = BDBEnviroment.getQueueNames();

		for (String queueName : queues)
		{
			try
			{
				QueueProcessorList.get(queueName);
			}
			catch (MaximumQueuesAllowedReachedException e)
			{
				// This never happens
			}
		}

		// / Compatibility mode

		String[] old_virtual_queues = pt.com.gcs.messaging.adapt.VirtualQueueStorage.getVirtualQueueNames();

		List<String> virtualQueues = new ArrayList<String>();
		for (int i = 0; i != virtual_queues.length; ++i)
		{
			virtualQueues.add(virtual_queues[i]);
		}

		List<String> removedVirtualQueues = new ArrayList<String>();
		for (String vqueue : old_virtual_queues)
		{
			log.debug("Add VirtualQueue '{}' from OLD storage", vqueue);
			if (!virtualQueues.contains(vqueue))
			{
				iaddQueueConsumer(vqueue, null);
				VirtualQueueStorage.saveVirtualQueue(vqueue);
				virtualQueues.add(vqueue);
			}
			if(!removedVirtualQueues.contains(vqueue))
			{
				pt.com.gcs.messaging.adapt.VirtualQueueStorage.deleteVirtualQueue(vqueue);
				removedVirtualQueues.add(vqueue);
			}
		}
		
		//----------------------------------------
		
		String[] queueNames = pt.com.gcs.messaging.adapt.BDBEnviroment.getQueueNames();
		
		for (String queueName : queueNames)
		{
			pt.com.gcs.messaging.adapt.QueueProcessor queueProcessor = new pt.com.gcs.messaging.adapt.QueueProcessor(
					new DBQueue(pt.com.gcs.messaging.adapt.BDBEnviroment.getLastChangedDatabaseEnv(),queueName ));
			
		
			if (queueProcessor.getQueuedMessagesCount() != 0)
			{
				long lastSeqValue = queueProcessor.getStorage().getLastSequenceValue();
				
				try
				{
					pt.com.gcs.messaging.QueueProcessor qp = QueueProcessorList.get(queueName);
					
					qp.setSequenceNumber(lastSeqValue);
										
					qp.setCounter( queueProcessor.getQueuedMessagesCount() );
					
					queueProcessor.wakeup();
				}
				catch (MaximumQueuesAllowedReachedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			queueProcessor.getStorage().deleteQueue();
		}
		
		
		DBQueue[] old_queues = pt.com.gcs.messaging.adapt.BDBEnviroment.getQueues();

		for (DBQueue dbQueue : old_queues)
		{
			if(dbQueue.env ==  pt.com.gcs.messaging.adapt.BDBEnviroment.getLastChangedDatabaseEnv())
				continue;
			
			pt.com.gcs.messaging.adapt.QueueProcessor queueProcessor = new pt.com.gcs.messaging.adapt.QueueProcessor(dbQueue);
			
			if (queueProcessor.getQueuedMessagesCount() != 0)
			{
				queueProcessor.wakeup();
			}
			queueProcessor.getStorage().deleteQueue();
		}

		
//		// This was the "one dir version"
		
//		String[] old_virtual_queues = pt.com.gcs.messaging.adapt.VirtualQueueStorage.getVirtualQueueNames();
//		
//				List<String> virtualQueues = new ArrayList<String>();
//				for (int i = 0; i != old_virtual_queues.length; ++i)
//				{
//					virtualQueues.add(virtual_queues[i]);
//				}
//
//				for (String vqueue : virtual_queues)
//				{
//					log.debug("Add VirtualQueue '{}' from OLD storage", vqueue);
//					if (!virtualQueues.contains(vqueue))
//					{
//						iaddQueueConsumer(vqueue, null);
//						VirtualQueueStorage.saveVirtualQueue(vqueue);
//						pt.com.gcs.messaging.adapt.VirtualQueueStorage.deleteVirtualQueue(vqueue);
//					}
//				}
		
//		String[] old_queues = pt.com.gcs.messaging.adapt.BDBEnviroment.getQueueNames();
//
//		for (String queueName : old_queues)
//		{
//			pt.com.gcs.messaging.adapt.QueueProcessor queueProcessor = new pt.com.gcs.messaging.adapt.QueueProcessor(queueName);
//			if (queueProcessor.getQueuedMessagesCount() != 0)
//			{
//				long lastSeqValue = queueProcessor.getStorage().getLastSequenceValue();
//				try
//				{
//					pt.com.gcs.messaging.QueueProcessor qp = QueueProcessorList.get(queueName);
//					qp.setSequenceNumber(lastSeqValue);
//					qp.setCounter(queueProcessor.getQueuedMessagesCount());
//					queueProcessor.wakeup();
//				}
//				catch (MaximumQueuesAllowedReachedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			queueProcessor.getStorage().deleteQueue();
//		}

		
		// / End compatibility mode

		connectToAllPeers();
		
		Shutdown.isShutingDown();

		log.info("{} initialized.", SERVICE_NAME);
	}

	private void idestroy()
	{
		try
		{
			//LocalQueueConsumers.removeAllListeners();
			//LocalTopicConsumers.removeAllListeners();
			log.info("Flush buffers");
			BDBEnviroment.sync();
		}
		catch (Throwable te)
		{
			log.error(te.getMessage(), te);
		}
	}

	private void ipublish(final InternalMessage message)
	{
		message.setType(MessageType.COM_TOPIC);
		LocalTopicConsumers.notify(message);
		RemoteTopicConsumers.notify(message);
	}

	
	private void startAcceptor(int portNumber) throws IOException
	{
		
	}
	
//	private void startAcceptor(int portNumber) throws IOException
//	{
//		acceptor = new NioSocketAcceptor(IO_THREADS);
//
//		acceptor.setReuseAddress(true);
//		((SocketSessionConfig) acceptor.getSessionConfig()).setReuseAddress(true);
//		((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(false);
//		((SocketSessionConfig) acceptor.getSessionConfig()).setKeepAlive(true);
//		((SocketSessionConfig) acceptor.getSessionConfig()).setWriteTimeout(120);
//		acceptor.setCloseOnDeactivation(true);
//
//		acceptor.setBacklog(100);
//
//		DefaultIoFilterChainBuilder filterChainBuilder = acceptor.getFilterChain();
//
//		// Add CPU-bound job first,
//		filterChainBuilder.addLast("GCS_CODEC", new ProtocolCodecFilter(new GcsCodec()));
//		// and then a thread pool.
//		filterChainBuilder.addLast("executor", new ExecutorFilter(new OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new IoEventQueueThrottle())));
//
//		acceptor.setHandler(new GcsAcceptorProtocolHandler());
//
//		// Bind
//		acceptor.bind(new InetSocketAddress(portNumber));
//
//		String localAddr = acceptor.getLocalAddress().toString();
//		log.info("{} listening on: '{}'.", SERVICE_NAME, localAddr);
//	}

	private void startConnector()
	{
		connector = new NioSocketConnector(IO_THREADS);
		((SocketSessionConfig) connector.getSessionConfig()).setKeepAlive(true);

		DefaultIoFilterChainBuilder filterChainBuilder = connector.getFilterChain();

		// Add CPU-bound job first,
		filterChainBuilder.addLast("GCS_CODEC", new ProtocolCodecFilter(new GcsCodec()));

		// and then a thread pool.
		filterChainBuilder.addLast("executor", new ExecutorFilter(new OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new IoEventQueueThrottle())));

		connector.setHandler(new GcsRemoteProtocolHandler());
		connector.setConnectTimeoutMillis(5000); // 5 seconds timeout
	}

	public synchronized static void deleteQueue(String queueName)
	{
		QueueProcessorList.remove(queueName);
	}

}
