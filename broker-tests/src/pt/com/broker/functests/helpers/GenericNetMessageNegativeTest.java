package pt.com.broker.functests.helpers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pt.com.protobuf.codec.ProtoBufBindingSerializer;
import pt.com.thrift.codec.ThriftBindingSerializer;
import pt.com.types.BindingSerializer;
import pt.com.types.NetAction;
import pt.com.types.NetMessage;
import pt.com.types.NetPing;
import pt.com.types.NetAction.ActionType;
import pt.com.xml.codec.SoapBindingSerializer;

public class GenericNetMessageNegativeTest extends GenericNegativeTest
{
	private NetMessage message;
	
	public GenericNetMessageNegativeTest(String testName)
	{
		super(testName);
	}
	
	@Override
	protected void build() throws Throwable
	{
		setDataToSend( buildMessage() );
		super.build();
	}
	
	private byte[] buildMessage()
	{
		BindingSerializer encoder = null;

		switch (getEncodingProtocolType())
		{
		case SOAP:
			encoder = new SoapBindingSerializer();
			break;
		case PROTOCOL_BUFFER:
			encoder = new ProtoBufBindingSerializer();
			break;
		case THRIFT:
			encoder = new ThriftBindingSerializer();
			break;
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		
		try
		{
			byte[] headerWithoutSize = new byte[] { 0, (byte) getEncodingProtocolType().ordinal(), 0, 0 };

			byte[] rawData = encoder.marshal(getMessage());


			dataOutputStream.write(headerWithoutSize);
			dataOutputStream.writeInt(rawData.length);
			dataOutputStream.write(rawData);
		}
		catch (IOException e)
		{
			System.err.println(e);
			setFailure(e);
		}

		byte[] byteArray = byteArrayOutputStream.toByteArray();
		
		return byteArray;
	}
	
	public void setMessage(NetMessage message)
	{
		this.message = message;
	}
	public NetMessage getMessage()
	{
		return message;
	}
	
}
