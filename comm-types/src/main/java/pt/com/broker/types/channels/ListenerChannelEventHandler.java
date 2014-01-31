package pt.com.broker.types.channels;

import pt.com.broker.types.channels.ListenerChannel.ChannelState;

public interface ListenerChannelEventHandler
{
	void stateChanged(ListenerChannel listenerChannel, ChannelState state);
}
