
//
//  MAIN PAGE
//

var QUEUE_PREFIX = "queue://";

var TOPIC_PREFIX = "topic://";

var imagesMetadataMapX = new Object();  // key: image element id, value: imageMetadata created by function processGraphAll

var FAULT_SHORT_MESSAGA_MAX_SIZE = 40;

function mainMonitorizationInit() 
{

  var statusPannel = jQuery('#status-box');
  statusPannel.hide();

  // queues
  var f_queues = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/snapshot?type=queuecountsnapshot',
    success: function(data){
      var panel = jQuery('#queue_size');
      setQueueInfo(data, panel);
    }
   });  
  }
 
  // sys message failed delivery
   var f_pendingAck = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/snapshot?type=sysmsgfailsnapshot',
    success: function(data){
      var panel = jQuery('#pending_ack');
      setSysMsgInfo(data, panel);
    }
   });
  }

  // dropbox
/*
  var f_dropboxes = function() {
   new Ajax.Request('/dataquery/snapshot?type=dropboxcountsnapshot',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = jQuery('#dropbox_files');
      var data = transport.responseJSON;      
      setDropboxInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get dropbox info...'); }
   });  
  }
*/
  // errors
  var f_errors = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/faults',
    success: function(data){
      var panel = jQuery('#errors');   
      setErrorInfo(data, panel);
    }
   });
  }

  // agents
  var f_agents = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/snapshot?type=agentstatussnapshot',
    success: function(data){
      var pannel = jQuery('#status-box');
      var agentsInfo = jQuery('#alertAgentsDown');
      setAgentsInfo(data, pannel, agentsInfo);
    }
   });
  }

  // rate// queue agent info
