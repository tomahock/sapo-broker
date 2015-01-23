import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//TODO: Delete this class
public class StupidTest {
	
	static final Logger log = LoggerFactory.getLogger(StupidTest.class);
	
	public static final String AGENT_LAUNCH_PROPERTY 		= "agent-launch";
	
	private static Boolean launchAgent;
	
	@BeforeClass
	public static void setup(){
		log.debug("Setting up test.");
		launchAgent = Boolean.valueOf(System.getProperty(AGENT_LAUNCH_PROPERTY));
		log.debug("Launch agent flag set to: {}", launchAgent);
	}
	
	@Test
	public void stupidTest(){
		log.info("I am a stupid test.");
		Assert.assertTrue("Yeah!", true);
	}

}