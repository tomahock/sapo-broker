dojo.require("dojox.cometd");

function sbJsBridge()
{
	// private properties
	var agentHostname = null;
	var agentPort = null;


	//public properties
	this.connected = false;
	this.subscriptions = {};
	
	/** CONNECT **/
	this.connect = function(hostname, port)
	{
		if( this.connected )
		{
			alert('Already connected.');
			return false;
		}		
		var url=new String(document.location).replace(/http:\/\/[^\/]*/,'').replace(/\/test\/.*$/,'')+"/cometd";
		//var url = dojox.cometd.init("http://127.0.0.1:9999/cometd");
		dojox.cometd.init(url);
		this.connected=true;
	}
	
	/** DISCONNECT **/
	this.disconnect = function()
	{
		if( !this.connected )
		{
			alert('To disconnect you need to be connected.');
			return false;
		}
		dojox.cometd.disconnect();
		this.connected=false;
	}

	/** SUBSCRIBE **/
	// callback signature:  function( message )
	this.subscribe = function(channel, callback)
	{
		if( !this.connected )
		{
			alert('Before subscribing, please connect.');
			return false;
		}
				
		if(arguments.length != 2 )
		{
			alert('To subscribe, please specify a channel name and a callback function.');
			return false;		
		}

		if( channel.length==0 )
		{
			alert('To subscribe, please specify a valid channel name.');
			return false;
		}

		if( ! (	this.subscriptions[channel] === undefined) )
		{
			alert( channel + ' has already been subscribed.');
			return false;
		}
		
		var h = dojox.cometd.subscribe(channel, callback);
		h.addCallback(function(){
		    console.debug("subscription to channel established");
		});

		this.subscriptions[channel] = h;

		return true;
	}
	
	/** UNSUBSCRIBE **/
	this.unsubscribe = function(channel)
	{
		if( !this.connected )
		{
			alert('To unsubscribe you must be connected.');
			return false;
		}
		
		if( this.subscriptions[channel] === undefined )
		{
			alert( channel + ' has not been subscribed.');
			return false;
		}
		var h = this.subscriptions[channel];
		dojox.cometd.unsubscribe(h);
		delete this.subscriptions[channel];

		return true;
	}
	/** PUBLISH **/
	this.publish = function(channel, text)
	{
		if( !this.connected )
		{
			alert('To publish you must be connected.');
			return false;
		}

		if(arguments.length != 2 )
		{
			alert('To publish, please specify a channel name and the text to send.');
			return false;		
		}
		
		if( (channel.len == 0) || (text.len == 0) )
		{
			alert('To publish, please specify a valid channel name and the text to send.');
			return false;
		}
		
		dojox.cometd.publish(channel, { subscription: channel, data: text});

		return true;
	}
}