function setAllQueueGeneralInfo(queueGeneralInfo,  panel)
{
	var count = 0;
	var newContent = "";
	if (queueGeneralInfo.length == 0)
	{
        	newContent = "<tr><td colspan='9' class='oddrow'>No information available.</td></tr><p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != queueGeneralInfo.length; ++i)
		{
			var queueName = removePrefix(queueGeneralInfo[i].queueName, QUEUE_PREFIX);
			
			var encQueueName = encodeURIComponent(queueName);

			var prevQueueInfo = previousGeneralQueueInfo[queueName];

			if( prevQueueInfo === undefined)
			{	
				prevQueueInfo = new Object();
				previousGeneralQueueInfo[queueName] = prevQueueInfo;
			}
		
			var queueSize = parseFloat(queueGeneralInfo[i].queueSize);
			var pic = getLocalPic(prevQueueInfo.queueSize, queueSize);
			prevQueueInfo.queueSize = queueSize;
			
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+ encQueueName+ "'>" + queueName + "</a></td><td>" + queueSize + "</td><td><img src='" + pic + "' /></td>";


			var inputRate = round(parseFloat(queueGeneralInfo[i].inputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + inputRate + "</td>";
	
			var outputRate = round(parseFloat(queueGeneralInfo[i].outputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + outputRate + "</td>";

			var failedRate = round(parseFloat(queueGeneralInfo[i].failedRate));
			newContent = newContent + "<td style='padding-left:2em'>" + failedRate + "</td>";

			var expiredRate = round(parseFloat(queueGeneralInfo[i].expiredRate));
			newContent = newContent + "<td style='padding-left:2em'>" + expiredRate + "</td>";

			var redeliveredRate = round(parseFloat(queueGeneralInfo[i].redeliveredRate));
			newContent = newContent + "<td style='padding-left:2em'>" + redeliveredRate + "</td>";

			var subscriptions = round(parseFloat(queueGeneralInfo[i].subscriptions));
			newContent = newContent + "<td style='padding-left:2em'>" + subscriptions + "</td></tr>";
		}
	}

	panel.html(newContent);
}
  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=queuecount&window=all", "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=faultrate&window=all", "img_error_rate", "count_error_rate", "e/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=inputrate&window=all", "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=outputrate&window=all", "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }

  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=queuecount&window=last", "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=faultrate&window=last", "img_error_rate", "count_error_rate", "e/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=inputrate&window=last", "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=outputrate&window=last", "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }

  f_queues();
  setInterval(f_queues, 5000);
  f_pendingAck();
  setInterval(f_pendingAck, 5100);
  f_agents();
  setInterval(f_agents, 5300);
  f_errors();
  setInterval(f_errors, 5400);
// f_dropboxes();
// setInterval(f_dropboxes, 5500);
  f_rates_all();
  setInterval(f_rates_latest, 5600);
}

function processGraphLatest(queryStr, imgId, legendId, unit, imagesMetadataMap)
{
   jQuery.ajax(
   {
    type: 'GET',
    url: queryStr,
    success: function(data){
	var imgMetadata = imagesMetadataMap[imgId];
	if( (imgMetadata == undefined) || (imgMetadata == null) )
	{
		return;
	}
	if(data.length != 1)
		return;

	var img = jQuery('#'+imgId);
	var legend = jQuery('#'+legendId);


	var newSample = parseFloat(data[0].value);

	var circularQueue = imgMetadata.getCircularQueue();
	circularQueue.add(newSample);
	
	var circularQueueSize = circularQueue.size();

	var min = circularQueue.get(0);
	var max = circularQueue.get(0);

	var values = new Array(); 
	
	for(var i = 0; i < circularQueueSize;++i)
	{
		var curValue = circularQueue.get(i);
		if(curValue < min)
			min = curValue;
		if(curValue > max)
			max = curValue;

		values[i] = curValue;
	}

	drawGraph(values, img, legend, min, max, values[values.length-1], unit);
    }
   });
}


function processGraphAll(queryStr, imgId, legendId, unit, imagesMetadataMap)
{
  jQuery.ajax(
   {
    type: 'GET',
    url: queryStr,
    success: function(data){
	if(data.length == 0)
		return;

	var img = jQuery('#'+imgId);
	var legend = jQuery('#'+legendId);

	// determine max ans min	
	// first sample
	var min = parseFloat(data[0].value);
	var max = parseFloat(data[0].value);
	
	var values = new Array(); 

	var imgMetadata = new ImageMetadata();
	imgMetadata.init(min, max);	
	
	var circularQueue = new CircularQueue();
	circularQueue.init(data.length);
	imgMetadata.setCircularQueue(circularQueue); 

	for(var i = 0; i < data.length;++i)
	{
		var curValue = parseFloat(data[i].value);
		if(curValue < min) min = curValue;
		if(curValue > max) max = curValue;

		circularQueue.add(curValue);
		
		values[i] = curValue;
	}

	imagesMetadataMap[imgId] = imgMetadata;
	
	drawGraph(values, img, legend, min, max, parseFloat(data[data.length-1].value), unit);
    }
   });
}

function drawGraph(values, graphPlaceHolder, legendPlaceHolder, min, max, latest, unit)
{
	var s_unit = "";
	if(typeof(unit)!=undefined && unit!=null)
	{
		s_unit="&nbsp;" + unit;
	}
	
	latest = round(latest);
	min = round(min,2);
	max = round(max,2);

	
	legendPlaceHolder.html("<p><span class='mvalue-latest'>" + latest + s_unit+ "</span></p><p><span class='mlabel'>Min: </span><span class='mvalue'>" + min + "</span>;<span class='mlabel'> Max: </span><span class='mvalue'>" + max + "</span></p>");

	graphPlaceHolder.sparkline(values, {width:'200px', height:'90px', lineColor:'#336699', fillColor:'', lineWidth:'3', spotColor:'#990000', minSpotColor:'#990000', maxSpotColor:'#990000'});
	
}

function normalizeValue(curValue, minValue, maxValue)
{
	var dif = maxValue-minValue;
	var bottom = curValue - minValue;
	var curValue = (bottom != 0) ? ((bottom / dif) * 100) : bottom;
	return round(curValue);	
}

var previousQueueInfo = new Object();

// general queue info
function setQueueInfo(queueInfo, panel)
{
	var newContent = "";

	if (queueInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		var content = "";
		for(var i = 0; i != queueInfo.length; ++i)
		{
			var queueName = removePrefix(queueInfo[i].queueName, QUEUE_PREFIX);
			var encQueueName = encodeURIComponent(queueName);
			var strCount = queueInfo[i].count;
			var count = parseFloat(strCount);
	
			var previousValue = previousQueueInfo[queueName];
			var pic = getLocalPic(previousValue, count);
			previousQueueInfo[queueName] = count;
	
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			content = content + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+encQueueName+"'>"+ queueName+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";

		}

		newContent = content;
	}
	panel.html(newContent);
}

var previousSysMsgInfo = new Object();
// pending sys messages 
function setSysMsgInfo(sysMsgInfo, panel)
{
	var newContent = "";

	if (sysMsgInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No failures.</td>";
  	}
	else
	{
		var content = "";
		var count = 0;
		for(var i = 0; i != sysMsgInfo.length; ++i)
		{
			var sysInfo = sysMsgInfo[i];
			var agentname = sysInfo.agentName;
			var agentHostname = sysInfo.agentHostname;
			
			var count = parseFloat(sysInfo.count);

			var previousValue = previousSysMsgInfo[agentname];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[agentname] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			content = content + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentname+"'>" + agentHostname + "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
		newContent = content;
	}
	panel.html(newContent);
}

var previousDropboxInfo = new Object();

// general dropbox info
function setDropboxInfo(dropboxInfo, panel)
{
	var newContent = "";

	if (dropboxInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		for(var i = 0; i != dropboxInfo.length; ++i)
		{
			var agentname = dropboxInfo[i].agentName;
			var agentHostname = dropboxInfo[i].agentHostname;
			var count = parseFloat(dropboxInfo[i].count);

			var previousValue = previousDropboxInfo[agentname];
			var pic = getLocalPic(previousValue, count);
			previousDropboxInfo[agentname] = count;

			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentname+"'>"+ agentHostname+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
	}
	panel.html(newContent);
}

var previousFaultInfo = new Object();
// general fault info
function setErrorInfo(errorInfo, panel)
{
	var newContent = "";

	if (errorInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
  	}
	else
	{
		for(var i = 0; i != errorInfo.length; ++i)
		{
			var shortMessage = errorInfo[i].shortMessage;
			var encShortMessage = encodeURIComponent(shortMessage);
			var count = errorInfo[i].count;
			var previousValue = previousSysMsgInfo[shortMessage];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[shortMessage] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./faulttype.html?type="+encShortMessage+"'>"+ shortMessage+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
	}
	panel.html(newContent);
}

// general agents info
function setAgentsInfo(agentInfo, pannel, messageLabel)
{
	var MAX_QUEUES_ALARM = 300;

	var countDown = 0;
	var alarmQueues = 0;
	for(var i = 0; i != agentInfo.length; ++i)
	{
		if(agentInfo[i].status == "Down")
			++countDown;
			
		if(agentInfo[i].queueCount >= MAX_QUEUES_ALARM)
			++alarmQueues;
	}

	if( (countDown == 0) && (alarmQueues == 0) ) 
	{
		pannel.hide();
	}
	else
	{
		var msg = "";
		if(countDown != 0)
			msg += countDown + ( ((countDown == 1) ? " agent" : " agents") + " down!\n");
		if(alarmQueues != 0)
			msg += alarmQueues + ( ((alarmQueues == 1) ? " agent" : " agents") + " with more than " + (MAX_QUEUES_ALARM-1) + " queues!" );
			
		messageLabel.text(msg);
		pannel.show();
	}
}

//
// QUEUE PAGE
//
function queueMonitorizationInit() 
{
  var queueName = jQuery.query.get('queuename');
  var qnPanel =  jQuery('#queue_name'); 
  
  var encQueueName = encodeURIComponent(queueName);
  
  var countPanel = jQuery('#queue_msg_count');

  if (queueName == null)
  {
        qnPanel.html("<b>Queue name not specified</b>");
	return;
  }
 qnPanel.text(queueName);

  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=queuecountrate&window=all&queuename=" + encQueueName, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=queueinputrate&window=all&queuename=" + encQueueName, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=queueoutputrate&window=all&queuename=" + encQueueName, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }

  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=queueinputrate&window=last&queuename=" + encQueueName, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=queueoutputrate&window=last&queuename=" + encQueueName, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }
  var f_queues_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=queuecountrate&window=last&queuename=" + encQueueName, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
  }

  var f_generalInfo = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/queue?queuename=' + queueName,
    success: function(data){
      var panel = jQuery('#general_queue_information');
  
      setGeneralQueueInfo(data, panel);
    }
   });
  }

  f_rates_all();
  setInterval(f_rates_latest, 30000);
  setInterval(f_queues_latest, 5200);

