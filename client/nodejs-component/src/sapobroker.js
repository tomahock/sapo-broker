/**
 * @fileOverview This file contains the SapoBroker class
 * @author <a href="mailto:jpmca@ua.pt">João Abreu</a>
 * @version 0.1.0
 */


/**
 * Requires the NodeJS net module
 * @type {object}
 */
var net = require('net'),
	/**
	 * Requires the NodeJS util module
	 * @type {object}
	 */
		util = require('util'),
	/**
	 * Requires the XML2JSON module for XML Parsing
	 * @type {parser}
	 */
		parser = require('../node_modules/xml2json'),
	/**
	 * Requires the winston module for logging
	 * @type {winston}
	 */
		winston = require('../node_modules/winston'),
	/**
	 * Requires the BrokerClient class
	 * @type {BrokerClient}
	 */
		BrokerClient = require('./brokerclient'),
	/**
	 * Initializes the BrokerUtils variable, so that will either assume the value of BrokerUtilsJSON or BrokerUtilsXML
	 * @type {object}
	 */
		BrokerUtils;

/**
 * Invert for production
 */
winston.add(winston.transports.File, { filename: 'debug.log', json: false, timestamp: true, maxsize: 5242880 });
//winston.remove(winston.transports.Console);

/**
 * Exports class as SapoBroker
 */
module.exports = SapoBroker;

/**
 * Defines SapoBroker as a child of BrokerClient and corrects the constructor pointer
 */
SapoBroker.prototype = new BrokerClient();
SapoBroker.prototype.contructor = SapoBroker;

	/**
	 * The encoding version passed in the header of the SAPO Broker replies
	 * @type {number}
	 */
var	ENCODING_VERSION = 0,

	/**
	 * The encoding type array that the SAPO Broker can use
	 * 0 - XML
	 * 1 - PROTOBUF
	 * 3 - JSON
	 * @type {Array} ENCODING_TYPES
	 */
	ENCODING_TYPES   = [ 0, 1, 3 ],

	/**
	 * The maximum length of a payload // THIS IS WRONG
	 * @type {number}
	 */
	MAX_LENGTH       = 262144;

/**
 * Calls the BrokerClient constructor and sets self as reference to the SapoBroker scope
 * @class Represents a client for the SapoBroker with methods based on NodeJS net module events
 * @augments BrokerClient
 */
function SapoBroker()
{
	BrokerClient.call( this );

	winston.info( 'Client instantiated' );
}

/**
 * Initializes the SapoBroker and overrides the BrokerClient init for the sake of avoiding repetition
 * Creates the socket, sets the internal event handlers, creates variables and sets the configurations
 * @param  {object} configs Configuration object passed by a consumer
 * @property {net.Socket} socket The socket that connects to the Broker
 * @property {object} configs The client configurations
 * @property {integer} configs.port The client port (Default: 6622)
 * @property {string} configs.host The client host (Default: '10.135.66.175')
 * @property {string} configs.transport The type of transport used (Default: 'json')
 * @property {string} configs.return The format the consumer wants the data to return in (xml, js, json, Default: xml)
 * @property {object} configs.callbacks The callbacks that the consumer wants to call after events happen
 * @property {array} subMap The map of TOPIC / QUEUE that the client has subscribed
 * @property {array} pollMap The map of QUEUE that the client has polled
 * @property {boolean} isReconnecting Boolean variable that shows whether the client is reconnecting
 * @property {integer} errorCounter The number of times the client has tried to reconnect
 * @property {buffer} composeBuffer The buffer that the client uses to compose the data received through the socket
 * @override
 */
SapoBroker.prototype.init = function( configs )
{
	winston.info( 'Initializing client' );

	this.socket = new net.Socket();
	this.socket.setKeepAlive( true );

	winston.info( 'Socket created' );

	this.setHandlers({
		connect: this.onConnect.bind(this),
		data:    this.onData.bind(this),
		end:     this.onEnd.bind(this),
		timeout: this.onTimeout.bind(this),
		drain:   this.onDrain.bind(this),
		error:   this.onError.bind(this),
		close:   this.onClose.bind(this)
	});

	this.subMap  = [];
	this.pollMap = [];

	this.isReconnecting = false;
	this.errorCounter   = 0;

	this.composeBuffer = new Buffer( 0 );

	this.configs = {};
	this.configure( configs );
};

