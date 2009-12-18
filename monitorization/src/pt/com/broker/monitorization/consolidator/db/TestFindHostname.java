package pt.com.broker.monitorization.consolidator.db;

public class TestFindHostname
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String result = AgentName.findHostname("10.135.34.141:3315");
		System.out.println(result);
	}

}
