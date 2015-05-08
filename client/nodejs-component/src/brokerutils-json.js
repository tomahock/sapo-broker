/**
 * @fileOverview This file contains the BrokerUtils class
 * @author <a href="mailto:jpmca@ua.pt">João Abreu</a>
 * @version 0.1.0
 */

/**
 * Exports class as BrokerUtils
 */
module.exports = BrokerUtilsJSON;

/**
 * @namespace Holds functionality of BrokerUtils
 */
function BrokerUtilsJSON () {}

/**
 * Returns a JSON publish formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsJSON.makePublish = function( message )
{
	var jsonMessage;

	jsonMessage = {
		action: {
			publish: {
				action_id: message.actionId,
				destination_type: message.destinationType,
				destination: message.destinationName,
				BrokerMessage: {
					message_id: message.messageId,
					payload: new Buffer( message.payload ).toString( 'base64' ),
					expiration: message.expiration,
					timestamp: message.timestamp
				}
			},
			action_type: 'PUBLISH'
		}
	};

    return JSON.stringify( jsonMessage );
};

/**
 * Returns a JSON subscribe formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsJSON.makeSubscribe = function( message )
{
	var jsonSubscribe;

	jsonSubscribe = {
		action: {
			subscribe: {
				action_id: message.actionId,
				destination_type: message.destinationType,
				destination: message.destinationName
			},
			action_type: 'SUBSCRIBE'
		}
	};

	return JSON.stringify( jsonSubscribe );
};

/**
 * Returns a JSON unsubscribe formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsJSON.makeUnsubscribe = function( message )
{
	var jsonUnsubscribe;

	jsonUnsubscribe = {
		action: {
			unsubscribe: {
				action_id: message.actionId,
				destination_type: message.destinationType,
				destination: message.destinationName
			},
			action_type: 'UNSUBSCRIBE'
		}
	};

	return JSON.stringify( jsonUnsubscribe );
};

/**
 * Returns a JSON acknowledge formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsJSON.makeAcknowledge = function( message )
{
	var jsonAcknowledge;

	jsonAcknowledge = {
		action: {
			acknowledge: {
				action_id: message.actionId,
				message_id: message.messageId,
				destination: message.destinationName
			},
			action_type: 'ACKNOWLEDGE'
		}
	};

	return JSON.stringify( jsonAcknowledge );
};

/**
 * Returns a JSON poll formatted for the Sapo Broker
 * @param  {object} message The message object sent from the client
 * @return {string}
 * @static
 */
BrokerUtilsJSON.makePoll = function( message )
{
	var jsonPoll;

	jsonPoll = {
		action: {
			poll: {
				action_id: message.actionId,
				destination: message.destinationName,
				timeout: message.timeout
			},
			action_type: 'POLL'
		}
	};


	return JSON.stringify( jsonPoll );
};