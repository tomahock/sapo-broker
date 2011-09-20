package pt.com.broker.jsbridge;

import java.util.List;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.http.netty.WebSocketHandler;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.jsbridge.protocol.JsonMessage;
import pt.com.broker.jsbridge.protocol.JsonMessage.MessageType;
import pt.com.broker.jsbridge.protocol.JsonSerializer;

public class JSBridgeHandler implements WebSocketHandler
{
	private static final Logger log = LoggerFactory.getLogger(JSBridgeHandler.class);

	@Override
	public void handleMessage(Channel channel, WebSocketFrame ws_frame)
	{
		if (ws_frame.isText())
		{
			handleTextMessage(channel, ws_frame.getTextData());
		}
		else if (ws_frame.isBinary())
		{
			throw new IllegalArgumentException("Processing of binary messages is not implemented in the 'pt.com.broker.jsbridge.JSBridgeHandler' handler");
		}
		else
		{
			throw new IllegalArgumentException("Unknown message type for 'pt.com.broker.jsbridge.JSBridgeHandler'");
		}
	}

	public void handleTextMessage(Channel channel, String json_message)
	{
		JsonMessage bsg;
		try
		{
			bsg = JsonSerializer.fromJson(json_message);

			switch (bsg.getAction())
			{
			case SUBSCRIBE:
				handleSubscribeRequest(channel, bsg);
				break;
			case UNSUBSCRIBE:
				handleUnsubscribeRequest(channel, bsg);
				break;
			case PUBLISH:
				handlePublishRequest(channel, bsg);
				break;
			default:
				throw new IllegalArgumentException("Unexpected message type received - " + bsg.getAction());
			}
		}
		catch (Throwable e)
		{
			handleError(channel, ErrorAnalyser.findRootCause(e).getMessage());
		}
	}

	@Override
	public void handleWebSocketOpened(Channel channel)
	{
	}

	@Override
	public void handleWebSocketClosed(Channel channel)
	{
		List<BridgeChannel> bc_lst = ConfigurationInfo.getBridgeChannels();

		for (BridgeChannel bridgeChannel : bc_lst)
		{
			try
			{
				bridgeChannel.unsubscribe(channel);
			}
			catch (Throwable e)
			{
				Throwable t = ErrorAnalyser.findRootCause(e);
				log.error(t.getMessage(), t);
			}
		}
	}

	private void handlePublishRequest(Channel channel, JsonMessage request) throws Throwable
	{
		String channelName = request.getChannel();
		String payload = request.getPayload();

		if (StringUtils.isBlank(channelName))
		{
			throw new IllegalArgumentException("Null or empty channel name.");
		}

		if (StringUtils.isBlank(payload))
		{
			throw new IllegalArgumentException("Null or empty payload.");
		}

		BridgeChannel bc = ConfigurationInfo.getBridgeChannelByName(channelName);

		if ((bc == null) || (!bc.allowPublication()))
		{
			throw new IllegalArgumentException(String.format("Can not publish to channel '%s'", channelName));
		}

		bc.publish(payload);
	}

	private void handleSubscribeRequest(Channel channel, JsonMessage request) throws Throwable
	{
		String channelName = request.getChannel();

		if (StringUtils.isBlank(channelName))
		{
			throw new IllegalArgumentException("Null or empty channel name.");
		}

		BridgeChannel bc = ConfigurationInfo.getBridgeChannelByName(channelName);

		if ((bc == null) || (!bc.allowSubscription()))
		{
			throw new IllegalArgumentException(String.format("Can not subscribe to channel '%s'", channelName));
		}

		bc.subscribe(channel);
	}

	private void handleUnsubscribeRequest(Channel channel, JsonMessage request) throws Throwable
	{
		String channelName = request.getChannel();

		if (StringUtils.isBlank(channelName))
		{
			throw new IllegalArgumentException("Null or empty channel name.");
		}

		BridgeChannel bc = ConfigurationInfo.getBridgeChannelByName(channelName);

		if (bc == null)
		{
			throw new IllegalArgumentException(String.format("Can not unsubscribe to channel '%s'", channelName));
		}

		bc.unsubscribe(channel);
	}

	private void handleError(Channel channel, String message)
	{
		try
		{
			JsonMessage faultMessage = new JsonMessage(MessageType.FAULT, message);

			String fault = JsonSerializer.toJson(faultMessage);

			WebSocketFrame wsf = new DefaultWebSocketFrame(fault);

			channel.write(wsf);
		}
		catch (Throwable e)
		{
			Throwable t = ErrorAnalyser.findRootCause(e);
			log.error(t.getMessage(), t);
		}
	}
}
