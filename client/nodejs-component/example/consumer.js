var SapoBroker = require ('../src/sapobroker');
var utf8 = require('utf8');

function brokerConsumerInit()
{
	var client;

	client = new SapoBroker();

	client.init( {
		port      : 3323, 
		host      : '127.0.0.1',
		transport : 'protobuf',
		return    : 'json',
		callbacks : 
		{
			onConnect: null,
			onData: brokerConsumerDataCb,
			onEnd: null,
			onTimeout: null,
			onDrain: null,
			onError: null,
			onClose: null
		}
	} );

	client.connect();

	client.subscribe( {destinationName: '/sapo/broker/dev/test_node', destinationType: 'TOPIC'} , null );
}

function brokerConsumerDataCb( data )
{
	console.log('Data:', data.action.notification.message);
	var msg = new Buffer(data.action.notification.message.payload, 'Base64').toString('utf8');
	console.log('Message: ', msg);
} 

function init()
{
	brokerConsumerInit();
}

init();