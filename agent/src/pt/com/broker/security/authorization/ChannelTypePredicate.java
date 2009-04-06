package pt.com.broker.security.authorization;

import java.util.List;

import pt.com.broker.security.SessionProperties;
import pt.com.gcs.conf.global.ChannelType;

public class ChannelTypePredicate implements AclPredicate
{

	private ChannelType channelType;

	public ChannelTypePredicate(ChannelType channelType)
	{
		this.channelType = channelType;
	}

	@Override
	public boolean match(SessionProperties properties)
	{
		List<ChannelType> channelTypes = properties.getChannelTypes();
		if (channelTypes == null)
		{
			return false;
		}
		for (ChannelType ct : channelTypes)
		{
			if (channelType.equals(ct))
				return true;
		}
		return false;

	}

	public ChannelType getChannelType()
	{
		return channelType;
	}

	@Override
	public String toString()
	{
		return "ChannelTypePredicate (" + channelType + ")";
	}

}
