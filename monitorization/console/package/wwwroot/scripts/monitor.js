
//
//  MAIN PAGE
//

var QUEUE_PREFIX = "queue://";

function mainMonitorizationInit() 
{
  // queues
  var f_queues = function() {
   new Ajax.Request('/dataquery/last?predicate=queue-size&minvalue=0',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queuesInformationPanel');
      var data = transport.responseJSON;      
      setQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...') }
   });  
  }
  // dropbox
  var f_dropboxes = function() {
   new Ajax.Request('/data/dropbox',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('dropboxInformationPanel');
      var data = transport.responseJSON;      
      setDropboxInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get dropbox info...') }
   });  
  }
  // faults
  var f_faults = function() {
   new Ajax.Request('/dataquery/fault?count=10',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('faultsInformationPanel');
      var data = transport.responseJSON;      
      setFaultInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get faults info...') }
   });  
  }

  // agents
  var f_agents = function() {
   new Ajax.Request('/dataquery/last?predicate=status',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('agentsDownInformationPanel');
      var data = transport.responseJSON;      
      setAgentsInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent status info...') }
   });  
  }

  f_queues();
  setInterval(f_queues, 3000);
  f_agents();
  setInterval(f_agents, 3000);
  f_faults();
  setInterval(f_faults, 3000);
 /* f_dropboxes();
  setInterval(f_dropboxes, 3000);
*/
}

var previousQueueInfo = new Object();

// general queue info
function setQueueInfo(queueInfo, panel)
{
	var newContent = "";

	if (queueInfo.length == 0)
	{
        	newContent = "<p>There is no queue info</P>";
  	}
	else
	{
		var queues = new Object();
		for(var i = 0; i != queueInfo.length; ++i)
		{
			var queueName = removePrefix(queueInfo[i].subject, QUEUE_PREFIX);
			var strCount = queueInfo[i].value;
			var count = parseFloat(strCount);
			var curDate = parseISO8601(queueInfo[i].time);
			//queues[queueName] += count;
			if( queues[queueName] === undefined)
			{	
				queues[queueName] = new Object();
				queues[queueName].count = count;
				queues[queueName].time = curDate;
			}
			else
			{
				queues[queueName].count += count;
			}
			if (curDate > queues[queueName].time)
			{
				queues[queueName].time = curDate;
			}
		}
		var content = "<table>";
		for(var queueName in queues)
		{
			var count = queues[queueName].count;
			var previousValue = previousQueueInfo[queueName];
			var pic = getLocalPic(previousValue, count);
			var curDate = queues[queueName].time;
			previousQueueInfo[queueName] = count;
			//newContent = newContent + "<p><a href='./queue.html?queuename="+queueName+"'>"+ queueName+ "</a> : " + count + "<img src='" + pic + "' /></p>";
			content = content + "<tr><td><a href='./queue.html?queuename="+queueName+"'>"+ queueName+ "</a></td><td style='text-align:right'>" +  count + "</td><td>" + getHumanTextDiff(curDate) +"</td><td><img src='"+ pic + "' /></td></tr>";
		}
		content += "</table>";
		newContent = content;
	}
	panel.innerHTML = newContent;
}

// general dropbox info
function setDropboxInfo(dropboxInfo, panel)
{
	var newContent = "";

	if (dropboxInfo.length == 0)
	{
        	newContent = "<p>There is no dropbox info</P>";
  	}
	else
	{
		for(var i = 0; i != dropboxInfo.length; ++i)
		{
			var agentname = dropboxInfo[i].agentName;
			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> : " + dropboxInfo[i].dropboxLocation + " : " + dropboxInfo[i].messages + " : " + dropboxInfo[i].goodMessages +"</p>";
		}
	}
	panel.innerHTML = newContent;
}
// general fault info
function setFaultInfo(faultInfo, panel)
{
	var newContent = "";

	if (faultInfo.length == 0)
	{
        	newContent = "<p>There is no faults info</P>";
  	}
	else
	{
		for(var i = 0; i != faultInfo.length; ++i)
		{
			var agentname = faultInfo[i].agentName;

			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> : <a href='./fault.html?faultid="+faultInfo[i].id+"' title='"  + faultInfo[i].time + "'> : " + faultInfo[i].shortMessage + "</a></p>";
			//if(i == 0) alert(newContent);
		}
	}
	panel.innerHTML = newContent;
}

// general agents info
function setAgentsInfo(agentInfo, panel)
{
	var newContent = "";

	if (agentInfo.length == 0)
	{
        	newContent = "<p>All agents are online</P>";
  	}
	else
	{
		for(var i = 0; i != agentInfo.length; ++i)
		{
			var agentname = agentInfo[i].agentName;
			var result = parseFloat(agentInfo[i].value);
			if( result == 0)
			{
				newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> : Down : " + agentInfo[i].time +"</p>";
			}
		}
	}
	panel.innerHTML = newContent;
}


