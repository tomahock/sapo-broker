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

	@Option(shortName = "s", defaultValue = "3390")
	int getSslPort();
	
	@Option(shortName = "u", defaultValue = "0")
	int useSsl();
	
	@Option(shortName = "l", defaultValue = ".")
	String getKeystoreLocation();
	
	@Option(shortName = "w", defaultValue = "")
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