//  f_rates();
//  setInterval(f_rates, 5200);
  f_generalInfo();
  setInterval(f_generalInfo, 5000);
}

var previousGeneralQueueInfo = new Object();
// queue agent info
function setGeneralQueueInfo(queueGeneralInfo,  panel)
{
	var count = 0;
	var newContent = "";
	if (queueGeneralInfo.length == 0)
	{
        	newContent = "<p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != queueGeneralInfo.length; ++i)
		{
			var agentname = queueGeneralInfo[i].agentName;		

			var prevQueueInfo = previousGeneralQueueInfo[agentname];

			if( prevQueueInfo === undefined)
			{	
				prevQueueInfo = new Object();
				previousGeneralQueueInfo[agentname] = prevQueueInfo;
			}
		
			var agentCount = parseFloat(queueGeneralInfo[i].queueSize);
			var pic = getLocalPic(prevQueueInfo.queueSize, agentCount);
			prevQueueInfo.queueSize = agentCount;
			
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+ agentname+ "'>" + queueGeneralInfo[i].agentHostname + "</a></td><td>" + agentCount + "</td><td><img src='" + pic + "' /></td>";

			var inputRate = round(parseFloat(queueGeneralInfo[i].inputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + inputRate + "</td>";
	
			var outputRate = round(parseFloat(queueGeneralInfo[i].outputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + outputRate + "</td>";

			var redeliveredRate = round(parseFloat(queueGeneralInfo[i].redeliveredRate));
			newContent = newContent + "<td style='padding-left:2em'>" + redeliveredRate + "</td>";

			newContent = newContent + "<td>" + parseFloat(queueGeneralInfo[i].subscriptions) + "</td></tr>";
		}
	}

	panel.html(newContent);
}
// Delete queue confirmation
function confirmDelete()
{
	var qnPanel =  jQuery('#queue_name'); 
	var queueName = qnPanel.innerHTML;
	var res = confirm('Are you sure you want to delete queue: ' + queueName);
	if(res)
	{
		window.location = 'deletequeue.html?queuename='+queueName;
	}		
	
	
	return false;
}

//
// DELETE QUEUE PAGE
//
function queueDeleteInit()
{
	var qnPanel =  jQuery('#queue_name');
	var queueName = jQuery.query.get('queuename');

	if (queueName == null)
	{
		qnPanel.html("<b>Queue name not specified</b>");
		return;
	}
	qnPanel.text(queueName);
	var msgPanel =  jQuery('#msg_pannel'); 
	msgPanel.html("Deleting queue. This may take some time...");
	jQuery.ajax(
	{
	    type: 'GET',
	    url: '/action/deletequeue?queuename='+queueName,
	    success: function(data){
	      var newContent = "";

	      if (data.length == 0)
	      {
	      	newContent = "There is no queue info";
	      }
	      else
	      {	      
		      var fail = false;
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
	      msgPanel.html(newContent);
	    }
	});
}

  var f_pendingAck = function() {
   
  }

//
// FAULT PAGE
//
function faultInformationInit()
{
  var idPanel = jQuery('#shortmsg');
  var faultId = jQuery.query.get('id');
  if (faultId == null)
  {
        idPanel.html("Fault id not specified");
	return;
  }
  idPanel.text(faultId);
  
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/faults?id='+faultId,
    success: function(data){
      var shortMsgPanel = jQuery('#shortmsg');
      
      var encShortMessage = encodeURIComponent(data[0].shortMessage);
      
      shortMsgPanel.html("<a href='./faulttype.html?type=" + encShortMessage + "'>"+ data[0].shortMessage + "</a>");

      var datePanel = jQuery('#fault_date');
      datePanel.html(getHumanTextDiff(parseISO8601(data[0].time)) + " ago");

      var agentPanel = jQuery('#agent_name');
      agentPanel.html("<a href='./agent.html?agentname=" + data[0].agentName + "'>"+ data[0].agentHostname + "</a>");

      var msgPanel = jQuery('#message');
      msgPanel.html(data[0].message);
    }
   });
}

/*
  var f_pendingAck = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '',
    success: function(data){

    }
   });
  }
*/

//
// ALL AGENT PAGE
//

function allAgentInit()
{
   var panel = jQuery('#agents');
   panel.html("<tr><td colspan='10' class='oddrow'>Please wait...</td></tr>");
   var f_allAgentInit = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/agent',
    success: function(data){
	var panel = jQuery('#agents');
	setAllAgentGeneralInfo(data, panel);
    }
   });
  }

   f_allAgentInit();
   setInterval(f_allAgentInit, 5000);
}