//
// QUEUE PAGE
//
function queueMonitorizationInit() 
{
  var params = SAPO.Utility.Url.getQueryString();
  var qnPanel =  s$('queue_name'); 
  var queueName = params.queuename;
  
  var countPanel = s$('queue_msg_count');

  if (queueName == null)
  {
        qnPanel.innerHTML = "<b>Queue name not specified</b>";
	return;
  }
  qnPanel.innerHTML = queueName;

  var f_agents = function() {
   new Ajax.Request('/dataquery/last?predicate=queue-size&subject='+ QUEUE_PREFIX + queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queue_agents');
      var data = transport.responseJSON;      
      setQueueAgentInfo(data,panel,countPanel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  var f_subscriptions = function() {



   new Ajax.Request('/dataquery/last?predicate=subscriptions&subject='+ QUEUE_PREFIX + queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queue_subscriptions');
      var data = transport.responseJSON;      
      setQueueSubscriptionsInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  f_agents();
  setInterval(f_agents, 3000);
  
  f_subscriptions();
  setInterval(f_subscriptions, 3000);
}

var previousAgentQueueInfo = new Object();
var previousCount = undefined;
// queue agent info
function setQueueAgentInfo(agentQueueInfo, panel,countPanel)
{
	var count = 0;
	var newContent = "";
	if (agentQueueInfo.length == 0)
	{
        	newContent = "<p>There are no agents that contain the specified queue</P>";
  	}
	else
	{
		for(var i = 0; i != agentQueueInfo.length; ++i)
		{
			var agentCount = parseFloat(agentQueueInfo[i].value);
			var agentname = agentQueueInfo[i].agentName;

			var previousAgentCount = previousAgentQueueInfo[agentname];
			
			var pic = getLocalPic(previousAgentCount, agentCount);
			
			previousAgentQueueInfo[agentname] = agentCount;
			
			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> :  " + agentCount + " : " + agentQueueInfo[i].time + "<img src='" + pic + "' /></p>";
			count += agentCount;
		}
	}
	var tendencyPic = getLocalPic(previousCount, count);
	previousCount = count;
	
	panel.innerHTML = newContent;
	countPanel.innerHTML = count + "<img src='" + tendencyPic + " />";
}
// Delete queue confirmation
function confirmDelete()
{
	var qnPanel =  s$('queue_name'); 
	var queueName = qnPanel.innerHTML;
	var res = confirm('Are you sure you want to delete queue: ' + queueName);
	if(res)
	{
		window.location = 'deletequeue.html?queuename='+queueName;
	}		
	
	
	return false;
}

// queue subscription info
function setQueueSubscriptionsInfo(subscriptionsQueueInfo, panel)
{
	var newContent = "";

	if (subscriptionsQueueInfo.length == 0)
	{
        	newContent = "<p>There are no subscriptions</P>";
  	}
	else
	{
		for(var i = 0; i != subscriptionsQueueInfo.length; ++i)
		{
			var agentname = subscriptionsQueueInfo[i].agentName;
			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> :  " + parseFloat(subscriptionsQueueInfo[i].value) + " : " + subscriptionsQueueInfo[i].time +"</p>";
		}
	}
	panel.innerHTML = newContent;
}

//
// DELETE QUEUE PAGE
//
function queueDeleteInit()
{
	var params = SAPO.Utility.Url.getQueryString();
	var qnPanel =  s$('queue_name'); 
	var queueName = params.queuename;

	if (queueName == null)
	{
		qnPanel.innerHTML = "<b>Queue name not specified</b>";
		return;
	}
	qnPanel.innerHTML = queueName;
	var msgPanel =  s$('msg_pannel'); 
	msgPanel.innerHTML = "Deleting queue. This may take some time...";
	new Ajax.Request('/action/deletequeue?queuename='+queueName,
	{
	    method:'get',
	    onSuccess: function(transport){
	      var data = transport.responseJSON;
	      var newContent = "";

	      if (data.length == 0)
	      {
	      	newContent = "There is no queue info";
	      }
	      else
	      {	      var fail = false;
		      for(var i = 0; i != data.length; ++i)
		      {
				if(data[i].sucess == "true")
				{
					newContent = newContent + "<a href='./agent.html?agentname="+data[i].agentName+"'>"+ data[i].agentName+ "</a> : OK" +"\n";
				}
				else
				{
					newContent = newContent + "<a href='./agent.html?agentname="+data[i].agentName+"'>"+ data[i].agentName+ "</a> : Failed : " + data[i].reason +"\n";
					fail = true;
				}
		      }
		      if(fail)
		      {
				newContent = newContent + "\n\n Message delete failures caused by connection failure or the existence of active subscribers will be retried later."
		      }
			
	      }
	      
	      msgPanel.innerHTML = newContent;
	    },
	    onFailure: function(){ alert('Something went wrong...') }
	 });
}

//
// FAULT PAGE
//
function faultInformationInit()
{
  var params = SAPO.Utility.Url.getQueryString();
  var params = SAPO.Utility.Url.getQueryString();
  var idPanel = s$('fault_id');
  var faultId = params.faultid;
  if (faultId == null)
  {
        idPanel.innerHTML = "Fault id not specified";
	return;
  }
  idPanel.innerHTML = faultId;
  new Ajax.Request('/dataquery/fault?id='+faultId,
   {
    method:'get',
    onSuccess: function(transport){
      var data = transport.responseJSON; 

      var shortMsgPanel = s$('fault_shortmsg');
      shortMsgPanel.innerHTML = data[0].shortMessage;

      var datePanel = s$('fault_date');
      datePanel.innerHTML = data[0].time;

      var agentPanel = s$('agent_name');
      agentPanel.innerHTML = data[0].agentName;

      var msgPanel = s$('fault_msg');
      msgPanel.innerHTML = data[0].message;
    },
    onFailure: function(){ alert('Something went wrong...') }
   });
}
//
// AGENT PAGE
//
function agentMonitorizationInit() 
{
  var params = SAPO.Utility.Url.getQueryString();
  var idPanel = s$('agent_name');
  var agentname = params.agentname;
  if (agentname == null)
  {
        idPanel.innerHTML = "<b>Agent name not specified</b>";
	return;
  }
  idPanel.innerHTML = agentname;
  // queues
  var f_queues = function() {
   new Ajax.Request('/dataquery/last?predicate=queue-size&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queuesInformationPanel');
      var data = transport.responseJSON;      
      setAgentQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s queue info...') }
   });  
  }
  // faults
  var f_faults = function() {
   new Ajax.Request('/dataquery/fault?count=10&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('faultsInformationPanel');
      var data = transport.responseJSON;      
      setAgentFaultInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s faults info...') }
   });  
  }
  // subscriptions
  var f_subscriptions = function() {
   new Ajax.Request('/dataquery/last?predicate=subscriptions&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('subscriptionInformationPanel');
      var data = transport.responseJSON;      
      setAgentSubscriptionInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s subscriptions info...') }
   });  
  }

  // state
  var f_state = function() {
   new Ajax.Request('/dataquery/last?predicate=status&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('agent_state');
      var data = transport.responseJSON;      
      var content = "<p>Agent status not available.</p>";
      if(data.length != 0)
	content = ( ( parseFloat(data[0].value) == 0) ? "Down" : "Ok") +" : " + data[0].time;
      panel.innerHTML = content;     
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s status info...') }
   });  
  }
  // dropbox
  var f_dropbox = function() {
   new Ajax.Request('/data/dropbox/agent?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('agent_dropbox');
      var data = transport.responseJSON;      
      var content = "Agent dropbox information not available.";
      if(data.length != 0)
	content = data[0].dropboxLocation +" : " + data[0].messages +" : " + data[0].goodMessages;
      panel.innerHTML = content;     
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s dropbox info...') }
   });
  }

  // hostname
  var f_hostname = function() {
   new Ajax.Request('/hostname?name='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('host_name');
      var data = response.evalJSON();
      var content = "";
      content = data.hostname;
      panel.innerHTML = content;     
    },
    onFailure: function(){ var panel = s$('host_name'); panel.innerHTML = "";}
   });  
  }

  f_hostname();

  f_queues();
  setInterval(f_queues, 3000);
  f_subscriptions();
  setInterval(f_subscriptions, 3000);
  f_faults();
  setInterval(f_faults, 3000);
  f_state();
  setInterval(f_state, 3000);