/**
 * Calls the BrokerClient.configure and sets the configurations for the return and the callbacks
 * @param  {object} configs Configuration object passed by a client
 */
SapoBroker.prototype.configure = function( configs )
{
	BrokerClient.prototype.configure.call( this, configs );

	if ( configs == null ){
		configs = {};
	}

	if ( configs.transport == null ){
		if ( this.configs.transport != null ) {
			configs.transport = this.configs.transport;
		} else {
			configs.transport = 'json';
		}
	}

	if ( configs.transport === 'json' ) {
		/**
		 * Requires the BrokerUtilsJSON class
		 * @type {BrokerUtilsJSON}
		 */
		BrokerUtils = require('./brokerutils-json');
	} else if ( configs.transport === 'xml' ) {
		/**
		 * Requires the BrokerUtilsXML class
		 * @type {BrokerUtilsXML}
		 */
		BrokerUtils = require('./brokerutils-xml');
	} else if( configs.transport === 'protobuf' ) {
		BrokerUtils = require('./brokerutils-protobuf');
	} else {
		console.log( 'Invalid transport type, defaulting to json...' );
		configs.transport = 'json';

		/**
		 * Requires the BrokerUtilsJSON class
		 * @type {BrokerUtilsJSON}
		 */
		BrokerUtils = require('./brokerutils-json');
	}

	if ( configs.callbacks == null ){
		if( this.configs.callbacks != null ) {
			configs.callbacks = this.configs.callbacks;
		} else {
			configs.callbacks = {};
		}
	}

	if ( configs.return == null ){
		if( this.configs.return != null ) {
			configs.return = this.configs.return;
		} else {
			configs.return = 'xml';
		}
	}

	if ( configs.return !== 'xml' && configs.return !== 'js' && configs.return !== 'json' ){
		winston.warn( 'Invalid return format (xml|js|json). Defaulting to JS object...' );
		configs.return = 'js';
	}

	this.configs.transport = configs.transport;
	this.configs.return    = configs.return;
	this.configs.callbacks = configs.callbacks;

	winston.info( 'Transport Format: '   +configs.transport );
	winston.info( 'Return Format: '      +configs.return );
	winston.info( 'Consumer Callbacks: ' +util.inspect( configs.callbacks ) );
};

/**
 * Connects the socket to the Broker with the given configurations
 */
SapoBroker.prototype.connect = function()
{
	BrokerClient.prototype.connect.call( this );

	if ( this.isReconnecting === true ) {
		for ( var sub in this.subMap ) {
			this.subscribe ( this.subMap[sub].name, this.subMap[sub].type, this.subMap[sub].ackMode );
		}
		for ( var queue in this.pollMap ) {
			message = { destinationName: this.pollMap[queue] };
			this.poll( message );
		}

		this.isReconnecting = false;
	}
};

/**
 * Subscribes to a given TOPIC / QUEUE and adds it to the subMap
 * @param  {string} destinationName The name of the TOPIC / QUEUE to subscribe
 * @param  {string} destinationType TOPIC, QUEUE or VIRTUAL_QUEUE
 * @param  {string} ackMode         The type of acknowledge mode (Default: AUTO)
 */
SapoBroker.prototype.subscribe = function( message, ackMode )
{
	/**
	 * The message that the client will write to the Broker
	 * @type {string}
	 * @private
	 */
	var msgSubscribe;

	winston.info( 'Subscribing ' + message.destinationName + ' as ' + message.destinationType + ', Acknowledge Mode is ' + ackMode );

	// Default values
	destinationType = ( message.destinationType != null ) ? message.destinationType : 'TOPIC';
	ackMode         = ( ackMode != null ) ? ackMode : 'AUTO';

	msgSubscribe = BrokerUtils.makeSubscribe( message );
	/*
		msgSubscribe = {
			 action: {
				 action_type: 'SUBSCRIBE',
				 subscribe : {
					 destination     : message.destinationName,
					 destination_type: message.destinationType
				 }
			 }
		};
	*/
	this.write( msgSubscribe );

	if ( this.subMap[ message.destinationName ] == null ) {
		this.subMap[ message.destinationName ] = {
			name    : message.destinationName,
			type    : message.destinationType,
			ackMode : ackMode
		};
	}
};

