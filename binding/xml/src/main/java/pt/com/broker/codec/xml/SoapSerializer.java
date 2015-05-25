package pt.com.broker.codec.xml;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.xml.soap.FaultCode;
import pt.com.broker.codec.xml.soap.FaultReason;
import pt.com.broker.codec.xml.soap.SoapEnvelope;
import pt.com.broker.codec.xml.soap.SoapFault;

public class SoapSerializer
{
	private static final Logger log = LoggerFactory.getLogger(SoapSerializer.class);

	private static JAXBContext jaxbContext = null;

	static
	{
		try
		{

			jaxbContext = JAXBContext.newInstance(SoapEnvelope.class, SoapFault.class, FaultReason.class, FaultCode.class);

		}
		catch (JAXBException e)
		{

			e.printStackTrace();

		}

	}

	public static void ToXml(SoapEnvelope soapEnv, OutputStream out)
	{

		try
		{

			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(soapEnv, out);

		}
		catch (JAXBException ex)
		{

			throw new RuntimeException(ex);
		}
	}

	public static SoapEnvelope FromXml(InputStream in)
	{

		try
		{

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			Object o = jaxbUnmarshaller.unmarshal(in);

			if (o instanceof SoapEnvelope)
				return (SoapEnvelope) o;
			else
				return new SoapEnvelope();

		}
		catch (JAXBException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

}