//  f_dropbox();
//  setInterval(f_dropbox, 30000);
}
// agent queue info
function setAgentQueueInfo(queueInfo, panel)
{
	var newContent = "";

	if (queueInfo.length == 0)
	{
        	newContent = "<p>There is no queue info</P>";
  	}
	else
	{
		for(var i = 0; i != queueInfo.length; ++i)
		{
			var queueName = removePrefix(queueInfo[i].subject, QUEUE_PREFIX);
			newContent = newContent + "<p><a href='./queue.html?queuename="+queueName+"'>"+ queueName+ "</a> : " + parseFloat(queueInfo[i].value) +"</p>";
		}
	}
	panel.innerHTML = newContent;
}

// agent subscription info
function setAgentSubscriptionInfo(subscriptionsInfo, panel)
{
	var newContent = "";

	if (subscriptionsInfo.length == 0)
	{
        	newContent = "<p>There are no subscriptions</P>";
  	}
	else
	{
		for(var i = 0; i != subscriptionsInfo.length; ++i)
		{
			var destinationName = subscriptionsInfo[i].subject;
			var isTopic = isPrefix(destinationName, "topic://");
			var imageLoc;
			if(isTopic)
			{
				destinationName = removePrefix(destinationName, "topic://");
				imageLoc = "images/topic.gif";
			} else {
				destinationName = removePrefix(destinationName, "queue://");
				imageLoc = "images/queue.gif";
			}
			newContent =  newContent + "<p><img src=\"images/clock.gif\" title=\"" + subscriptionsInfo[i].time + "\"/><img src=\"" + imageLoc + "\"/>" + destinationName + " : "  + parseFloat( subscriptionsInfo[i].value ) + "</p>";
		}
	}
	panel.innerHTML = newContent;
}

