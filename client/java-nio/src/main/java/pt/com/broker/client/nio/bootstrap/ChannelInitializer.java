package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.connection.ConnectionEventListener;
import pt.com.broker.client.nio.handlers.AcceptMessageHandler;
import pt.com.broker.client.nio.handlers.ConnectionStatusChangeEventHandler;
import pt.com.broker.client.nio.handlers.HeartBeatEventHandler;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveFaultHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.BindingSerializer;

/**
 * Created by luissantos on 06-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class ChannelInitializer extends BaseChannelInitializer
{

	static final Logger log = LoggerFactory.getLogger(ChannelInitializer.class);

	private final PongMessageHandler pongMessageHandler;
	private final ReceiveFaultHandler faultHandler;
	private final AcceptMessageHandler acceptMessageHandler;
	private final ReceiveMessageHandler receiveMessageHandler;

	protected ConsumerManager consumerManager;
	protected PongConsumerManager pongConsumerManager;
	protected PendingAcceptRequestsManager acceptRequestsManager;

	protected SSLContext context;

	private List<ConnectionEventListener> connectionEventListeners;

	/**
	 * <p>
	 * Constructor for ChannelInitializer.
	 * </p>
	 *
	 * @param serializer
	 *            a {@link pt.com.broker.types.BindingSerializer} object.
	 * @param consumerManager
	 *            a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
	 * @param pongConsumerManager
	 *            a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
	 */
	public ChannelInitializer(BindingSerializer serializer, ConsumerManager consumerManager, PongConsumerManager pongConsumerManager, List<ConnectionEventListener> connectionEventListeners)
	{

		super(serializer);

		setConsumerManager(consumerManager);

		setPongConsumerManager(pongConsumerManager);

		this.connectionEventListeners = connectionEventListeners;

		pongMessageHandler = new PongMessageHandler(getPongConsumerManager());

		faultHandler = new ReceiveFaultHandler(getConsumerManager());

		acceptMessageHandler = new AcceptMessageHandler(null);

		receiveMessageHandler = new ReceiveMessageHandler(getConsumerManager());

	}

	/** {@inheritDoc} */
	@Override
	protected void initChannel(Channel ch) throws Exception
	{

		super.initChannel(ch);

		log.debug("**************** Initializing channel! **********************");

		ChannelPipeline pipeline = ch.pipeline();

		SSLContext sslContext = getContext();

		if (sslContext != null)
		{

			SSLEngine engine = sslContext.createSSLEngine();

			/*
			 * 
			 * http://stackoverflow.com/a/17979954/3564261
			 */
			/*
			 * SSLParameters params = engine.getSSLParameters();
			 * 
			 * params.setEndpointIdentificationAlgorithm("HTTPS");
			 * 
			 * engine.setSSLParameters(params);
			 */

			engine.setUseClientMode(true);
			pipeline.addFirst("ssl", new SslHandler(engine, false));

		}
		pipeline.addLast("heartbeat_handler", new HeartBeatEventHandler());

		/* add message receive handler */
		pipeline.addLast("broker_notification_handler", receiveMessageHandler);

		pipeline.addLast("broker_pong_handler", pongMessageHandler);
		pipeline.addLast("broker_fault_handler", faultHandler);
		pipeline.addLast("broker_accept_handler", acceptMessageHandler);
		pipeline.addLast("reconnect_handler", new ConnectionStatusChangeEventHandler(connectionEventListeners));
		pipeline.addLast("exception_catcher", new ChannelHandlerAdapter()
		{

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx,
					Throwable cause) throws Exception
			{
				if (!(cause instanceof IOException))
				{
					log.error("*************Unexpected exception caught*********************", cause);
				}
			}

		});

	}

	/**
	 * <p>
	 * Getter for the field <code>consumerManager</code>.
	 * </p>
	 *
	 * @return a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
	 */
	public ConsumerManager getConsumerManager()
	{
		return consumerManager;
	}

	/**
	 * <p>
	 * Setter for the field <code>consumerManager</code>.
	 * </p>
	 *
	 * @param consumerManager
	 *            a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
	 */
	public void setConsumerManager(ConsumerManager consumerManager)
	{
		this.consumerManager = consumerManager;
	}

	/**
	 * <p>
	 * Getter for the field <code>pongConsumerManager</code>.
	 * </p>
	 *
	 * @return a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
	 */
	public PongConsumerManager getPongConsumerManager()
	{
		return pongConsumerManager;
	}

	/**
	 * <p>
	 * Setter for the field <code>pongConsumerManager</code>.
	 * </p>
	 *
	 * @param pongConsumerManager
	 *            a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
	 */
	public void setPongConsumerManager(PongConsumerManager pongConsumerManager)
	{

		this.pongConsumerManager = pongConsumerManager;

		if (pongMessageHandler != null)
		{
			pongMessageHandler.setManager(pongConsumerManager);
		}

	}

	/**
	 * <p>
	 * Getter for the field <code>acceptRequestsManager</code>.
	 * </p>
	 *
	 * @return a {@link pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager} object.
	 */
	public PendingAcceptRequestsManager getAcceptRequestsManager()
	{
		return acceptRequestsManager;
	}

	/**
	 * <p>
	 * Setter for the field <code>acceptRequestsManager</code>.
	 * </p>
	 *
	 * @param acceptRequestsManager
	 *            a {@link pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager} object.
	 */
	public void setAcceptRequestsManager(PendingAcceptRequestsManager acceptRequestsManager)
	{
		this.acceptRequestsManager = acceptRequestsManager;
		acceptMessageHandler.setManager(acceptRequestsManager);
	}

	/**
	 * <p>
	 * Getter for the field <code>context</code>.
	 * </p>
	 *
	 * @return a {@link javax.net.ssl.SSLContext} object.
	 */
	public SSLContext getContext()
	{
		return context;
	}

	/**
	 * <p>
	 * Setter for the field <code>context</code>.
	 * </p>
	 *
	 * @param context
	 *            a {@link javax.net.ssl.SSLContext} object.
	 */
	public void setContext(SSLContext context)
	{
		this.context = context;
	}

	/**
	 * <p>
	 * Setter for the field <code>faultHandler</code>.
	 * </p>
	 *
	 * @param adapter
	 *            a {@link pt.com.broker.client.nio.events.BrokerListener} object.
	 */
	public void setFaultHandler(BrokerListener adapter)
	{

		faultHandler.setFaultListenerAdapter(adapter);

	}

}
