namespace java pt.com.thrift

enum DestinationType
{
	TOPIC = 0;
	QUEUE = 1;
	VIRTUAL_QUEUE = 2;		
}

enum ActionType
{
	PUBLISH = 0;
	POLL = 1;
	ACCEPTED = 2;
	ACKNOWLEDGE_MESSAGE = 3;
	SUBSCRIBE = 4;
	UNSUBSCRIBE = 5;
	NOTIFICATION = 6;
	FAULT = 7;
	PING = 8;
	PONG = 9;
}

struct Header
{
	1: map<string, string> parameters;
}

struct BrokerMessage
{		
	1: optional string message_id;
	2: binary payload;		
	3: optional i64 expiration;
	4: optional i64 timestamp;
}

struct Publish
{
	1: optional string action_id;
	2: DestinationType destination_type;
	3: string destination;
	4: BrokerMessage message;
}

struct Poll
{
	1: optional string action_id;
	2: string destination;
}	

struct Accepted
{
	1: string action_id;
}

struct AcknowledgeMessage
{
	1: optional string action_id;
	2: string message_id;
	3: string destination;
}

struct Subscribe
{
	1: optional string action_id;
	2: string destination;
	3: DestinationType destination_type;
}

struct Unsubscribe
{
	1: optional string action_id;
	2: string destination;
	3: DestinationType destination_type;
}

struct Notification
{
	1: string destination;
	2: string subscription;
	3: DestinationType destination_type;
	4: BrokerMessage message;
}

struct Fault
{
	1: optional string action_id;
	2: string fault_code;
	3: string fault_message;
	4: optional string fault_detail;
}

struct Ping
{
	1: i64 timestamp;
}

struct Pong
{
	1: i64 timestamp;
}	
 	
struct Action
{				
	1: optional Publish publish;
	2: optional Poll poll;
	3: optional Accepted accepted;
	4: optional AcknowledgeMessage ack_message;
	5: optional Subscribe subscribe;
	6: optional Unsubscribe unsubscribe;
	7: optional Notification notification;
	8: optional Fault fault;
	9: optional Ping ping;
	10: optional Pong pong;		
	11: ActionType action_type;
}

struct ThriftMessage
{	
	1: optional Header header;
	2: Action action;
}
