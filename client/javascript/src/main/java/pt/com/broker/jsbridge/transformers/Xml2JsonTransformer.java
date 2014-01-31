package pt.com.broker.jsbridge.transformers;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.jsbridge.MessageTransformer;
import pt.com.broker.types.NetBrokerMessage;

public class Xml2JsonTransformer implements MessageTransformer
{
	private static final Logger log = LoggerFactory.getLogger(Xml2JsonTransformer.class);

	javax.xml.transform.Source xsltSource = null;

	public Xml2JsonTransformer()
	{
		super();

		try
		{
			xsltSource = new javax.xml.transform.stream.StreamSource(new File("./conf/xml2json.xsl"));
		}
		catch (Throwable t)
		{
			log.error("Failed to open XSLT transformation.", t);
		}
	}

	@Override
	public NetBrokerMessage transform(NetBrokerMessage message)
	{
		if (xsltSource == null)
			return message;

		String xmlMessage = new String(message.getPayload());

		String jsonMessage = null;

		javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(new StringReader(xmlMessage));
		StringWriter sw = new StringWriter();
		javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(sw);

		// create an instance of TransformerFactory
		javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();

		try
		{
			javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(xmlSource, result);
			// jsonMessage = StringEscapeUtils.escapeHtml(sw.toString() );
			jsonMessage = sw.toString();
		}
		catch (Throwable t)
		{

			log.error("Error while transforming XML \"" + xmlMessage + "\" to JSON", t);
			jsonMessage = "{\"transform-error\":\"" + StringEscapeUtils.escapeJavaScript(ErrorAnalyser.findRootCause(t).getMessage()) + "\"}";
		}

		return new NetBrokerMessage(jsonMessage.getBytes());
	}
}
