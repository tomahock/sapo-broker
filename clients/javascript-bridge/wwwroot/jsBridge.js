function jsBridge()
{
	if (!window.console) console = {log: function(){ }, error: function(){ }};

	// private properties
	var ws = null;
  	var faultCallback = null;

	//public properties
	this.connected = false;
	
	this.reconnecting = false;
	this.intervalHandle = null; //TODO: make this private

	this.subscriptions = {};

	this.SUBSCRIBE = "SUBSCRIBE";
	this.UNSUBSCRIBE = "UNSUBSCRIBE";
	this.PUBLISH = "PUBLISH";
	this.NOTIFICATION = "NOTIFICATION";
	this.FAULT = "FAULT";

	this.self = this;

	function JsonMessage(action, channel, payload)
	{
		this.action = action
		this.channel = channel;
		if( !( payload === undefined) )
		{
			this.payload = payload;
		}
	}

	/** CONNECT **/
	this.connect = function()
	{
		var jsBridgeConnected = this.connected;

		var jsBridgeSelf = this.self;

		faultCallback = function(faultMessage) {
			alert('Error(' + faultMessage.Code + ')! ' + faultMessage.Message);
		}
		
		var ws_host = "ws://" + window.location.host + "/websocket";

		if(!jsBridgeSelf.connected)
		{
			console.log("[sbWSlib] connecting WS");
		} else {
			console.log("[sbWSlib] reconnecting WS");
		}	

		this.ws = new WebSocket(ws_host);

		this.ws.onopen = function () {
			jsBridgeSelf.connected = true;
			if(jsBridgeSelf.reconnecting)
			{
				console.log("[sbWSlib] reconnected");
				
				jsBridgeSelf.reconnecting = false;
				window.clearInterval(jsBridgeSelf.intervalHandle);
				jsBridgeSelf.intervalHandle = null;

				for(subscriptionChannel in jsBridgeSelf.subscriptions)
				{
					var subscribe = new JsonMessage(jsBridgeSelf.SUBSCRIBE, subscriptionChannel);
					var _message = JSON.stringify( subscribe );
					console.log("[sbWSlib] sending subscription message: " + _message);
					jsBridgeSelf.ws.send(_message);
				}
			}
    		};
		
		this.ws.onclose = function ()
		{
			console.log("[sbWSlib] closed");
			if(jsBridgeSelf.reconnecting)
			{
				return;
			}
			jsBridgeSelf.connected = false;
			jsBridgeSelf.reconnecting = true;
			console.log("[sbWSlib] Setting up reconnection");
			jsBridgeSelf.intervalHandle = window.setInterval( function() { jsBridgeSelf.connect(); }, 1000 );
		}		
		
		this.ws.onmessage = function(event)
		{
			var _message = JSON.parse( event.data );
			if(_message.action === undefined)
			{
				//action is not defined. Ignore this message
				return;
			}

			if( _message.action == this.FAULT)
			{
				faultCallback(_message.payload);
			} 
			else
			{
			  if(_message.channel === undefined)
			  {
				//action is not defined. Ignore this message
				return;
			  }
			  var callback = jsBridgeSelf.subscriptions[_message.channel];
			  callback(_message);
			}
		};
	}
	
	/** DISCONNECT **/
	this.disconnect = function()
	{
		if( !this.connected )
		{
			alert('To disconnect you need to be connected.');
			return false;
		}
		this.connected = false;
		this.ws.close();
	}

	this.registerFaultCallback = function( callback )
	{
		this.faultCallback = callback;
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

		var subscribe = new JsonMessage(this.SUBSCRIBE, channel);
		var _message = JSON.stringify( subscribe );
		this.ws.send(_message);

		this.subscriptions[channel] = callback;

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

		var unsubscribe = new JsonMessage(this.UNSUBSCRIBE, channel);
		var _message = JSON.stringify( unsubscribe );
		this.ws.send(_message);
		
		delete this.subscriptions[channel];

		return true;
	}

	/** PUBLISH **/
	this.publish = function(channel, content)
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
		
		if( (channel.len == 0) || (content.len == 0) )
		{
			channel.write(fault);
		
			alert('To publish, please specify a valid channel name and the text to send.');
			return false;
		}

		var publish = new JsonMessage(this.PUBLISH, channel, content);
		var _message = JSON.stringify( publish );

		this.ws.send(_message);

		return true;
	}
}
