package pt.com.broker.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

public class ChannelAttributes
{
	private static final ConcurrentMap<Channel, Map<String, Object>> PROPS = new ConcurrentHashMap<Channel, Map<String, Object>>();

	public static void set(ChannelHandlerContext ctx, String name, Object value)
	{
		Channel channel = ctx.getChannel();
//		System.out.println("ChannelAttributes.set.ctx: " + ctx.toString());
//		System.out.println("ChannelAttributes.set.name: " + name);
//		System.out.println("ChannelAttributes.set.channel: " + ctx.getChannel().toString());
		Map<String, Object> attribs = PROPS.get(channel);

		if (attribs == null)
		{
			attribs = new HashMap<String, Object>();
		}

		attribs.put(name, value);
		PROPS.put(channel, attribs);
	}

	public static Object get(ChannelHandlerContext ctx, String name)
	{
		Channel channel = ctx.getChannel();
//		System.out.println("ChannelAttributes.get.ctx: " + ctx.toString());
//		System.out.println("ChannelAttributes.get.name: " + name);
//		System.out.println("ChannelAttributes.get.channel: " + channel.toString());
		Map<String, Object> attribs = PROPS.get(channel);
		if (attribs == null)
		{
			return null;
		}
		return attribs.get(name);
	}

	public static void remove(ChannelHandlerContext ctx)
	{
		PROPS.remove(ctx.getChannel());
	}

	public static Set<String> getAttributeKeys(ChannelHandlerContext ctx)
	{
		Channel channel = ctx.getChannel();
		Map<String, Object> attribs = PROPS.get(channel);
		if (attribs != null)
		{
			return attribs.keySet();
		}
		else
		{
			return Collections.emptySet();
		}
	}

}
