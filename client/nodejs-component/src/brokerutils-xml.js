/**
 * @fileOverview This file contains the BrokerUtils class
 * @author <a href="mailto:jpmca@ua.pt">João Abreu</a>
 * @version 0.1.0
 */

/**
 * Requires the entities module for HTML entities parsing
 * @type {object}
 */
var entities = require('entities');

/**
 * Exports class as BrokerUtils
 */
module.exports = BrokerUtilsXML;

/**
 * @namespace Holds functionality of BrokerUtils
 */
function BrokerUtilsXML () {}

/**
 * Returns a XML message formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsXML.makeMessage = function( message )
{
	var xmlMessage;

	xmlMessage = ''
	+ '<BrokerMessage>'
	+ '<DestinationName>'+ message.destinationName +'</DestinationName>'
	+ '<TextPayload>'+ entities.encode( message.payload ) +'</TextPayload>';

	if ( object.priority != null ) {
		xmlMessage += ''
		+ '<Priority>'+ message.priority +'</Priority>';
	}

	if ( object.messageid != null ) {
		xmlMessage += ''
		+ '<MessageId>'+ message.messageId +'</MessageId>';
	}

	if ( object.timestamp != null ) {
		xmlMessage += ''
		+ '<Timestamp>'+ message.timestamp +'</Timestamp>';
	}

	if ( object.expiration != null ) {
		xmlMessage += ''
		+ '<Expiration>'+ message.expiration +'</Expiration>';
	}

	if ( object.correlationid != null ) {
		xmlMessage += ''
		+ '<CorrelationId>'+ message.correlationId +'</CorrelationId>';
	}

	xmlMessage += ''
    + '</BrokerMessage>';

    return xmlMessage;
};

/**
 * Returns a SOAP subscribe formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsXML.makeSubscribe = function( message )
{
	var xmlSubscribe;

	xmlSubscribe = ''
	+ '<soapenv:Envelope xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"><soapenv:Body>'
	+ '<Notify xmlns="http://services.sapo.pt/broker">'
	+ '<DestinationName>'+ message.destinationName +'</DestinationName>'
	+ '<DestinationType>'+ message.destinationType +'</DestinationType>'
	+ '</Notify>'
	+ '</soapenv:Body></soapenv:Envelope>';

	return xmlSubscribe;
};

/**
 * Returns a SOAP unsubscribe formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsXML.makeUnsubscribe = function( message )
{
	var xmlUnsubscribe;

	xmlUnsubscribe = ''
	+ '<soapenv:Envelope xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"><soapenv:Body>'
	+ '<Unsubscribe xmlns="http://services.sapo.pt/broker">'
	+ '<DestinationName>'+ message.destinationName +'</DestinationName>'
	+ '<DestinationType>'+ message.destinationType +'</DestinationType>'
	+ '</Unsubscribe>'
	+ '</soapenv:Body></soapenv:Envelope>';

	return xmlUnsubscribe;
};

/**
 * Returns a XML acknowledge formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsXML.makeAcknowledge = function( message )
{
	var xmlAcknowledge;

	xmlAcknowledge = ''
	+ '<soapenv:Envelope xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"><soapenv:Body>'
	+ '<Acknowledge xmlns="http://services.sapo.pt/broker">'
	+ '<MessageId>'+ message.messageId +'</MessageId>'
	+ '<DestinationName>'+ message.destinationName +'</DestinationName>'
	+ '</Acknowledge>'
	+ '</soapenv:Body></soapenv:Envelope>';

	return xmlAcknowledge;
};

/**
 * Returns a SOAP poll formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsXML.makePoll = function( message )
{
	var xmlPoll;

	xmlPoll = ''
	+ '<?xml version="1.0" encoding="UTF-8"?>'
	+ '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:mq="http://services.sapo.pt/broker">'
	+ '<soap:Body>'
	+ '<mq:Poll>'
	+ '<mq:DestinationName>'+ message.destinationName +'</mq:DestinationName>'
	+ '</mq:Poll>'
	+ '</soap:Body>'
	+ '</soap:Envelope>';

	return xmlPoll;
};

/**
 * Returns a SOAP event formatted for the Broker
 * @param  {string} messageType Enqueue, Publish
 * @param  {string} message The message object
 * @return {string}
 * @static
 */
BrokerUtilsXML.makeEvent = function( messageType, message )
{
	var xmlEvent;

	xmlEvent = ''
	+ '<soapenv:Envelope xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope"><soapenv:Body>'
	+ '<'+ messageType +' xmlns="http://services.sapo.pt/broker">'
	+ BrokerUtilsXML.makeMessage( message )
	+ '</'+ messageType +'>'
	+ '</soapenv:Body></soapenv:Envelope>';

	return xmlEvent;
};