/**
 * Unsubscribes to a given TOPIC / QUEUE and removes it from the subMap
 * @param {string} destinationName The name of the TOPIC / QUEUE to unsubscribe
 */
SapoBroker.prototype.unsubscribe = function( message )
{
	/**
	 * The message that the client will write to the Broker
	 * @type {string}
	 * @private
	 */
	var msgUnsubscribe;

	winston.info( 'Unsubscribing ' + message.destinationName );

	if ( this.subMap[ message.destinationName ] == null ) {
		return;
	}

	msgUnsubscribe = BrokerUtils.makeUnsubscribe( message.destinationName );
	this.write ( msgUnsubscribe );

	delete this.subMap[ message.destinationName ];
};

/**
 * Send an acknowledge receipt to the Broker
 * @param  {string} messageId       The ID of the message
 * @param  {string} destinationName The name of the TOPIC / QUEUE to send the acklowledge to
 */
SapoBroker.prototype.acknowledge = function( message )
{
	/**
	 * The message that the client will write to the Broker
	 * @type {string}
	 * @private
	 */
	var msgAcknowledge;

	winston.info( 'Sending acknowledge to '+message.destinationName+' of '+message.messageId );

	msgAcknowledge = BrokerUtils.makeAcknowledge( messsage.messageId, message.destinationName );
	this.write( msgAcknowledge );
};

/**
 * Sends an enqueue message to the Broker
 * @param  {string} message The message to be sent to the broker
 */
SapoBroker.prototype.enqueue = function( message )
{
	if ( this.configs.transport === 'json' ) {
		console.log( 'This method is not available in JSON transport. Please send a Publish with a DestinationType instead.' );
		return;
	}

	this.sendEvent( 'Enqueue', message );
};

/**
 * Sends a publish message to the Broker
 * @param  {string} message The message to be sent to the broker
 */
SapoBroker.prototype.publish = function( message )
{
	this.write(BrokerUtils.makePublish(message));

	//this.sendEvent( 'Publish', message );
	if ( this.configs.transport === 'xml' ) {
		
	} else {
		/**
		 * The message that the client will write to the Broker
		 * @type {string}
		 * @private
		 */
		//var msgPublish;

		//winston.info( 'Sending a Publish' );
		//msgPublish = BrokerUtils.makePublish( message );
		//this.write( msgPublish );
	}
};

/**
 * Sends a poll message to the Broker
 * @param  {string} queue The name of the QUEUE to poll
 */
SapoBroker.prototype.poll = function( message )
{
	/**
	 * The message that the client will write to the Broker
	 * @type {string}
	 * @private
	 */
	var msgPoll;

	winston.info( 'Polling ' + message.destinationName );

	msgPoll = BrokerUtils.makePoll( message );
	this.write( msgPoll );

	this.pollMap[ message.destinationName ] = 1;
};

/**
 * Composes and sends an Enqueue or Publish message to the Broker
 * @param  {string} messageType Enqueue or Publish
 * @param  {string} message     The message to be sent
 */
SapoBroker.prototype.sendEvent = function( messageType, message )
{
	/**
	 * The message that the client will write to the Broker
	 * @type {string}
	 * @private
	 */
	var msgEvent;

	winston.info( 'Sending ' + messageType );

	msgEvent = BrokerUtils.makeEvent( messageType, message );
	this.write( msgEvent );
};

