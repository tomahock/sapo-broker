package pt.com.gcs.security;

import java.net.InetAddress;

import org.apache.mina.filter.firewall.Subnet;

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
		Object _address = properties.get("ADDRESS");
		if(_address == null)
		{
			return false;
		}
		
		InetAddress address = (InetAddress) _address;
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