// queue agent info
function setAllAgentGeneralInfo(agentGeneralInfo,  panel)
{
	var count = 0;
	var newContent = "";
	if (agentGeneralInfo.length == 0)
	{
        	newContent = "<tr><td colspan='10' class='oddrow'>No information available.</td></tr>";
  	}
	else
	{
		for(var i = 0; i != agentGeneralInfo.length; ++i)
		{
			var agentname = agentGeneralInfo[i].agentName;		

			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+ agentname+ "'>" + agentGeneralInfo[i].agentHostname + "</a></td>";

			newContent = newContent + "<td>" + agentGeneralInfo[i].status + "</td>";

			var inputRate = round(parseFloat(agentGeneralInfo[i].inputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + inputRate + "</td><td style='padding-right:2em'></td>";
	
			var outputRate = round(parseFloat(agentGeneralInfo[i].outputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + outputRate + "</td><td style='padding-right:2em'></td>";

			var faultRate = round(parseFloat(agentGeneralInfo[i].faultRate));
			newContent = newContent + "<td style='padding-left:2em'>" + faultRate + "</td><td style='padding-right:2em'></td>";

			newContent = newContent + "<td>" + agentGeneralInfo[i].pendingAckSystemMsg + "</td>";
			
			newContent = newContent + "<td>" + agentGeneralInfo[i].dropboxCount + "</td>";

			var tatalQueueCount = round(parseFloat(agentGeneralInfo[i].queueCount));
			newContent = newContent + "<td>" + tatalQueueCount + "</td></tr>";
		}
	}

	panel.html(newContent);
}


//
// AGENT PAGE
//
function agentMonitorizationInit() 
{
  var idPanel = jQuery('#host_name');
  var agentname = jQuery.query.get('agentname');
  if (agentname == null)
  {
        idPanel.html("<b>Agent name not specified</b>");
	return;
  }
  idPanel.text(agentname);

  // queues
  var f_queues = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/queue?agentname='+agentname,
    success: function(data){
      var panel = jQuery('#queue_size');    
      setAgentQueueInfo(data, panel);
    }
   });
  }

  // faults
  var f_faults = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/faults?agentname='+agentname,
    success: function(data){
      var panel = jQuery('#errors');   
      setAgentFaultInfo(data, panel);
    }
   });
  }
  // subscriptions
  var f_subscriptions = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/subscription?agentname='+agentname,
    success: function(data){
      var panel = jQuery('#subscriptions');
      setAgentSubscriptionInfo(data, panel);
    }
   });
  }
  // dropbox
  var f_dropbox = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/data/dropbox/agent?agentname='+agentname,
    success: function(data){
      var panel = jQuery('#agent_dropbox');
      var content = "Agent dropbox information not available.";
      if(data.length != 0)
	content = data[0].dropboxLocation +" : " + data[0].messages +" : " + data[0].goodMessages;
      panel.html(content);
    }
   });
  }
  // hostname
  var f_hostname = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/hostname?name='+agentname,
    success: function(data){
      var panel = jQuery('#host_name');
      var content = "";
      content = data.hostname;
      panel.html(content);    
    }
   });
  }
  // misc
  var f_misc = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/agent?agentname='+agentname,
    success: function(data){
      var panel = jQuery('#misc_info');
      setMiscAgentInfo(data, panel);
    }
   });
  }

  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=agentqueuecount&window=all&agentname=" + agentname, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=agentfaultrate&window=all&agentname=" + agentname, "img_error_rate", "count_error_rate", "e/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=agentinputrate&window=all&agentname=" + agentname, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=agentoutputrate&window=all&agentname=" + agentname, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }
  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=agentfaultrate&window=last&agentname=" + agentname, "img_error_rate", "count_error_rate", "e/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=agentinputrate&window=last&agentname=" + agentname, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=agentoutputrate&window=last&agentname=" + agentname, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }
  var f_queues_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=agentqueuecount&window=last&agentname=" + agentname, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
  }

  f_hostname();

  f_rates_all();
  setInterval(f_rates_latest, 30000);
  setInterval(f_queues_latest, 10000);

  f_queues();
  setInterval(f_queues, 5100);
  f_subscriptions();
  setInterval(f_subscriptions, 5200);
  f_faults();
  setInterval(f_faults, 5300);
  f_misc();
  setInterval(f_misc, 5500);
}

