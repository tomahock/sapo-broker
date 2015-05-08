/**
 * Exports class as BrokerUtils
 */
module.exports = BrokerUtilsProtobuf;

var fs = require('fs');
var Schema = require('protobuf').Schema;
var schema = new Schema(fs.readFileSync('../broker.desc'));
var Atom = schema['sapo_broker.Atom'];
/**
 * @namespace Holds functionality of BrokerUtils
 */
function BrokerUtilsProtobuf () {
	
}

BrokerUtilsProtobuf.makePublish = function( message )
{
	return Atom.serialize({
		'action': {
			'publish': {
				'actionId': message.actionId,
				'destinationType': message.destinationType,
				'destination': message.destinationName,
				'message': {
					'payload': message.payload
				}
			},
			'actionType': 'PUBLISH'
		}
	});
};

BrokerUtilsProtobuf.makeSubscribe = function( message )
{
	return Atom.serialize({
		'action': {
			'subscribe': {
				'actionId': message.actionId,
				'destinationType': message.destinationType,
				'destination': message.destinationName
			},
			'actionType': 'SUBSCRIBE'
		}
	});
};

BrokerUtilsProtobuf.makeUnsubscribe = function( message )
{
	return Atom.serialize({
		'action': {
			'unsubscribe': {
				'actionId': message.actionId,
				'destinationType': message.destinationType,
				'destination': message.destinationName
			},
			'actionType': 'UNSUBSCRIBE'
		}
	});
};

BrokerUtilsProtobuf.makeAcknowledge = function( message )
{
	return Atom.serialize({
		'action': {
			'acknowledge': {
				'actionId': message.actionId,
				'messageId': message.messageId,
				'destination': message.destinationName
			},
			'actionType': 'ACKNOWLEDGE'
		}
	});
};

BrokerUtilsProtobuf.makePoll = function( message )
{
	return Atom.serialize({
		'action': {
			'poll': {
				'actionId': message.actionId,
				'destination': message.destinationName,
				'timeout': message.timeout
			},
			'actionType': 'POLL'
		}
	});
};

BrokerUtilsProtobuf.decodeMsg = function(buffer)
{
	return Atom.parse(buffer);
}