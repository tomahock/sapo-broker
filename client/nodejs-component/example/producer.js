var SapoBroker = require ('../src/sapobroker');

var client = new SapoBroker();

function brokerConsumerInit()
{
	client.init( {
		port      : 3323, 
		host      : 'localhost',
		transport : 'protobuf',
		return    : 'json',
		callbacks : 
		{
			onConnect: publishMessages
		}
	} );
	client.connect();
}

function publishMessages(){
	for(var i = 0; i < 100; i++){
		console.log('Publishing message!');
		client.publish({
			'destinationType': 'TOPIC',
			'destinationName': '/sapo/broker/dev/test_node',
			'payload': 'Test Node from Node Producer. Yeah!'
		});
	}
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