// agent queue info
function setMiscAgentInfo(miscInfo, panel)
{
	var newContent = "";

	if (miscInfo.length != 1)
	{
        	newContent = "<p>No information available.</P>";
  	}
	else
	{
		var i = 1;

		var rowClass =  ( ((i++)%2) == 0) ? "evenrow" : "oddrow";
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>Status</td><td style='padding-right:2em'>" + miscInfo[0].status +"</td></tr>";

		rowClass =  ( ((i++)%2) == 0) ? "evenrow" : "oddrow";
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>TCP Connections</td><td style='padding-right:2em'>" + round(parseFloat(miscInfo[0].tcpConnections), 0) +"</td></tr>";

		rowClass =  ( ((i++)%2) == 0) ? "evenrow" : "oddrow";
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>Legacy TCP Connections</td><td style='padding-right:2em'>" + round(parseFloat(miscInfo[0].tcpLegacyConnections), 0) +"</td></tr>";

		rowClass =  ( ((i++)%2) == 0) ? "evenrow" : "oddrow";
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>SSL Connections</td><td style='padding-right:2em'>" + round(parseFloat(miscInfo[0].ssl), 0) +"</td></tr>";

		rowClass =  ( ((i++)%2) == 0) ? "evenrow" : "oddrow";
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>Dropbox</td><td style='padding-right:2em'>" + round(parseFloat(miscInfo[0].dropboxCount), 0) +"</td></tr>";

		rowClass =  ( ((i++)%2) == 0) ? "evenrow" : "oddrow";
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>Fault Rate</td><td style='padding-right:2em'>" + round(parseFloat(miscInfo[0].faultRate), 1) +"</td></tr>";

		rowClass =  ( ((i++)%2) == 0) ? "evenrow" : "oddrow";
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>Failed Sys Msg</td><td style='padding-right:2em'>" +  round(parseFloat(miscInfo[0].pendingAckSystemMsg), 0) +"</td></tr>";
	}
	panel.html(newContent);
}


// agent queue info
var previousAgentQueueCount = new Object();
function setAgentQueueInfo(queueInfo, panel)
{
	var newContent = "";

	if (queueInfo.length == 0)
	{
        	newContent = "<p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != queueInfo.length; ++i)
		{
			var queueName = removePrefix(queueInfo[i].queueName, QUEUE_PREFIX);
			var encQueueName = encodeURIComponent(queueName);
			
			var queueCount = parseFloat(queueInfo[i].queueSize);
			
			var previous = previousAgentQueueCount[queueName];
			var pic = getLocalPic(previous, queueCount);
			previousAgentQueueCount[queueName] = queueCount;	

			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			newContent = newContent + "<tr class=\"" + rowClass +"\"><td style='padding-left:2em'><a href='./queue.html?queuename="+encQueueName+"'>"+ queueName+ "</a></td><td style='padding-right:2em'>" + queueCount +"</td><td style='padding-right:2em'><img src='" + pic + "' /></td></tr>";
		}
	}
	panel.html(newContent);
}

// agent subscription info
function setAgentSubscriptionInfo(subscriptionsInfo, panel)
{
	var newContent = "";

	if (subscriptionsInfo.length == 0)
	{
        	newContent = "<p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != subscriptionsInfo.length; ++i)
		{
			var subscriptionName = subscriptionsInfo[i].subscriptionName;
			var encSubscriptionName = encodeURIComponent(subscriptionName);
			var count = parseFloat(subscriptionsInfo[i].subscriptions);
			var isTopic = isPrefix(subscriptionName, TOPIC_PREFIX);
			
			subscriptionName = removePrefix(subscriptionName, isTopic ? TOPIC_PREFIX : QUEUE_PREFIX);
			
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			if(isTopic)
			{
				newContent = newContent + "<tr class=\"" + rowClass +"\"><td style='padding-left:2em'><a href='./topic.html?subscriptionname=" + encSubscriptionName + "'>" + subscriptionName + "</td><td style='padding-right:2em'>TOPIC</td><td style='padding-right:2em'>" +  count + "</td></tr>";
			}
			else
			{
				newContent = newContent + "<tr class=\"" + rowClass +"\"><td style='padding-left:2em'><a href='./queue.html?queuename="+encSubscriptionName+"'>"+ subscriptionName+ "</a></td><td style='padding-right:2em'>QUEUE</td><td style='padding-right:2em'>" + count +"</td></tr>";	
			}
		}
	}
	panel.html(newContent);
}

