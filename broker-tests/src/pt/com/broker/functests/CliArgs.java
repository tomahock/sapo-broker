package pt.com.broker.functests;

import org.caudexorigo.cli.Option;

/**
 * Auxiliary interface that maps command line arguments.
 * 
 */
public interface CliArgs
{
	/**
	 * 
	 * @return Perform all tests.
	 */
	@Option(shortName = "a", defaultValue = "0")
	int getAll();

	/**
	 * 
	 * @return Perform just negative.
	 */
	@Option(shortName = "n", defaultValue = "0")
	int getNegative();

	/**
	 * 
	 * @return Perform just positive.
	 */
	@Option(shortName = "p", defaultValue = "0")
	int getPositive();

	/**
	 * 
	 * @return Perform just positive topic related tests.
	 */
	@Option(shortName = "t", defaultValue = "0")
	int getTopic();

	/**
	 * 
	 * @return Perform authentication and SSL related tests.
	 */
	@Option(shortName = "s", defaultValue = "0")
	int getSslAndAuthentication();

	/**
	 * 
	 * @return Perform just positive queue related tests.
	 */
	@Option(shortName = "q", defaultValue = "0")
	int getQueue();

	/**
	 * 
	 * @return Perform just positive virtual queue related tests.
	 */
	@Option(shortName = "v", defaultValue = "0")
	int getVirtualQueue();

	/**
	 * 
	 * @return Perform just UDP related tests.
	 */
	@Option(shortName = "u", defaultValue = "0")
	int getUdp();

	/**
	 * 
	 * @return Specify the number of runs
	 */
	@Option(shortName = "r", defaultValue = "1")
	int getNumberOfRuns();
}
