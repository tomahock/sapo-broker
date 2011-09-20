package pt.com.broker.performance.distributed;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineConfiguration
{
	private static final Logger log = LoggerFactory.getLogger(MachineConfiguration.class);

	private String machineName;
	private List<String> producers;
	private List<String> consumers;

	private boolean stop = false;

	private MachineConfiguration()
	{

	}

	public MachineConfiguration(String machineName, List<String> producers, List<String> consumers)
	{
		this.machineName = machineName;
		this.producers = producers;
		this.consumers = consumers;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public List<String> getProducers()
	{
		return producers;
	}

	public List<String> getConsumers()
	{
		return consumers;
	}

	public byte[] serialize()
	{
		byte[] data = null;
		try
		{
			UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
			ObjectOutputStream outputObj = new ObjectOutputStream(bout);
			outputObj.writeUTF(machineName);
			outputObj.writeInt(producers.size());
			for (String producer : producers)
			{
				outputObj.writeUTF(producer);
			}
			outputObj.writeInt(consumers.size());
			for (String consumer : consumers)
			{
				outputObj.writeUTF(consumer);
			}
			outputObj.writeBoolean(stop);

			outputObj.flush();

			data = bout.toByteArray();
		}
		catch (IOException e)
		{
			log.error("Failed to serialize object", e);
		}
		return data;
	}

	public static MachineConfiguration deserialize(byte[] data)
	{
		MachineConfiguration machineConfiguration = new MachineConfiguration();

		try
		{
			ObjectInputStream inputObj;
			inputObj = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));

			machineConfiguration.machineName = inputObj.readUTF();

			int producersCount = inputObj.readInt();

			machineConfiguration.producers = new ArrayList<String>(producersCount);
			for (int i = 0; i != producersCount; ++i)
			{
				machineConfiguration.producers.add(inputObj.readUTF());
			}

			int consumersCount = inputObj.readInt();

			machineConfiguration.consumers = new ArrayList<String>(consumersCount);
			for (int i = 0; i != consumersCount; ++i)
			{
				machineConfiguration.consumers.add(inputObj.readUTF());
			}

			machineConfiguration.stop = inputObj.readBoolean();
		}
		catch (Throwable e)
		{
			log.error("Failed to deserialize object", e);
			return null;
		}

		return machineConfiguration;
	}

	public void setStop(boolean stop)
	{
		this.stop = stop;
	}

	public boolean isStop()
	{
		return stop;
	}

}