// agent's fault info
function setAgentFaultInfo(errorInfo, panel)
{
	var newContent = "";
	if (errorInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"1\">No information available.</td>";
  	}
	else
	{
		for(var i = 0; i != errorInfo.length; ++i)
		{
			var shortMessage = errorInfo[i].shortMessage;
			var encShortMessage = encodeURIComponent(shortMessage);
			var limitedShortMessage = shortMessage.substring(0, (shortMessage.length>FAULT_SHORT_MESSAGA_MAX_SIZE) ? FAULT_SHORT_MESSAGA_MAX_SIZE : shortMessage.length);
			var count = errorInfo[i].count;
			var previousValue = previousSysMsgInfo[shortMessage];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[shortMessage] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./faulttype.html?type="+encShortMessage+"'>"+ limitedShortMessage+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
	}
	panel.html(newContent);
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
	var agentname = jQuery.query.get('agentname');
	var agentIp = agentname.split(":")[0];

	window.location = "http://"+ agentIp + ":3380" + page; 
	
	return false;
}


//
// INACTIVE QUEUES
//
function inactiveQueuesInformationInit()
{
  var infoPanel = jQuery('#queues_info');
  infoPanel.html("<tr><td colspan='1' class='oddrow'>Please wait...</td></tr>");
  var f_inactiveQueues = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/inactivequeue',
    success: function(data){
      var infoPanel = jQuery('#queues_info');
      setInactiveQueuesInfo(data, infoPanel);
    }
   });
  }
  f_inactiveQueues();
  setInterval(f_inactiveQueues, 5000);
}

function setInactiveQueuesInfo(inactiveQueuesInfo,  panel)
{
	var count = 0;
	var newContent = "";
	if (inactiveQueuesInfo.length == 0)
	{
        	newContent = "<tr><td colspan='1' class='oddrow'>No inactive queues.</td></tr><p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != inactiveQueuesInfo.length; ++i)
		{
			var queueName = removePrefix(inactiveQueuesInfo[i].queueName, QUEUE_PREFIX);
			var encQueueName = encodeURIComponent(queueName);
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+ encQueueName+ "'>" + queueName + "</a></td></tr>";
		}
	}

	panel.html(newContent);
}

//
// ALL QUEUES
//
function allQueuesInformationInit()
{
  var infoPanel = jQuery('#queues_info');
  infoPanel.html("<tr><td colspan='9' class='oddrow'>Please wait...</td></tr>");
  var f_allQueues = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/queue',
    success: function(data){
      var infoPanel = jQuery('#queues_info');
      setAllQueueGeneralInfo(data, infoPanel);
    }
   });
  }
  f_allQueues();
  setInterval(f_allQueues, 5000);
}

