package pt.com.broker.functests.samples;

import org.caudexorigo.cli.Option;

/**
 * Auxiliary interface that maps command line arguments.
 * 
 */

public interface CliArgs
{
	/**
	 * 
	 * @return Host name.
	 */
	@Option(shortName = "h", defaultValue = "localhost")
	String getHost();

	/**
	 * 
	 * @return Host port.
	 */
	@Option(shortName = "p", defaultValue = "3323")
	int getPort();

	/**
	 * 
	 * @return Host UDP port.
	 */
	@Option(shortName = "u", defaultValue = "3323")
	int getUdpPort();

	/**
	 * 
	 * @return Destination name (e.g. "/test").
	 */
	@Option(shortName = "n", defaultValue = "/test")
	String getDestination();

	/**
	 * 
	 * @return Destination Type (TOPIC, QUEUE, VIRTUAL_QUEUE).
	 */
	@Option(shortName = "d", defaultValue = "TOPIC")
	String getDestinationType();

	/**
	 * 
	 * @return STS location (e.g. https://services.sapo.pt/STS/).
	 */
	@Option(shortName = "S", defaultValue = ".")
	String getSTSLocation();

	/**
	 * 
	 * @return User name.
	 */
	@Option(shortName = "U", defaultValue = "")
	String getUsername();

	/**
	 * 
	 * @return User password.
	 */
	@Option(shortName = "P", defaultValue = "")
	String getUserPassword();

	/**
	 * 
	 * @return Message length. Useful for generate random messages with known size.
	 */
	@Option(shortName = "l", defaultValue = "256")
	int getMessageLength();

	/**
	 * 
	 * @return Delay time. Time to wait between messages in milliseconds.
	 */
	@Option(shortName = "D", defaultValue = "0")
	long getDelay();

	/**
	 * 
	 * @return Number of messages
	 */
	@Option(shortName = "z", defaultValue = "1000000")
	long getMessageNumber();

	/**
	 * 
	 * @return Protocol Type. Encoding Protocol Type
	 */
	@Option(shortName = "T", defaultValue = "PROTOCOL_BUFFER")
	String getProtocolType();

	/**
	 * 
	 * @return Poll Timeout
	 */
	@Option(shortName = "t", defaultValue = "0")
	long getPollTimeout();

	/**
	 * 
	 * @return Reserve Time (used in poll)
	 */
	@Option(shortName = "r", defaultValue = "-1")
	long getReserveTime();

	@Option(shortName = "o", defaultValue = "full")
	String getOutput();

	@Option(longName = "strip-newlines", defaultValue = "false")
	boolean stripNewlines();
}