SapoBroker.prototype.newComposeData = function(){
	//First thing to do is to check if we have a complete broker message.
	if(this.composeBuffer.length >= 8){
		//The first 8 bytes are the encoding type (2bytes) + encoding version (2bytes) + bodyLength (Int 4bytes)
		var encodingType = this.composeBuffer.readInt16BE(0),
			encodingVersion = this.composeBuffer.readInt16BE(2),
			bodyLength = this.composeBuffer.readInt32BE(4);
		if(ENCODING_TYPES.indexOf( encodingType ) !== -1
			&& encodingVersion === ENCODING_VERSION
			&& !this.chunkDamaged( bodyLength )){
			if(this.composeBuffer.length == bodyLength + 8){
				//We have received a complete message:
				var body = this.composeBuffer.slice( 8, bodyLength + 8 ); //Slice works with endIndex not count
				if ( this.configs.transport === 'json' ) {
					this.parseJSON( body.toString() );
				} else if(this.configs.transport === 'xml'){
					this.parseXML( body.toString() );
				} else if(this.configs.transport === 'protobuf'){
					this.parseProtobuf(body);
				} else {
					winston.error('Invalid or unspecified protocol: ' + this.configs.transport);
					this.composeBuffer = new Buffer( 0 );	
				}
				this.composeBuffer = new Buffer( 0 );
			} else if(this.composeBuffer.length > bodyLength + 8){
				//Some error occured, reset the buffer.
				this.composeBuffer = new Buffer( 0 );
			}
		} else {
			winston.error('Invalid packet received.');
			this.composeBuffer = new Buffer( 0 );
		}
	}
};

/**
 * Composes the chunk parts received through the socket
 * @see SapoBroker.onData, SapoBroker.parseData, SapoBroker.returnData, SapoBroker.chunkDamaged
 */
SapoBroker.prototype.composeData = function()
{
	this.newComposeData();
	winston.info( 'Composing received data' );

	/**
	 * While the compose buffer isn't empty, try to compose chunks
	 */
	while ( this.composeBuffer.length !== 0 ) {

		/**
		 * If the client received the rest of a chunk OR if the compose buffer is larger than 8 and the header is valid
		 */
		if ( this.savedChunkData != null
			|| 	this.composeBuffer.length >= 8
			&& ENCODING_TYPES.indexOf( this.composeBuffer.readInt16BE( 0 ) ) !== -1
			&& this.composeBuffer.readInt16BE( 2 ) === ENCODING_VERSION
//			&& this.composeBuffer.readInt32BE( 4 ) <= MAX_LENGTH
		) {

			/**
			 * The real length of the chunk gotten from the header
			 * @type {int}
			 * @private
			 */
			var chunkLength;

			/**
			 * If it was waiting for the rest of a chunk, assume previous values, else, get length
			 */
			if ( this.savedChunkData != null ) {
				chunkLength = this.savedChunkData.length;
			} else {
				chunkLength = this.composeBuffer.readInt32BE( 4 ) + 8;

				/**
				 * Checks if length is valid, else, discard binary
				 */
				if ( this.chunkDamaged( chunkLength ) ){
					this.composeBuffer = this.composeBuffer.slice( 2 );
					break;
				}
			}

			/**
			 * If the length of the received data is larger than the chunk length, proceed
			 */
			if ( this.composeBuffer.length >= chunkLength ) {
				/**
				 * The sliced chunk according to the header length
				 * @type {buffer}
				 */
				var bufferSliced = this.composeBuffer.slice( 8, chunkLength ),

				/**
				 * The remaining data after the chunk was sliced
				 * @type {buffer}
				 */
				bufferRemaining = this.composeBuffer.slice( chunkLength );


				this.savedChunkData = null;

				/**
				 * If there are no more chunks in the remaining data, or if it preceeds a packet header
				 */
				if ( bufferRemaining[0] == null
					|| 	bufferRemaining.length >= 8
					&& ENCODING_TYPES.indexOf( bufferRemaining.readInt16BE( 0 ) ) !== -1
					&& bufferRemaining.readInt16BE( 2 ) === ENCODING_VERSION
//					&& bufferRemaining.readInt32BE( 4 ) <= MAX_LENGTH
				) {
					/**
					 * Set compose buffer as the remaining data, convert the sliced chunk to string and send to SapoBroker.parseJSON / XML
					 */

					this.composeBuffer = bufferRemaining;

					if ( this.configs.transport === 'json' ) {
						this.parseJSON( bufferSliced.toString() );
					} else {
						this.parseXML( bufferSliced.toString() );
					}
				} else {
					/**
					 * Else, chunk might be broken; check the length of the remaining data - if less than eight octets, wait for the rest
					 *
					 */
					if ( bufferRemaining.length < 8 ) {
						break;
					}

					/**
					 * Else, chunk is broken
					 */
					winston.warn( 'Chunk is broken. Getting the next one...' );

					/**
					 * Checks if has been a slice to the data
					 * @type {Boolean}
					 */
					var sliceTrigger = false;

					/**
					 * For the length of the received data, ignoring the first two octets, try to find the next chunk
					 * @type {Number}
					 */
					for ( var n = 2; n < this.composeBuffer.length; n++ ) {

						/**
						 * If header is found
						 */
						if ( ENCODING_TYPES.indexOf( this.composeBuffer.readInt16BE( n ) ) !== -1 ) {
							/**
							 * If header is complete
							 */
							if ( this.composeBuffer.readInt16BE( n + 2 ) === ENCODING_VERSION /* && this.composeBuffer.readInt32BE( n + 4 ) <= MAX_LENGTH */ ) {
								this.composeBuffer = this.composeBuffer.slice( n );
								sliceTrigger = true;

								break;
							} else {
								/**
								 * Otherwise, if it is the last octet, wait for more data
								 */
								if ( this.composeBuffer[ n + 1 ] == null ) {
									this.composeBuffer = this.composeBuffer.slice( n );
									sliceTrigger = true;

									break;
								}
							}
						}
					}

					/**
					 * If no chunk is found, clear compose buffer
					 */
					if ( sliceTrigger === false ) {
						this.composeBuffer = new Buffer(0);
					}
				}
			} else {
				/**
				 * Else, save data in savedChunkData
				 * @type {Object}
				 */

				this.savedChunkData = {
					length: chunkLength
				};

				winston.info( 'Chunk is not complete. The client will wait for the rest' );

				break;
			}
		} else {
			/**
			 * Same code as above, check the comments there
			 */

			if ( this.composeBuffer.length < 8 ) {
				break;
			}

			winston.warn( 'Chunk is broken. Getting the next one...' );

			for ( var n = 8; n < this.composeBuffer.length; n++ ) {

				if ( ENCODING_TYPES.indexOf( this.composeBuffer.readInt16BE( n ) ) !== -1 ) {

					if ( this.composeBuffer.readInt16BE( n + 2 ) === ENCODING_VERSION /* && this.composeBuffer.readInt32BE( n + 4 ) <= MAX_LENGTH */ ) {
						this.composeBuffer = this.composeBuffer.slice( n );
						sliceTrigger = true;

						break;
					} else {

						if( this.composeBuffer[ n + 1 ] == null ){
							this.composeBuffer = this.composeBuffer.slice( n );
							sliceTrigger = true;

							break;
						}
					}
				}
			}

			if ( sliceTrigger === false ) {
				this.composeBuffer = new Buffer( 0 );
			}
		}
	}
};

