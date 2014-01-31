package pt.com.broker.functests.helpers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.codec.thrift.ThriftBindingSerializer;
import pt.com.broker.codec.xml.SoapBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

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
		setDataToSend(buildMessage());
		super.build();
	}

	private byte[] buildMessage()
	{

		BindingSerializer encoder = null;

		switch (getEncodingProtocolType())
		{
		case SOAP_v0:
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

			if (!getEncodingProtocolType().equals(NetProtocolType.SOAP_v0))
			{
				dataOutputStream.write(headerWithoutSize);
			}
			dataOutputStream.writeInt(rawData.length);
			dataOutputStream.write(rawData);
		}
		catch (IOException e)
		{
			System.err.println(e);
			setFailure(e);
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			setFailure(ex);
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
