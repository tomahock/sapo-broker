package pt.com.gcs.security;

import java.util.List;

import pt.com.gcs.conf.ChannelType;

public class ChannelTypePredicate implements AclPredicate {

	private ChannelType channelType;
	
	public ChannelTypePredicate(ChannelType channelType) {
		this.channelType = channelType;
	}
	
	@Override
	public boolean match(SessionProperties properties) {
		Object _channelTypes = properties.get("CHANNELTYPE");
		if(_channelTypes == null)
		{
			return false;
		}
		List<ChannelType> channelTypes = (List<ChannelType>)_channelTypes;
		for(ChannelType ct : channelTypes)
		{
			if(channelType.equals(ct))
				return true;
		}
		return false;

	}

	public ChannelType getChannelType() {
		return channelType;
	}
	
	
	@Override
	public String toString() {
		return "ChannelTypePredicate (" + channelType + ")";
	}

}
