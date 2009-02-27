package pt.com.xml.codec;

import java.nio.charset.Charset;
import java.util.Date;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.caudexorigo.io.UnsynchByteArrayOutputStream;
import org.caudexorigo.text.DateUtil;

import pt.com.types.NetAcknowledgeMessage;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetPoll;
import pt.com.types.NetPublish;
import pt.com.types.NetSubscribe;
import pt.com.types.NetUnsubscribe;
import pt.com.types.SimpleFramingEncoder;
import pt.com.xml.Accepted;
import pt.com.xml.Acknowledge;
import pt.com.xml.BrokerMessage;
import pt.com.xml.CheckStatus;
import pt.com.xml.EndPointReference;
import pt.com.xml.Notification;
import pt.com.xml.Notify;
import pt.com.xml.Poll;
import pt.com.xml.Publish;
import pt.com.xml.SoapEnvelope;
import pt.com.xml.SoapFault;
import pt.com.xml.SoapHeader;
import pt.com.xml.SoapSerializer;
import pt.com.xml.Status;
import pt.com.xml.Unsubscribe;

public class SoapEncoder extends SimpleFramingEncoder
{

	private static final Charset CHARSET = Charset.forName("UTF-8");

	@Override
	public byte[] processBody(Object message, Short protocolType, Short protocolVersion)
	{
		if (!(message instanceof NetMessage))
		{
			throw new IllegalArgumentException("Not a valid message type for this encoder.");
		}
		NetMessage gcsMessage = (NetMessage) message;
		SoapEnvelope soap = buildSoapEnvelope(gcsMessage);
		UnsynchByteArrayOutputStream holder = new UnsynchByteArrayOutputStream();
		SoapSerializer.ToXml(soap, holder);
		return holder.toByteArray();
	}

	@Override
	public void processBody(Object message, ProtocolEncoderOutput pout, Short protocolType, Short protocolVersion)
	{
		if (!(message instanceof NetMessage))
		{
			throw new IllegalArgumentException("Not a valid message type for this encoder.");
		}

		NetMessage gcsMessage = (NetMessage) message;
		SoapEnvelope soap = buildSoapEnvelope(gcsMessage);

		IoBuffer wbuf = IoBuffer.allocate(2048, false);
		wbuf.setAutoExpand(true);
		wbuf.putInt(0);
		SoapSerializer.ToXml(soap, wbuf.asOutputStream());
		wbuf.putInt(0, wbuf.position() - 4);

		wbuf.flip();

		pout.write(wbuf);
	}

	private SoapEnvelope buildSoapEnvelope(NetMessage message)
	{
		NetMessage gcsMessage = (NetMessage) message;

		SoapEnvelope soap = new SoapEnvelope();

		switch (gcsMessage.getAction().getActionType())
		{
		case ACCEPTED:
			Accepted a = new Accepted();
			a.actionId = gcsMessage.getAction().getAcceptedMessage().getActionId();
			soap.body.accepted = a;
			break;
		case ACKNOWLEDGE_MESSAGE:
			Acknowledge ack = new Acknowledge();
			NetAcknowledgeMessage nack = gcsMessage.getAction().getAcknowledgeMessage();
			ack.actionId = nack.getActionId();
			ack.destinationName = nack.getDestination();
			ack.messageId = nack.getMessageId();
			soap.body.acknowledge = ack;
			break;
		case FAULT:
			SoapFault f = new SoapFault();
			NetFault nf = gcsMessage.getAction().getFaultMessage();
			f.faultCode.value = nf.getCode();
			f.faultReason.text = nf.getMessage();
			f.detail = nf.getDetail();
			soap.body.fault = f;
			break;
		case NOTIFICATION:
			Notification notf = new Notification();
			NetNotification nnotf = gcsMessage.getAction().getNotificationMessage();
			notf.brokerMessage = buildXmlBrokerMessage(nnotf.getMessage(), nnotf.getDestination());

			SoapEnvelope soap_env = new SoapEnvelope();
			SoapHeader soap_header = new SoapHeader();
			EndPointReference epr = new EndPointReference();
			epr.address = nnotf.getDestinationType().toString();
			soap_header.wsaFrom = epr;
			if (nnotf.getSubscription() != null)
			{
				soap_header.wsaTo = nnotf.getSubscription();
			}

			soap_header.wsaMessageID = "http://services.sapo.pt/broker/message/" + notf.brokerMessage.messageId;
			soap_header.wsaAction = "http://services.sapo.pt/broker/notification/";
			soap_env.header = soap_header;
			soap.header = soap_header;
			soap.body.notification = notf;
			break;
		case POLL:
			Poll poll = new Poll();
			NetPoll npoll = gcsMessage.getAction().getPollMessage();
			poll.actionId = npoll.getActionId();
			poll.destinationName = npoll.getDestination();
			soap.body.poll = poll;
			break;
		case PUBLISH:
			Publish pub = new Publish();
			NetPublish npub = gcsMessage.getAction().getPublishMessage();
			pub.actionId = npub.getActionId();
			pub.brokerMessage = buildXmlBrokerMessage(npub.getMessage(), npub.getDestination());
			soap.body.publish = pub;
			break;
		case SUBSCRIBE:
			Notify notify = new Notify();
			NetSubscribe nsubs = gcsMessage.getAction().getSubscribeMessage();
			notify.actionId = nsubs.getActionId();
			notify.destinationName = nsubs.getDestination();
			notify.destinationType = nsubs.getDestinationType().toString();
			soap.body.notify = notify;
			break;
		case UNSUBSCRIBE:
			Unsubscribe unsubscribe = new Unsubscribe();
			NetUnsubscribe nunsubscribe = gcsMessage.getAction().getUnsbuscribeMessage();
			unsubscribe.actionId = nunsubscribe.getActionId();
			unsubscribe.destinationName = nunsubscribe.getDestination();
			unsubscribe.destinationType = nunsubscribe.getDestinationType().toString();
			soap.body.unsubscribe = unsubscribe;
			break;
		case PING:
			soap.body.checkStatus = new CheckStatus();
			break;
		case PONG:
			Status status = new Status();
			long ts = gcsMessage.getAction().getPongMessage().getTimestamp();
			if (ts > 0)
			{
				status.timestamp = DateUtil.formatISODate(new Date(ts));
			}
			soap.body.status = status;
			break;
		}

		return soap;
	}

	private BrokerMessage buildXmlBrokerMessage(NetBrokerMessage net_bkmsg, String destinationName)
	{
		BrokerMessage bkmsg = new BrokerMessage();
		bkmsg.destinationName = destinationName;
		bkmsg.messageId = net_bkmsg.getMessageId();
		bkmsg.textPayload = new String(net_bkmsg.getPayload(), CHARSET);

		if (net_bkmsg.getExpiration() > 0)
		{
			bkmsg.expiration = DateUtil.formatISODate(new Date(net_bkmsg.getExpiration()));
		}

		if (net_bkmsg.getTimestamp() > 0)
		{
			bkmsg.timestamp = DateUtil.formatISODate(new Date(net_bkmsg.getTimestamp()));
		}

		return bkmsg;
	}
}