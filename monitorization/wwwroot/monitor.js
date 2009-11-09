
//
//  MAIN PAGE
//

function mainMonitorizationInit() 
{
  // queues
  var f_queues = function() {
   new Ajax.Request('/data/queues',
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queuesInformationPanel');
      var data = response.evalJSON();
      setQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  // dropbox
  var f_dropboxes = function() {
   new Ajax.Request('/data/dropbox',
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('dropboxInformationPanel');
      var data = response.evalJSON();
      setDropboxInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  // faults
  var f_faults = function() {
   new Ajax.Request('/data/faults',
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('faultsInformationPanel');
      var data = response.evalJSON();
      setFaultInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }

  // agents
  var f_agents = function() {
   new Ajax.Request('/data/agents/down',
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('agentsDownInformationPanel');
      var data = response.evalJSON();
      setAgentsInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }

  f_queues();
  setInterval(f_queues, 3000);
  f_dropboxes();
  setInterval(f_dropboxes, 3000);
  f_faults();
  setInterval(f_faults, 3000);
  f_agents();
  setInterval(f_agents, 3000);
}
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
		for(var i = 0; i != queueInfo.length; ++i)
		{
			newContent = newContent + "<p><a href='./queue.html?queuename="+queueInfo[i].name+"'>"+ queueInfo[i].name+ "</a> - " + queueInfo[i].count +"</p>";
		}
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
			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> : <a href='./fault.html?faultid="+faultInfo[i].id+"'>"  + faultInfo[i].shortMessage + "</a> : " + faultInfo[i].date +"</p>";
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
			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> : "  + agentInfo[i].status + " : " + agentInfo[i].date +"</p>";
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
   new Ajax.Request('/data/queues/agents?queuename='+queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queue_agents');
      var data = response.evalJSON();
      setQueueAgentInfo(data,panel,countPanel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  var f_subscriptions = function() {
   new Ajax.Request('/data/queues/subscriptions?queuename='+queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queue_subscriptions');
      var data = response.evalJSON();
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
			var agentCount = agentQueueInfo[i].count;
			var agentname = agentQueueInfo[i].agentName;

			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> :  " + agentCount + " : " + agentQueueInfo[i].date +"</p>";
			count += agentCount;
		}
	}
	panel.innerHTML = newContent;
	countPanel.innerHTML = count;
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
			newContent = newContent + "<p><a href='./agent.html?agentname="+ agentname+ "'>" + agentname + "</a> :  " + subscriptionsQueueInfo[i].subscriptionType + " : " + subscriptionsQueueInfo[i].count + " : " + subscriptionsQueueInfo[i].date +"</p>";
		}
	}
	panel.innerHTML = newContent;
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
        idPanel.innerHTML = "<b>Fault id not specified</b>";
	return;
  }
  idPanel.innerHTML = faultId;
  new Ajax.Request('/data/faults/faultid?id='+faultId,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var data = response.evalJSON();

      var shortMsgPanel = s$('fault_shortmsg');
      shortMsgPanel.innerHTML = data[0].shortMessage;

      var datePanel = s$('fault_date');
      datePanel.innerHTML = data[0].date;

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
   new Ajax.Request('/data/queues/agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queuesInformationPanel');
      var data = response.evalJSON();
      setAgentQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  // faults
  var f_faults = function() {
   new Ajax.Request('data/faults/agent?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('faultsInformationPanel');
      var data = response.evalJSON();
      setAgentFaultInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  // subscriptions
  var f_subscriptions = function() {
   new Ajax.Request('data/subscriptions/agent?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('subscriptionInformationPanel');
      var data = response.evalJSON();
      setAgentSubscriptionInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }

  // state
  var f_state = function() {
   new Ajax.Request('/data/agents/agent?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('agent_state');
      var data = response.evalJSON();
      var content = "<p>Agent status not available.</p>";
      if(data.length != 0)
	content = "<p>"+ data[0].status +" : " + data[0].date +"</p>";
      panel.innerHTML = content;     
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }
  // dropbox
  var f_dropbox = function() {
   new Ajax.Request('/data/dropbox/agent?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('agent_dropbox');
      var data = response.evalJSON();
      var content = "<p>Agent dropbox information not available.</p>";
      if(data.length != 0)
	content = "<p>"+ data[0].dropboxLocation +" : " + data[0].messages +" : " + data[0].goodMessages +"</p>";
      panel.innerHTML = content;     
    },
    onFailure: function(){ alert('Something went wrong...') }
   });
  }

  f_queues();
  setInterval(f_queues, 3000);
  f_subscriptions();
  setInterval(f_subscriptions, 3000);
  f_faults();
  setInterval(f_faults, 3000);
  f_state();
  setInterval(f_state, 30000);
  f_dropbox();
  setInterval(f_dropbox, 30000);
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
			newContent = newContent + "<p><a href='./queue.html?queuename="+queueInfo[i].name+"'>"+ queueInfo[i].name+ "</a> : " + queueInfo[i].count +"</p>";
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
			newContent = newContent + "<p>" + subscriptionsInfo[i].subscription + " : "  + subscriptionsInfo[i].subscriptionType + " : " + subscriptionsInfo[i].count + " : " + subscriptionsInfo[i].date +"</p>";
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
			newContent = newContent + "<p><a href='./fault.html?faultid="+faultInfo[i].id+"'>"  + faultInfo[i].shortMessage + "</a> : " + faultInfo[i].date +"</p>";
		}
	}
	panel.innerHTML = newContent;
}

