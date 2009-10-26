function jsTestInit() 
{
	// This are the available channels
	var publishChannels = new Array("/topic/foo", "/topic/xml");
	var subscribeChannels = new Array("/topic/foo","/topic/system", "/topic/json", "/blogs/post");

	// Add available channels to subscription and publication select elements
	var channelsToSubscribe = s$('channelsToSubscribe');
	var publicationChannels = s$('publicationChannels');
	
	for( var idx in publishChannels)
	{
		addOptionToSelectElement(publicationChannels, publishChannels[idx], publishChannels[idx]);
	}
	
	for( var idx in subscribeChannels)
	{
		addOptionToSelectElement(channelsToSubscribe, subscribeChannels[idx], subscribeChannels[idx]);
	}
	

	var receivedMessagesList = new Array();
	var receivedMessagesListIdx = -1;
	var receivedMessages=s$('receivedMessages');

	// Add subscription logic
	var subscribeChannels = s$('subscribeChannels');
	subscribeChannels.onclick = function(msg){
		var subscribedChannels = s$('subscribedChannels');
		// Find new subscriptions		
		var subscribeChannels = s$('channelsToSubscribe');
		
		switchSelectedElements(subscribeChannels, subscribedChannels, function(channelName){
			getBridge().subscribe(channelName, function(msg) {
				var MAX_ITEMS = 10;
				++receivedMessagesListIdx;
				if(receivedMessagesListIdx == MAX_ITEMS)
					receivedMessagesListIdx = 0;
				//sampleAddHtml(msg);
				
				receivedMessagesList[ receivedMessagesListIdx ] = msg;

				var newText = "";
				if(receivedMessagesListIdx +1 != MAX_ITEMS)
				{
					var receivedLen = receivedMessagesList.length;
					for( var i = receivedMessagesListIdx+1 ; i != receivedLen ; ++i)
					{
						newText =  receivedMessagesList[i].data.subscription + ": " + receivedMessagesList[i].data.data +"\n" + newText;
					}
				}
				for( var i = 0; i != receivedMessagesListIdx+1; ++i)
				{
					newText = receivedMessagesList[i].data.subscription + ": " + receivedMessagesList[i].data.data +"\n" + newText;
				}
				
				receivedMessages.value = newText;
			});
		});
		

		return false;
	}

	// Add unsubscription logic
	var unsubscribeChannels = s$('unsubscribeChannels');
	unsubscribeChannels.onclick = function(msg){
		var subscribedChannels = s$('subscribedChannels');
		// Find new subscriptions		
		var subscribeChannels = s$('channelsToSubscribe');
		
		switchSelectedElements(subscribedChannels, subscribeChannels, function(channelName){
			getBridge().unsubscribe(channelName);
		});
		

		return false;
	}
	
	// Add publication logic
	element = s$('publishBtn');
	element.onclick = function(msg){
		var publicationChannels	= s$('publicationChannels');
		var publishText = s$('publishText');

		getBridge().publish(publicationChannels.value, publishText.value);
		return false;
	}
}


function sampleAddHtml(message)
{
	//var data = response.evalJSON()
	//var htmlPlaceholder = s$('htmlContent');
	//htmlPlaceholder.innerHTML = message.data.data.
}


var sbJsBridge= new sbJsBridge();
function getBridge()
{
	if(!sbJsBridge.connected)
		sbJsBridge.connect("localhost", 8888);
	return sbJsBridge;
}


// user interface auxiliary functions


function addOptionToSelectElement(selectElement, optionValue, optionText)
{	
	selectElement.options[selectElement.length] = new Option(optionValue, optionText);
}

function removeOptionFromSelectElement(selectElement, optionValue)
{
	for(var idx in selectElement.options)
	{
		if(selectElement.options[idx].value === optionValue)
		{
			selectElement.remove(idx);
			return;
		}
	}
}

// onChange signature: onChange(valueOfOption)
function switchSelectedElements(fromSelectElement, toSelectElement, onChange)
{
	var newSubscriptions = new Array();
	for(var idx = 0; idx < fromSelectElement.length; ++idx)
	{
		if(fromSelectElement.options[idx].selected)
		{
			newSubscriptions[newSubscriptions.length] = fromSelectElement.options[idx].value;
			// Add selected channel to the correspondent list
			addOptionToSelectElement(toSelectElement, fromSelectElement.options[idx].value, fromSelectElement.options[idx].value);
		}
	}
	
	for( var idx in newSubscriptions)
	{
		// perform action
		onChange(newSubscriptions[idx]);
		
		// Remove elements from original select element
		removeOptionFromSelectElement(fromSelectElement, newSubscriptions[idx]);
	}
}

window.onload = jsTestInit;
