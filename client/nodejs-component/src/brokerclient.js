/**
 * @fileOverview This file contains the BrokerClient class
 * @author <a href="mailto:jpmca@ua.pt">João Abreu</a>
 * @version 0.1.0
 */

/**
 * Requires the NodeJS net module
 * @type {net}
 */
var net = require('net'),
/**
 * Requires the winston module for logging
 * @type {winston}
 */
	winston = require('../node_modules/winston');


/**
 * Exports class as BrokerClient
 */
module.exports = BrokerClient;

/**
 * Sets self as reference to the BrokerClient scope
 * @class Represents a basic Broker client with basic methods based on NodeJS net module events
 */
function BrokerClient()
{}

/**
 * Initializes the Broker client; that is, creates the socket and sets both the configurations and the internal handlers
 * @param  {object} configs Configuration object passed by a consumer
 * @property {net.Socket} socket The socket that connects to the Broker
 * @property {object} configs The client configurations
 * @property {integer} configs.port The client port (Default: 6622)
 * @property {string} configs.host The client host (Default: '10.135.66.175')
 * @property {boolean} isReconnecting Boolean variable that shows whether the client is reconnecting
 * @example
 *
 * 	var client,
 *  	configs;
 *
 * 	configs = {
 * 		port: 6699,
 * 		host: '10.135.66.175',
 * 		transport: 'json'
 * 	}
 *
 * 	client = new BrokerClient();
 * 	client.init( configs );
 *
 */
BrokerClient.prototype.init = function ( configs ){
	winston.info( 'Initializing client' );

	this.socket = new net.Socket();
	this.socket.setKeepAlive(true);

	winston.info( 'Socket created' );

	this.configs = {};
	this.configure( configs );

	this.isConnected = false;

	this.setHandlers({
		connect: this.onConnect.bind(this),
		data:    this.onData.bind(this),
		end:     this.onEnd.bind(this),
		timeout: this.onTimeout.bind(this),
		drain:   this.onDrain.bind(this),
		error:   this.onError.bind(this),
		close:   this.onClose.bind(this)
	});
};

/**
 * Sets the socket configurations
 * @param  {object} configs Configuration object passed by a consumer
 */
BrokerClient.prototype.configure = function( configs )
{
	if( configs == null ){
		configs = {};
	}

	/**
	 * Sets default values
	 */

	if ( configs.port == null ){
		if( this.configs.port != null ) {
			configs.port = this.configs.port;
		} else {
			configs.port = 6623;
		}
	}
	if ( configs.host == null ){
		if( this.configs.host != null ) {
			configs.host = this.configs.host;
		} else {
			configs.host = '10.135.66.175'; //FIXME!
		}
	}

	this.configs.port = configs.port;
	this.configs.host = configs.host;

	if ( this.isConnected === true ) {
		this.reconnect();
	}

	winston.info( 'Port: ' + configs.port );
	winston.info( 'Host: ' + configs.host );
};

/**
 * Connects the socket to the Broker with the given configurations
 */
BrokerClient.prototype.connect = function()
{
	winston.info( 'Connecting to ' + this.configs.host + ':' + this.configs.port );

	this.socket.connect({ port: this.configs.port, host: this.configs.host });
};

/**
 * Reconnects the socket to the Broker
 * @see BrokerClient.onClose; BrokerClient.isReconnecting;
 */
BrokerClient.prototype.reconnect = function()
{
	winston.info( 'Reconnecting to '+this.configs.host+':'+this.configs.port );

	this.isReconnecting = true;
	this.socket.destroy();
};

/**
 * Disconnects the socket from the Broker
 */
BrokerClient.prototype.disconnect = function()
{
	winston.info( 'Disconnecting' );

	this.socket.destroy();
};

/**
 * Adds the message length prefix and sends the message to the Broker through the socket
 * @param  message The message that the consumer wishes to send to the Broker
 */
BrokerClient.prototype.write = function( message )
{
	/**
	 * The buffer write prefix that contains the length of the message in an unsigned 32 bit integer in endian format
	 * @type {buffer}
	 * @private
	 */
	var bufferPrefix;

	bufferPrefix = new Buffer( 4 );
	bufferPrefix.writeInt32BE( message.length, 0 );
	console.log('I DONT THINK THIS SHOULD BE HERE');
	this.socket.write( bufferPrefix + message );
};

/**
 * Sets the Broker client internal event handlers
 * @param {object} handlers An object with the internal event handlers
 * @param {function} handlers.onConnect The connect event handler
 * @param {function} handlers.onData The data event handler
 * @param {function} handlers.onEnd The end event handler
 * @param {function} handlers.onTimeout The timeout event handler
 * @param {function} handlers.onDrain The drain event handler
 * @param {function} handlers.onError The error event handler
 * @param {function} handlers.onClose The close event handler
 */
BrokerClient.prototype.setHandlers = function( handlers )
{
	for ( var key in handlers ){

		if ( handlers.hasOwnProperty( key ) ){

			if( typeof( handlers[key] ) === 'function' ){
				this.socket.on( key, handlers[key] );
			}
		}
	}
};

/**
 * The Broker socket connect event handler
 */
BrokerClient.prototype.onConnect = function()
{
	winston.info( 'Connection established to ' + this.configs.host + ':' + this.configs.port );

	this.isConnected = true;
};

/**
 * The Broker socket data event handler
 * @param  {buffer} data The buffer received through the socket
 */
BrokerClient.prototype.onData = function( data )
{
	winston.info( 'Received ' + data.length + ' bytes of data' );
};

/**
 * The Broker socket end event handler
 */
BrokerClient.prototype.onEnd = function()
{
	winston.info( 'Connection ended' );

	this.isConnected = false;
};

/**
 * The Broker socket timeout event handler
 */
BrokerClient.prototype.onTimeout = function()
{
	winston.info( 'Connection timeout' );

	this.isConnected = false;
};

/**
 * The Broker socket drain event handler
 */
BrokerClient.prototype.onDrain = function()
{
	winston.info( 'Write buffer empty' );
};

/**
 * The Broker socket error event handler
 * @param  {string} error The error message received
 */
BrokerClient.prototype.onError = function( error )
{
	winston.error( error );

	this.isConnected = false;
};

/**
 * The Broker socket close event handler
 * @param  {boolean} hadError A boolean that shows whether the socket had an error
 */
BrokerClient.prototype.onClose = function( hadError )
{
	winston.info( 'Connection closed' );
	winston.info( 'Had Error: '+hadError );

	this.isConnected = false;

	if( this.isReconnecting === true ){
		this.connect();
	}
};