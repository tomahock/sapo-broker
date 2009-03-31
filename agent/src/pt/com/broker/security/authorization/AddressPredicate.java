package pt.com.broker.security.authorization;

import java.net.InetAddress;

import org.apache.mina.filter.firewall.Subnet;

import pt.com.broker.security.SessionProperties;

public class AddressPredicate implements AclPredicate {

	private Subnet subnet;
	
	public AddressPredicate(InetAddress address, int mask)
	{
		if(mask > 32 || mask < 0)
			throw new RuntimeException("Invalid mask value. Mask value: " + mask);
		
		subnet = new Subnet(address, mask);
	}
	
	@Override
	public boolean match(SessionProperties properties) {
		InetAddress address = properties.getAddress();
		if(address == null)
		{
			return false;
		}

		return subnet.inSubnet(address);
	}
	
	public Subnet getSubnet()
	{
		return subnet;
	}
	
	@Override
	public String toString() {
		return "AddressPredicate (" + subnet.toString() + ")";
	}

}
