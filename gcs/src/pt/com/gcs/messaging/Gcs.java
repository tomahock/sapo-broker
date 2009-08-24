package pt.com.gcs.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

	private static final int MAX_BUFFER_SIZE = 8 * 1024 * 1024;

	private static final Gcs instance = new Gcs();
	
	public static final int RECOVER_INTERVAL = 50;
	
	public static final int RECONNECT_INTERVAL = 5000;

	public static void ackMessage(String queueName, final String msgId)
	{
		instance.iackMessage(queueName, msgId);
	}

	public static void addAsyncConsumer(String destinationName, MessageListener listener)
	{
		if (listener.getDestinationType() == DestinationType.TOPIC)
		{
			instance.iaddTopicConsumer(destinationName, listener);
		}
		else if (listener.getDestinationType() == DestinationType.QUEUE)
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

			if (!GlobalConfig.contains(inet))
			{
				log.info("Remove peer '{}'", inet.toString());
				ioSession.close();
			}
		}
		List<Peer> peerList = GlobalConfig.getPeerList();
		for (Peer peer : peerList)
		{
			SocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
			connect(addr);
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

	public static InternalMessage poll(final String queueName)
	{
		return instance.ipoll(queueName);
	}

	public static void publish(InternalMessage message)
	{
		instance.ipublish(message);
	}

	public static void removeAsyncConsumer(MessageListener listener)
	{
		if (listener.getDestinationType() == DestinationType.TOPIC)
		{
			LocalTopicConsumers.remove(listener);
		}
		else if (listener.getDestinationType() == DestinationType.QUEUE)
		{
			LocalQueueConsumers.remove(listener);
		}
	}
	
	public static void addSyncConsumer(String queueName, IoSession session)
	{
		LocalQueueConsumers.addSyncConsumer(queueName, session);
	}

	
	public static void removeSyncConsumer(String queueName, IoSession session)
	{
		LocalQueueConsumers.removeSyncConsumer(queueName, session);
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
			QueueProcessorList.get((queueName != null) ? queueName : message.getDestination()).store(message);
			return true;
		}
		catch (MaximumQueuesAllowedReachedException e)
		{
			log.error("Tried to create a new queue. Not allowed because the limit was reached");
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

		connectToAllPeers();

		log.info("{} initialized.", SERVICE_NAME);
	}

	private void idestroy()
	{
		try
		{
			LocalQueueConsumers.removeAllListeners();
			LocalTopicConsumers.removeAllListeners();
			log.info("Flush buffers");
			BDBEnviroment.sync();
		}
		catch (Throwable te)
		{
			log.error(te.getMessage(), te);
		}
	}

	private InternalMessage ipoll(final String queueName)
	{
		InternalMessage m = null;
		try
		{
			m = QueueProcessorList.get(queueName).poll();
		}
		catch (MaximumQueuesAllowedReachedException e)
		{
			// This never happens
		}
		return m;
	}

	private void ipublish(final InternalMessage message)
	{
		message.setType(MessageType.COM_TOPIC);
		LocalTopicConsumers.notify(message);
		RemoteTopicConsumers.notify(message);
	}

	private void startAcceptor(int portNumber) throws IOException
	{
		acceptor = new NioSocketAcceptor(IO_THREADS);

		acceptor.setReuseAddress(true);
		((SocketSessionConfig) acceptor.getSessionConfig()).setReuseAddress(true);
		((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(false);
		((SocketSessionConfig) acceptor.getSessionConfig()).setKeepAlive(true);
		((SocketSessionConfig) acceptor.getSessionConfig()).setWriteTimeout(120);
		acceptor.setCloseOnDeactivation(true);

		acceptor.setBacklog(100);

		DefaultIoFilterChainBuilder filterChainBuilder = acceptor.getFilterChain();

		// Add CPU-bound job first,
		filterChainBuilder.addLast("GCS_CODEC", new ProtocolCodecFilter(new GcsCodec()));
		// and then a thread pool.
		filterChainBuilder.addLast("executor", new ExecutorFilter(new OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new IoEventQueueThrottle())));

		acceptor.setHandler(new GcsAcceptorProtocolHandler());

		// Bind
		acceptor.bind(new InetSocketAddress(portNumber));

		String localAddr = acceptor.getLocalAddress().toString();
		log.info("{} listening on: '{}'.", SERVICE_NAME, localAddr);
	}

	private void startConnector()
	{
		connector = new NioSocketConnector(IO_THREADS);
		((SocketSessionConfig) connector.getSessionConfig()).setKeepAlive(true);

		DefaultIoFilterChainBuilder filterChainBuilder = connector.getFilterChain();

		// Add CPU-bound job first,
		filterChainBuilder.addLast("GCS_CODEC", new ProtocolCodecFilter(new GcsCodec()));

		// and then a thread pool.
		filterChainBuilder.addLast("executor", new ExecutorFilter(new OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new IoEventQueueThrottle(MAX_BUFFER_SIZE))));

		connector.setHandler(new GcsRemoteProtocolHandler());
		connector.setConnectTimeoutMillis(5000); // 5 seconds timeout
	}

	public synchronized static void deleteQueue(String queueName)
	{
		QueueProcessorList.remove(queueName);
	}

}
