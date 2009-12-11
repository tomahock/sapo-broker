package pt.com.broker.performance.distributed;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestResult
{	
	private static final Logger log = LoggerFactory.getLogger(TestResult.class);
	
	private ActorType actorType;
	private String actorName;
	private String testName;
	private int messages;
	private long time;


	public enum ActorType{ Procucer, Consumer};
	
	
	private TestResult()
	{
		
	}
	
	public TestResult(ActorType actorType, String actorName, String testName, int messages, long time)
	{
		this.actorType = actorType;
		this.actorName = actorName;
		this.testName = testName;
		this.messages = messages;
		this.time = time;
	}
	
	public TestResult(ActorType actorType, String actorName, String testName)
	{
		this.actorType = actorType;
		this.actorName = actorName;
		this.testName = testName;
		this.messages = -1;
		this.time = 0;
	}

	public ActorType getActorType()
	{
		return actorType;
	}

	public String getActorName()
	{
		return actorName;
	}

	public String getTestName()
	{
		return testName;
	}

	public int getMessages()
	{
		return messages;
	}

	public double getTime()
	{
		return time;
	}
	
	public byte[] serialize()
	{
		byte[] data = null;
		try
		{
			UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
			ObjectOutputStream outputObj = new ObjectOutputStream(bout);
			outputObj.writeUTF(testName);
			outputObj.writeUTF(actorName);
			outputObj.writeUTF(actorType.toString());
			outputObj.writeInt(messages);
			outputObj.writeLong(time);
			
			outputObj.flush();

			data = bout.toByteArray();			
		}
		catch (IOException e)
		{
			log.error("Failed to serialize object", e);
		}
		return data;
	}
	
	public static TestResult deserialize(byte[] data)
	{
		TestResult testResult = new TestResult();
		
		try
		{
			ObjectInputStream inputObj;
			inputObj = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));
			
			testResult.testName = inputObj.readUTF();
			testResult.actorName = inputObj.readUTF();
			testResult.actorType = ActorType.valueOf( inputObj.readUTF() );
			testResult.messages = inputObj.readInt();
			testResult.time = inputObj.readLong();	
		}
		catch (Throwable e)
		{
			log.error("Failed to deserialize object", e);
			return null;
		}
		
		return testResult;
	}
}