/**
 * Checks if the chunk is damaged through its length
 * @param  {int} length The length of a chunk
 * @return {boolean}
 */
SapoBroker.prototype.chunkDamaged = function( length ){

	if( length === 0 || length == null ){

		winston.warn( 'Socket returned a damaged chunk' );

		return true;
	} else {
		return false;
	}
};

/**
 * Parses the composed chunks, sending acknowledges if needed, updating the pollMap, and finally sending data to SapoBroker.returnData
 * @param  {string} data The composed SOAP chunk
 */
SapoBroker.prototype.parseXML = function( data )
{
	/**
	 * The various fields of the received data
	 */

	/**
	 * The converted chunk to an object
	 * @type {object}
	 * @private
	 */
	var dataObject,

		/**
		 * The name of the TOPIC / QUEUE where the data came from
		 * @type {string}
		 * @private
		 */
			destinationName,

		/**
		 * The type of data, TOPIC, QUEUE or VIRTUAL_QUEUE
		 * @type {string}
		 * @private
		 */
			destinationType,

		/**
		 * The name of the subscription that led to the received message
		 * @type {string}
		 * @private
		 */
			messageDestinationName,

		/**
		 * The ID of the message
		 * @type {string}
		 * @private
		 */
			messageId,

		/**
		 * The options object passed to the XML parser
		 * @type {object}
		 * @private
		 */
			options;

	options = {
		object     : true,
		reversible : false,
		coerce     : false,
		sanitize   : false,
		trim       : false
	};

	try {
		winston.info( 'Parsing chunk' );

		dataObject = parser.toJson(data, options);

		destinationName        = dataObject['soap:Envelope']['soap:Header']["wsa:To"];
		destinationType        = dataObject['soap:Envelope']['soap:Header']['wsa:From']['wsa:Address'];
		messageId              = dataObject['soap:Envelope']['soap:Body']['mq:Notification']['mq:BrokerMessage']['mq:MessageId'];
		messageDestinationName = dataObject['soap:Envelope']['soap:Body']['mq:Notification']['mq:BrokerMessage']['mq:DestinationName'];

		winston.info( 'Chunk Info: FROM: ' + messageDestinationName + ' | TYPE: ' + destinationType +' | MESSAGEID: ' + messageId );

		if ( this.subMap[destinationName] != null
			&& ( this.subMap[destinationName].type === 'QUEUE' || this.subMap[destinationName].type === 'VIRTUAL_QUEUE' )
			&& this.subMap[destinationName].ackMode === 'AUTO'
		) {
			this.acknowledge( {messageId: messageId, destinationName: messageDestinationName} );
		}

		if ( this.pollMap[destinationName] != null ){
			delete this.pollMap[destinationName];
		}

		this.returnData( data, null, dataObject );
	} catch ( e ) {
		console.log( data );
		return false;
	}
};