var previousAllQueuesGeneralInfo = new Object();
// queue agent info
function setAllQueueGeneralInfo(queueGeneralInfo,  panel)
{
	var count = 0;
	var newContent = "";
	if (queueGeneralInfo.length == 0)
	{
        	newContent = "<tr><td colspan='8' class='oddrow'>No information available.</td></tr><p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != queueGeneralInfo.length; ++i)
		{
			var queueName = removePrefix(queueGeneralInfo[i].queueName, QUEUE_PREFIX);
			var encQueueName = encodeURIComponent(queueName);

			var prevQueueInfo = previousGeneralQueueInfo[queueName];

			if( prevQueueInfo === undefined)
			{	
				prevQueueInfo = new Object();
				previousGeneralQueueInfo[queueName] = prevQueueInfo;
			}
		
			var queueSize = parseFloat(queueGeneralInfo[i].queueSize);
			var pic = getLocalPic(prevQueueInfo.queueSize, queueSize);
			prevQueueInfo.queueSize = queueSize;
			
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+ encQueueName+ "'>" + queueName + "</a></td><td>" + queueSize + "</td><td><img src='" + pic + "' /></td>";


			var inputRate = round(parseFloat(queueGeneralInfo[i].inputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + inputRate + "</td>";
	
			var outputRate = round(parseFloat(queueGeneralInfo[i].outputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + outputRate + "</td>";

			var expiredRate = round(parseFloat(queueGeneralInfo[i].expiredRate));
			newContent = newContent + "<td style='padding-left:2em'>" + expiredRate + "</td>";

			var redeliveredRate = round(parseFloat(queueGeneralInfo[i].redeliveredRate));
			newContent = newContent + "<td style='padding-left:2em'>" + redeliveredRate + "</td>";

			var subscriptions = round(parseFloat(queueGeneralInfo[i].subscriptions));
			newContent = newContent + "<td style='padding-left:2em'>" + subscriptions + "</td></tr>";
		}
	}

	panel.html(newContent);
}

//
// ALL TOPICS PAGE
//
function allTopicsMonitorizationInit()
{
  var infoPanel = jQuery('#topics');
  infoPanel.html("<tr><td colspan='3' class='oddrow'>Please wait...</td></tr>");
  var f_topicInfo = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/subscription',
    success: function(data){
      var infoPanel = jQuery('#topics');
      setTopicGeneralInfo(data, infoPanel);
    }
   });
  }
  f_topicInfo();
  setInterval(f_topicInfo, 5000);
}

function setTopicGeneralInfo(topicsInfo, panel)
{
	var newContent = "";

	if (topicsInfo.length == 0)
	{
        	newContent = "<tr><td colspan='9' class='oddrow'>No information available.</td></tr>";
  	}
	else
	{
		for(var i = 0; i != topicsInfo.length; ++i)
		{
			var subscriptionName = topicsInfo[i].subscriptionName;
			var count = parseFloat(topicsInfo[i].subscriptions);
			var outputRate = round(parseFloat(topicsInfo[i].outputRate));
			var isTopic = isPrefix(subscriptionName, TOPIC_PREFIX);

			if(isTopic){	
				subscriptionName = removePrefix(subscriptionName, isTopic ? TOPIC_PREFIX : QUEUE_PREFIX);
				var encSubscriptionName = encodeURIComponent(subscriptionName);
			
				var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

				newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./topic.html?subscriptionname=" + encSubscriptionName + "'>" + subscriptionName + "</a></td><td style='padding-right:2em'>" + outputRate + "</td><td style='padding-right:2em'>" + count +"</td></tr>";
		    	}
		}
	}
	panel.html(newContent);
}


//
// TOPIC PAGE
//
function topicMonitorizationInit() 
{
  var subscriptionname = jQuery.query.get('subscriptionname');
  var tnPanel =  jQuery('#topic_name'); 
  var encSubscriptionName = encodeURIComponent(subscriptionname);

  if (subscriptionname == null)
  {
        tnPanel.html("<b>Queue name not specified</b>");
	return;
  }
 tnPanel.text(subscriptionname);
 var panel = jQuery('#general_topic_information');
 panel.html("<tr><td colspan='5' class='oddrow'>Please wait...</td></tr>");

  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=subscriptionoutputrate&window=all&subscriptionname=" + encSubscriptionName, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=subscriptiondiscardedrate&window=all&subscriptionname=" + encSubscriptionName, "img_discarded_rate", "discarded_size_rate", "m/s", imagesMetadataMapX);
  }
  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=subscriptionoutputrate&window=last&subscriptionname=" + encSubscriptionName, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=subscriptiondiscardedrate&window=last&subscriptionname=" + encSubscriptionName, "img_discarded_rate", "discarded_size_rate", "m/s", imagesMetadataMapX);
  }

  var f_generalInfo = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/subscription?subscriptionname=' + encSubscriptionName,
    success: function(data){
      var panel = jQuery('#general_topic_information');   
      setGeneralTopicInfo(data, panel);
    }
   }); 
  }
  f_rates_all();
  setInterval(f_rates_latest, 30000);
  f_generalInfo();
  setInterval(f_generalInfo, 5000);
}

function setGeneralTopicInfo(topicGeneralInfo,  panel)
{
	var count = 0;
	var newContent = "";
	if (topicGeneralInfo.length == 0)
	{
        	newContent = "<tr><td colspan='5' class='oddrow'>No information available.</td></tr>";
  	}
	else
	{
		for(var i = 0; i != topicGeneralInfo.length; ++i)
		{
			var agentname = topicGeneralInfo[i].agentName;		

			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+ agentname+ "'>" + topicGeneralInfo[i].agentHostname + "</a></td>";
	
			var outputRate = round(parseFloat(topicGeneralInfo[i].outputRate));
			newContent = newContent + "<td style='padding-left:2em'>" + outputRate + "</td>";

			var discardedRate = round(parseFloat(topicGeneralInfo[i].discardedRate));
			newContent = newContent + "<td style='padding-left:2em'>" + discardedRate + "</td>";

			newContent = newContent + "<td>" + parseFloat(topicGeneralInfo[i].subscriptions) + "</td></tr>";
		}
	}

	panel.html(newContent);
}

//
// FAULT TYPE PAGE
//
function faultTypeMonitorizationInit()
{
  var faultType = jQuery.query.get('type');
  var ftPanel = jQuery('#fault_type');
  ftPanel.text(faultType);
  
  var f_faultTypes = function() {
   jQuery.ajax(
   {
    type: 'GET',
    url: '/dataquery/faults?type='+faultType,
    success: function(data){
      var infoPanel = jQuery('#agents_messages');
      setFaultTypeInfo(data, infoPanel);
    }
   });
  }
  f_faultTypes();
  setInterval(f_faultTypes, 5000);
}