// agent's fault info
function setAgentFaultInfo(faultInfo, panel)
{
	var newContent = "";

	if (faultInfo.length == 0)
	{
        	newContent = "<p>There is no faults info</P>";
  	}
	else
	{
		for(var i = 0; i != faultInfo.length; ++i)
		{
			newContent = newContent + "<p><a href='./fault.html?faultid="+faultInfo[i].id+"'>"  + faultInfo[i].shortMessage + "</a> : " + faultInfo[i].time +"</p>";
		}
	}
	panel.innerHTML = newContent;
}
// go to agent's subscription page

function subscriptionsPage()
{
	return goToAgentPage("/broker/subscriptions");
}

// go to agent's misc information page

function miscInfoPage()
{
	return goToAgentPage("/broker/miscinfo");
}

function goToAgentPage(page)
{
	var anPanel =  s$('agent_name'); 
	var agentName = anPanel.innerHTML;
	var agentIp = agentName.split(":")[0];

	window.location = "http://"+ agentIp + ":3380" + page; 
	
	return false;
}

//
// ALL QUEUES
//
function allQueuesInformationInit()
{
  var infoPanel = s$('queue_list');
  infoPanel.innerHTML = "Information not available";
  var f_allQueues = function(){
	  new Ajax.Request('/dataquery/last?predicate=queue-size',
	   {
	    method:'get',
	    onSuccess: function(transport){
	      var infoPanel = s$('queue_list');
	var response = transport.responseText;
	      var data = transport.responseJSON;
	      setQueueInfo(data, infoPanel);
	    },
	    onFailure: function(){ alert('Something went wrong...') }
	   });
	}
  f_allQueues();
  setInterval(f_allQueues, 30000);
}


//
// UTILS
//

function getLocalPic(oldValue, newValue)
{
	var tendencyPic = "images/trend_flat.gif";
	if( oldValue !== undefined)
	{
		tendencyPic = (oldValue == newValue)? "images/trend_flat.gif" : (newValue > oldValue)? "images/trend_up_bad.gif" : "images/trend_down_good.gif";
	}
	return tendencyPic;
}

function removePrefix(string, prefix)
{
	if(isPrefix(string, prefix))
	{	
		return string.substring(prefix.length);
	}
	return string;
}

function isPrefix(string, prefix)
{
	return string.match("^"+prefix)==prefix;
}

function parseISO8601(str) {
 // we assume str is a UTC date ending in 'Z'

 var parts = str.split('T'),
 dateParts = parts[0].split('-'),
 timeParts = parts[1].split('Z'),
 timeSubParts = timeParts[0].split(':'),
 timeSecParts = timeSubParts[2].split('.'),
 timeHours = Number(timeSubParts[0]),
 _date = new Date;

 _date.setUTCFullYear(Number(dateParts[0]));
 _date.setUTCMonth(Number(dateParts[1])-1);
 _date.setUTCDate(Number(dateParts[2]));
 _date.setUTCHours(Number(timeHours));
 _date.setUTCMinutes(Number(timeSubParts[1]));
 _date.setUTCSeconds(Number(timeSecParts[0]));
 if (timeSecParts[1]) _date.setUTCMilliseconds(Number(timeSecParts[1]));

 // by using setUTC methods the date has already been converted to local time(?)
 return _date;
}
function getHumanTextDiff(date)
{
	var dDif = new Date( new Date() - date);
	var str = "  ";
	if( dDif.getMinutes() != 0 )
	{
		str += " " + dDif.getMinutes() +  " minutes";
	}
	
	if( dDif.getSeconds() != 0 )
	{
		str = (str == "") ? str : str + " and "; 
		str += " " + dDif.getSeconds() + " seconds";
	}
	
	return str;
	//return dDif.toTimeString();
}