/**
 * Parses the composed chunks, sending acknowledges if needed, updating the pollMap, and finally sending data to SapoBroker.returnData
 * @param  {string} data The composed JSON chunk
 */
SapoBroker.prototype.parseJSON = function( data )
{
	/**
	 * The various fields of the received data
	 */

	/**
	 * The converted chunk to an object
	 * @type {object}
	 * @private
	 */
	var dataObject,

		/**
		 * The name of the TOPIC / QUEUE where the data came from
		 * @type {string}
		 * @private
		 */
			destinationName,

		/**
		 * The type of data, TOPIC, QUEUE or VIRTUAL_QUEUE
		 * @type {string}
		 * @private
		 */
			destinationType,

		/**
		 * The name of the subscription that led to the received message
		 * @type {string}
		 * @private
		 */
			messageDestinationName,

		/**
		 * The ID of the message
		 * @type {string}
		 * @private
		 */
			messageId;

	try {
		winston.info( 'Parsing chunk' );

		dataObject = JSON.parse( data );

		destinationName        = dataObject.action.notification.subscription;
		destinationType        = dataObject.action.notification['destination_type'];
		messageId              = dataObject.action.notification.message['message_id'];
		messageDestinationName = dataObject.action.notification.destination;

		winston.info( 'Chunk Info: FROM: ' + messageDestinationName + ' | TYPE: ' + destinationType + ' | MESSAGEID: ' + messageId );

		if ( this.subMap[destinationName] != null
			&& ( this.subMap[destinationName].type === 'QUEUE' || this.subMap[destinationName].type === 'VIRTUAL_QUEUE' )
			&& this.subMap[destinationName].ackMode === 'AUTO'
			) {
			this.acknowledge( messageId, messageDestinationName );
		}

		if ( this.pollMap[destinationName] != null ){
			delete this.pollMap[destinationName];
		}

		this.returnData( null, data, dataObject );
	} catch ( e ) {
		console.log('Error caught: ', e);
		console.log( data );
		return false;
	}
};

SapoBroker.prototype.parseProtobuf = function(dataBuff){
	try {
		this.returnData(null, BrokerUtils.decodeMsg(dataBuff), null);
	} catch( e ) {
		console.log('Error caught: ', e);
		return false;
	}
};

/**
 * Returns the parsed data to the consumer callback in the requested format
 * @param  {string} xmlData The original XML data
 * @param  {string} jsonData The original JSON data
 * @param  {object} jsData  The data in object format
 */
SapoBroker.prototype.returnData = function( xmlData, jsonData, jsData )
{
	/**
	 * The data to return
	 * @private
	 */
	var dataToReturn;

	winston.info( 'Sending formatted chunk to consumer callback' );

	switch ( this.configs.return ) {
		case 'js':
			dataToReturn = jsData;
			break;
		case 'xml':
			if ( xmlData == null ) {
				xmlData = parser.toXml( jsonData, { object:false, reversible:true, coerce:false, sanitize:false, trim:false } );
			}
			dataToReturn = xmlData;
			break;
		case 'json':
		default:
			if ( jsonData == null ) {
				jsonData = parser.toJson( xmlData, { object:false, reversible:true, coerce:false, sanitize:false, trim:false } );
			}
			dataToReturn = jsonData;
			break;
	}

	if ( typeof( this.configs.callbacks.onData ) === 'function' ) {
		this.configs.callbacks.onData( dataToReturn );
	}
};