function setFaultTypeInfo(faultInfo, panel)
{
	var newContent = "";

	if (faultInfo.length == 0)
	{
        	newContent = "<p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != faultInfo.length; ++i)
		{
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+faultInfo[i].agentName+"'>"+ faultInfo[i].agentHostname + "</a></td><td style='padding-right:2em'>" + getHumanTextDiff( parseISO8601(faultInfo[i].time) ) +" ago. </td><td style='padding-right:2em'><a href='./fault.html?id=" + faultInfo[i].id +  "'>detail...</a></td></tr>";			
		}
	}
	panel.html(newContent);
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

function round(value, precision)
{
	if(value == 0) return 0;
	
	var mult = 1;
	
	if(typeof(precision)!=undefined && precision!=null)
	{
		mult = precision;
	}

	if(precision == 0)
	{
		Math.round(value);
	}
	
	var factor = 10;	
	for(var i = 1;  mult > i; ++i)
	{
		factor = factor * 10;
	}
	
	
	return Math.round(value * factor) / factor;
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

function getHumanTextDiff2(date)
{
	var res = jQuery.timeFormat(date.getTime());

	return res;
}

function getHumanTextDiff(date)
{
	var nowMillis = new Date().getTime();
	var dateMillis = date.getTime();

	var str = "";

	var tDif = nowMillis - dateMillis;

	var tDifMillis = tDif % 1000;

	var tDifSec = Math.floor(tDif / (1000)) % 60;

	var tDifMin = Math.floor(tDif / (1000 * 60)) % (60 * 60);

	var tDifHours = Math.floor(tDif / (1000 * 60 * 60 )) % (60 * 60 * 60);

	if( (tDifHours % 24) != 0 )
	{
		str += (tDifHours % 24) +  " hours";
	}
	if( tDifMin != 0 )
	{
		str = (str == "") ? str : str + " and "; 
		str += " " + tDifMin +  " minutes";
	}
	
	if( tDifSec != 0 )
	{
		str = (str == "") ? str : str + " and "; 
		str += " " + tDifSec + " seconds";
	}

	if( str === "" && tDifMillis != 0 )
	{
		//str = (str == "") ? str : str + " and "; 
		str += tDifMillis + " milliseconds";
	}

	if(str === "")
	{
		return date.toString();
	}
	
	return str;
}
/*
function getHumanTextDiff(date)
{
	var now = new Date();
	var dDif = new Date( now - date);

	var tzOffsetHours = 0; //(now.getTimezoneOffset() != 0) ? (now.getTimezoneOffset() / 60) : 0;

	var str = "";

	if( ((dDif.getHours() + tzOffsetHours) % 24) != 0 )
	{
		str += ((dDif.getHours() + tzOffsetHours) % 24) +  " hours";
	}
	if( dDif.getMinutes() != 0 )
	{
		str = (str == "") ? str : str + " and "; 
		str += " " + dDif.getMinutes() +  " minutes";
	}
	
	if( dDif.getSeconds() != 0 )
	{
		str = (str == "") ? str : str + " and "; 
		str += " " + dDif.getSeconds() + " seconds";
	}

	if( str === "" && dDif.getMilliseconds() != 0 )
	{
		//str = (str == "") ? str : str + " and "; 
		str += dDif.getMilliseconds() + " milliseconds";
	}

	if(str === "")
	{
		return date.toString();
	}
	
	return str;
}
*/

/*
	Circular queue
*/


function test_circular_queue()
{
	var cq = new CircularQueue(); 
	cq.init(5);
	
	var counter = 0;
	cq.add(counter++);
	console.log('showing after: ' + counter);
	showAllElements(cq);
	cq.add(counter++);
	
	console.log('showing after: ' + counter);
	showAllElements(cq);

	cq.add(counter++);
	cq.add(counter++);
	cq.add(counter++);

	console.log('showing after: ' + counter);
	showAllElements(cq);

	cq.add(counter++);
	cq.add(counter++);
	cq.add(counter++);
	cq.add(counter++);

	console.log('showing after: ' + counter);
	showAllElements(cq);

	cq.add(counter++);
	cq.add(counter++);
	cq.add(counter++);
	cq.add(counter++);

	console.log('showing after: ' + counter);
	showAllElements(cq);

//	var count = cq.size();

//	var value = cq.get(2);
}

function showAllElements(circular_queue)
{
	var size = circular_queue.size();
	for(var i = 0; i != size; ++i)
	{
		console.log(circular_queue.get(i));
	}
}

function ImageMetadata()
{
	var _min;
	var _max;
	var _circularQueue;
	
	this.init = function(min, max)
	{
		this._min = min;
		this._max = max;
	}
	
	this.getMin = function()
	{
		return this._min;
	}
	
	this.setMin = function(min)
	{
		this._min = min;
	}

	this.getMax = function()
	{
		return this._max;
	}

	this.setMax = function(max)
	{
		this._max = max;
	}

	this.getCircularQueue = function()
	{
		return this._circularQueue;
	}

	this.setCircularQueue = function(circularQueue)
	{
		this._circularQueue = circularQueue;
	}
}

function CircularQueue()
{
	var _buffer;
	var _next;
	var _size;
	var _elementCount;
	this.init = function(size)
	{
		this._buffer = new Object();
		this._next = 0;
		this._size = size;
		this._elementCount = 0;
	}
	this.add = function(value)
	{
		this._buffer[this._next] =  value;
		if(this._elementCount < this._size)
		{
			++this._elementCount;
		}
		this._next = (this._next +1 ) % this._size;
	}
	this.size = function()
	{
		return this._elementCount;
	}
	this.get = function(index)
	{
		return  this._buffer[ ( ((this._count < this._size) ? 0 : this._next ) + index ) % this._elementCount];
	}
}
/*
	Circular queue end
*/
