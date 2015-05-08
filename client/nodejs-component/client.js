var SapoBroker = require ('./src/sapobroker');

function brokerConsumerInit()
{
	var client;

	client = new SapoBroker();

	client.init( {
		port      : 3323, 
		host      : 'localhost',
		transport : 'json',
		return    : 'js',
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
	console.log( data );
} 

function init()
{
	brokerConsumerInit();
}

init();