/**
 * Adds the message header prefix and sends the message to the Broker through the socket
 * @param  message The message that the consumer wishes to send to the Broker
 */
SapoBroker.prototype.write = function( message )
{
	/**
	 * The buffer write prefix that contains the header of the message
	 * @type {buffer}
	 * @private
	 */
	var bufferPrefix;

	bufferPrefix = new Buffer( 8 );

	if ( this.configs.transport === 'json' ) {
		bufferPrefix.writeInt16BE( 3, 0 );
	} else if ( this.configs.transport === 'xml' ) {
		bufferPrefix.writeInt16BE( 0, 0 );
	} else if ( this.configs.transport === 'protobuf' ) {
		bufferPrefix.writeInt16BE( 1, 0 );
	}

	bufferPrefix.writeInt16BE( 0, 2 );
	bufferPrefix.writeInt32BE( message.length, 4 );
	this.socket.write( bufferPrefix + message );
};

/**
 * Calls the BrokerClient.onConnect event handler and the consumer callback
 */
SapoBroker.prototype.onConnect = function()
{
	BrokerClient.prototype.onConnect.call( this );

	this.errorCounter = 0;

	if ( typeof( this.configs.callbacks.onConnect ) === 'function' ) {
		this.configs.callbacks.onConnect();
	}
};

/**
 * Calls the BrokerClient.onData event handler and sends the data to the SapoBroker.composeData for parsing
 * @see SapoBroker.composeData
 * @param  {buffer} data Data received
 */
SapoBroker.prototype.onData = function( data )
{
	BrokerClient.prototype.onData.call( this, data );

	this.composeBuffer = Buffer.concat( [ this.composeBuffer, data ] );

	//this.composeData();
	this.newComposeData();
};

/**
 * Calls the BrokerClient.onEnd event handler and the consumer callback
 */
SapoBroker.prototype.onEnd = function()
{
	BrokerClient.prototype.onEnd.call( this );

	if ( typeof( this.configs.callbacks.onEnd ) === 'function' ) {
		this.configs.callbacks.onEnd();
	}
};

/**
 * Calls the BrokerClient.onTimeout event handler and the consumer callback
 */
SapoBroker.prototype.onTimeout = function()
{
	BrokerClient.prototype.onTimeout.call( this );

	if ( typeof( this.configs.callbacks.onTimeout ) === 'function' ) {
		this.configs.callbacks.onTimeout();
	}
};

/**
 * Calls the BrokerClient.onDrain event handler and the consumer callback
 */
SapoBroker.prototype.onDrain = function()
{
	BrokerClient.prototype.onDrain.call( this );

	if ( typeof( this.configs.callbacks.onDrain ) === 'function' ) {
		this.configs.callbacks.onDrain();
	}
};

/**
 * Calls the BrokerClient.onError event handler, tries to reconnect the socket 5 times and calls the consumer callback
 * @param  {string} error The description of the error
 */
SapoBroker.prototype.onError = function( error )
{
	BrokerClient.prototype.onError.call( this, error );

	if( this.errorCounter >= 5 ){
		winston.info( 'Connection failed 5 times. Check the host or the port.');
	} else {
		this.errorCounter++;
		this.reconnect();

		winston.info( '#'+this.errorCounter+' Attempting reconnection...' );
	}

	if ( typeof( this.configs.callbacks.onError ) === 'function' ) {
		this.configs.callbacks.onError( error );
	}
};

/**
 * Calls the BrokerClient.onClose event handler and the consumer callback
 * @param  {boolean} hadError A boolean that shows whether the socket had an error
 */
SapoBroker.prototype.onClose = function( hadError )
{
	BrokerClient.prototype.onClose.call( this, hadError );

	if ( typeof( this.configs.callbacks.onClose ) === 'function' ) {
		this.configs.callbacks.onClose( hadError );
	}
};
