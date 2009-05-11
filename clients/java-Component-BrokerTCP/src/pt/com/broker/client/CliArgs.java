package pt.com.broker.client;

import org.caudexorigo.cli.Option;

public interface CliArgs
{
	@Option(shortName = "h", defaultValue = "localhost")
	String getHost();

	@Option(shortName = "p", defaultValue = "3323")
	int getPort();

	@Option(shortName = "n", defaultValue = "/test")
	String getDestination();

	@Option(shortName = "d", defaultValue = "TOPIC")
	String getDestinationType();
	
	@Option(shortName = "L", defaultValue = ".")
	String getKeystoreLocation();
	
	@Option(shortName = "W", defaultValue = "")
	String getKeystorePassword();
	
	@Option(shortName = "S", defaultValue = ".")
	String getSTSLocation();
	
	@Option(shortName = "U", defaultValue = "")
	String getSTSUsername();
	
	@Option(shortName = "P", defaultValue = "")
	String getSTSPassword();
	
	
	@Option(shortName = "l", defaultValue = "1000")
	int getMessageLength();
	
}