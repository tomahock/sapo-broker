package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;

/**
 * Created by luissantos on 07-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
@ChannelHandler.Sharable
public class ReceiveFaultHandler extends SimpleChannelInboundHandler<NetMessage>
{

	private ConsumerManager manager;

	private BrokerListener faultListenerAdapter;

	/**
	 * <p>
	 * Constructor for ReceiveFaultHandler.
	 * </p>
	 *
	 * @param manager
	 *            a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
	 */
	public ReceiveFaultHandler(ConsumerManager manager)
	{
		super();
		this.manager = manager;
	}

	/**
	 * <p>
	 * Getter for the field <code>manager</code>.
	 * </p>
	 *
	 * @return a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
	 */
	public ConsumerManager getManager()
	{
		return manager;
	}

	/** {@inheritDoc} */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, NetMessage msg) throws Exception
	{

		NetFault fault = msg.getAction().getFaultMessage();

		if (fault == null)
		{
			ctx.fireChannelRead(msg);
			return;
		}

		try
		{

			deliverFaultMessage(ctx, msg);

		}
		catch (Throwable throwable)
		{

			throw new Exception(throwable);
		}

	}

	/**
	 * <p>
	 * deliverFaultMessage.
	 * </p>
	 *
	 * @param ctx
	 *            a {@link io.netty.channel.ChannelHandlerContext} object.
	 * @param msg
	 *            a {@link pt.com.broker.types.NetMessage} object.
	 * @throws java.lang.Throwable
	 *             if any.
	 */
	protected void deliverFaultMessage(ChannelHandlerContext ctx, NetMessage msg) throws Throwable
	{

		NetFault fault = msg.getAction().getFaultMessage();

		ChannelDecorator decorator = new ChannelDecorator(ctx.channel());

		if (fault.getCode().equals(NetFault.PollTimeoutErrorCode) || fault.getCode().equals(NetFault.NoMessageInQueueErrorCode))
		{

			getManager().deliverMessage(msg, decorator.getHost());
			return;
		}

		BrokerListener listener = getFaultListenerAdapter();

		if (listener != null)
		{
			getFaultListenerAdapter().deliverMessage(msg, decorator.getHost());
			return;
		}

		ctx.fireChannelRead(msg);

	}

	/**
	 * <p>
	 * Getter for the field <code>faultListenerAdapter</code>.
	 * </p>
	 *
	 * @return a {@link pt.com.broker.client.nio.events.BrokerListener} object.
	 */
	public BrokerListener getFaultListenerAdapter()
	{
		return faultListenerAdapter;
	}

	/**
	 * <p>
	 * Setter for the field <code>faultListenerAdapter</code>.
	 * </p>
	 *
	 * @param faultListenerAdapter
	 *            a {@link pt.com.broker.client.nio.events.BrokerListener} object.
	 */
	public void setFaultListenerAdapter(BrokerListener faultListenerAdapter)
	{
		this.faultListenerAdapter = faultListenerAdapter;
	}

}
