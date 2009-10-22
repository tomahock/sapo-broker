#ifndef __SAPO_BROKER_XML_TEMPLATES_H__
#define _SAPO_BROKER_XML_TEMPLATES_H_

// Template for publishing messages (Topics or queues)
// This will be filled by the correct params 
// Not pretty, but fast

#define TPUBLISH "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body>\
<%s xmlns=\"http://services.sapo.pt/broker\"><BrokerMessage>\
<DestinationName>%s</DestinationName>\
<TextPayload>\
<![CDATA[%s]]>\
</TextPayload>\
</BrokerMessage></%s>\
</soapenv:Body></soapenv:Envelope>"


#define TPUBLISH_TIME "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body>\
<%s xmlns=\"http://services.sapo.pt/broker\"><BrokerMessage>\
<Expiration>%s</Expiration>\
<DestinationName>%s</DestinationName>\
<TextPayload>\
<![CDATA[%s]]>\
</TextPayload>\
</BrokerMessage></%s>\
</soapenv:Body></soapenv:Envelope>"


#define TACK "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body>\
<Acknowledge xmlns=\"http://services.sapo.pt/broker\">\
<MessageId>%s</MessageId>\
<DestinationName>%s</DestinationName>\
</Acknowledge>\
</soapenv:Body></soapenv:Envelope>"

// Template for subscribing topics/queues
// This will be filled by the correct params 
// Not pretty, but fast

#define TSUBSCRIBE "<soapenv:Envelope xmlns:soapenv='http://www.w3.org/2003/05/soap-envelope'><soapenv:Body>\
<Notify xmlns='http://services.sapo.pt/broker'>\
<DestinationName>%s</DestinationName>\
<DestinationType>%s</DestinationType>\
</Notify>\
</soapenv:Body></soapenv:Envelope>"

#endif /* _SAPO_BROKER_XML_TEMPLATES_H__ */
