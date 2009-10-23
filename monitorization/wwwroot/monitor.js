function mainMonitorizationInit() 
{
  var f = function() {
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
  f();
  setInterval(f, 3000);
}

function setQueueInfo(queueInfo, panel)
{
	var newContent = "";

	var child;
	for(var i = 0; i != queueInfo.length; ++i)
	{
		newContent = newContent + "<p><a href='./queue.html?queuename="+queueInfo[i].name+"'>"+ queueInfo[i].name+ "</a> - " + queueInfo[i].count +"</p>";
	}
	panel.innerHTML = newContent;
}

function setQueueAgentInfo(agentQueueInfo, panel)
{
	var newContent = "";

	var child;
	for(var i = 0; i != agentQueueInfo.length; ++i)
	{
		newContent = newContent + "<p>"+ agentQueueInfo[i].agentName+ " : " + agentQueueInfo[i].count + " : " + agentQueueInfo[i].date +"</p>";
	}
	panel.innerHTML = newContent;
}

function setQueueSubscriptionsInfo(subscriptionsQueueInfo, panel)
{
	var newContent = "";

	var child;
	for(var i = 0; i != subscriptionsQueueInfo.length; ++i)
	{
		//newText = newText +"\n" + queueInfo[i].name + " : " + queueInfo[i].count;
		//opcao 1
		newContent = newContent + "<p>"+ subscriptionsQueueInfo[i].agentName+ " : " + subscriptionsQueueInfo[i].subscriptionType + " : " + subscriptionsQueueInfo[i].count + " : " + subscriptionsQueueInfo[i].date +"</p>";
	}
	panel.innerHTML = newContent;
}

function queueMonitorizationInit() 
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

  var f_agents = function() {
   new Ajax.Request('/data/queues/agents?queuename='+queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('queue_agents');
      var data = response.evalJSON();
      setQueueAgentInfo(data,panel);
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

//window.onload = mainMonitorizationInit;
