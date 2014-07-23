package pt.com.broker.codec.xml;

import java.io.InputStream;
import java.io.OutputStream;

import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.codec.xml.soap.FaultCode;
import pt.com.broker.codec.xml.soap.FaultReason;
import pt.com.broker.codec.xml.soap.SoapEnvelope;
import pt.com.broker.codec.xml.soap.SoapFault;

import javax.xml.bind.*;

public class SoapSerializer {
    private static final Logger log = LoggerFactory.getLogger(SoapSerializer.class);

    private static JAXBContext jaxbContext = null;

    static {
        try {

            jaxbContext = JAXBContext.newInstance(SoapEnvelope.class,SoapFault.class , FaultReason.class, FaultCode.class);

        } catch (JAXBException e) {

            e.printStackTrace();

        }

    }



    public static void ToXml(SoapEnvelope soapEnv, OutputStream out) {



        try {
            IMarshallingContext mctx = JibxActors.getMarshallingContext();

            mctx.marshalDocument(soapEnv, "UTF-8", null, System.out);

            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(soapEnv, out);

            jaxbMarshaller.marshal(soapEnv, System.out);

        } catch (JiBXException e) {

            if (soapEnv.body.notification != null) {
                BrokerMessage bmsg = soapEnv.body.notification.brokerMessage;
                StringBuilder buf = new StringBuilder();
                buf.append("\ncorrelationId: " + bmsg.correlationId);
                buf.append("\ndestinationName: " + bmsg.destinationName);
                buf.append("\nexpiration: " + bmsg.expiration);
                buf.append("\nmessageId: " + bmsg.messageId);
                buf.append("\npriority: " + bmsg.priority);
                buf.append("\ntextPayload: " + bmsg.textPayload);
                buf.append("\ntimestamp: " + bmsg.timestamp);

                log.error("Unable to marshal soap envelope:" + buf.toString());
            }

            JibxActors.reload();
            throw new RuntimeException(e);
        }catch (JAXBException ex){

            throw new RuntimeException(ex);
        }
    }

    public static SoapEnvelope FromXml(InputStream in) {


        try {

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            Object o = jaxbUnmarshaller.unmarshal(in);

            if (o instanceof SoapEnvelope)
                return (SoapEnvelope) o;
            else
                return new SoapEnvelope();



        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        /*
        try {
            IUnmarshallingContext uctx = JibxActors.getUnmarshallingContext();
            Object o = uctx.unmarshalDocument(in, "UTF-8");
            if (o instanceof SoapEnvelope)
                return (SoapEnvelope) o;
            else
                return new SoapEnvelope();
        } catch (JiBXException e) {
            JibxActors.reload();
            throw new RuntimeException(e);
        }*/
    }




}