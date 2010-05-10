
//
//  MAIN PAGE
//

var QUEUE_PREFIX = "queue://";

var TOPIC_PREFIX = "topic://";

var imagesMetadataMapX = new Object();  // key: image element id, value: imageMetadata created by function processGraphAll

function mainMonitorizationInit() 
{
  // queues
  var f_queues = function() {
   new Ajax.Request('/dataquery/last?predicate=queue-size&minvalue=0&count=10',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queue_size');
      var data = transport.responseJSON;      
      setQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...'); }
   });  
  }
 
  // pending ack
   var f_pendingAck = function() {
   new Ajax.Request('/dataquery/last?subject=system-message&predicate=ack-pending&minvalue=0&count=10',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('pending_ack');
      var data = transport.responseJSON;      
      setSysMsgInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get pending ack info...'); }
   });  
  }
 
  // dropbox
/*
  var f_dropboxes = function() {
   new Ajax.Request('/dataquery/last?predicate=count&subject=dropbox&count=10&minvalue=-1&orderby=object_value',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('dropbox_files');
      var data = transport.responseJSON;      
      setDropboxInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get dropbox info...'); }
   });  
  }
*/
  // errors
  var f_errors = function() {
   new Ajax.Request('/dataquery/faults',
   //new Ajax.Request('dataquery/faults?groupby=shortmessage',
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('errors');
      var data = transport.responseJSON;      
      setErrorInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get faults info...'); }
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
    onFailure: function(){ alert('Something went wrong while trying to get agent status info...'); }
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

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+ queueName+ "'>" + queueName + "</a></td><td>" + queueSize + "</td><td><img src='" + pic + "' /></td>";


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

	panel.innerHTML = newContent;
}
  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=queuecount&window=all", "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=faultrate&window=all", "img_error_rate", "count_error_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=inputrate&window=all", "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=outputrate&window=all", "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }

  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=queuecount&window=last", "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=faultrate&window=last", "img_error_rate", "count_error_rate", "m/s", imagesMetadataMapX);
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
  new Ajax.Request(queryStr,
   {
    method:'get',
    onSuccess: function(transport){

	var imgMetadata = imagesMetadataMap[imgId];
	if( (imgMetadata == undefined) || (imgMetadata == null) )
	{
		return;
	}

	var data = transport.responseJSON;

	if(data.length != 1)
		return;

	var newSample = parseFloat(data[0].value);

	var img = s$(imgId);
	var data = transport.responseJSON;

	
	var circularQueue = imgMetadata.getCircularQueue();
	circularQueue.add(newSample);
	
	var circularQueueSize = circularQueue.size();

	var norm_min = circularQueue.get(0);
	var norm_max = circularQueue.get(0);
	
	for(var i = 1; i < circularQueueSize;++i)
	{
		var curValue = circularQueue.get(i);
		if(curValue < norm_min)
			norm_min = curValue;
		if(curValue > norm_max)
			norm_max = curValue;
	}


	var url="http://chart.apis.google.com/chart?cht=ls&chs=200x90&chd=t:"

	for(var i = 0; i < circularQueueSize; ++i)
	{
		var curValue = circularQueue.get(i);
		var sample = normalizeValue(curValue, norm_min, norm_max);
		url = url + "" + ( (i != 0) ? ("," + sample) : sample );
	}

	url = url + "&chco=336699&chls=3,1,0&chm=o,990000,0," + (circularQueueSize-1) + ",4&chxt=r,x,y&chxs=0,990000,40,0,_|1,990000,1,0,_|2,990000,1,0,_&chxl=0:||1:||2:||&chxp=0,42.3&chf=bg,s,cecece";

	min = round(norm_min,2);
	max = round(norm_max,2);

	var s_unit = "";
	if(typeof(unit)!=undefined && unit!=null)
	{
		s_unit="&nbsp;" + unit;
	}
	

	var legend = s$(legendId);
	legend.innerHTML = "<p><span class='mvalue-latest'>" + round(newSample) + s_unit+ "</span></p><p><span class='mlabel'>Min: </span><span class='mvalue'>" + min + "</span>;<span class='mlabel'> Max: </span><span class='mvalue'>" + max + "</span></p>";


	img.src = url;
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...'); }
   });
}

function processGraphLatestTest(queryStr, imgId, legendId, unit, imagesMetadataMap)
{
  new Ajax.Request(queryStr,
   {
    method:'get',
    onSuccess: function(transport){

	var imgMetadata = imagesMetadataMap[imgId];
	if( (imgMetadata == undefined) || (imgMetadata == null) )
	{
		return;
	}

	var data = transport.responseJSON;

	if(data.length != 1)
		return;

	var img = s$(imgId);
	var data = transport.responseJSON;
	var min = imgMetadata.getMin();
	var max = imgMetadata.getMax();
	var originalMin = min;
	var originalMax = max;

//	console.log(imgId + ' Min: '+min + ' Max: ' + max);
	
	//get sample
	var newSample  = parseFloat(data[0].value);
	//console.log('new sample: ' + newSample);
	//var newSample  = 100;
	if(newSample < min)
	{
		//console.log(imgId + ' replacing min. Was '+min+ ' Now is: ' + newSample);
		min = newSample;
		imgMetadata.setMin(newSample);

	}
	if(newSample > max)
	{
		//console.log( imgId + ' replacing Max. Was '+max+ ' Now is: ' + newSample);

		max = newSample;
		imgMetadata.setMax(newSample);
	}
	
	var circularQueue = imgMetadata.getCircularQueue();
	circularQueue.add(newSample);
	
	var circularQueueSize = circularQueue.size();
/*
	for(var i = 0; i < circularQueueSize; ++i)
	{
		var x = circularQueue.get(i);
		if(x < min)
			alert('MIN alert. Sample: ' + sample + 'Min: ' + min );
		if(x > max)
			alert('MAX alert. Sample: ' + sample + 'Max: ' + max );

	}
	console.log(imgId);
*/

	//console.log('min '+min+ ' max: ' + max);

	var norm_min = circularQueue.get(0);
	var norm_max = circularQueue.get(0);
	
	for(var i = 1; i < data.length;++i)
	{
		var curValue = circularQueue.get(i);
		if(curValue < norm_min) norm_min = curValue;
		if(curValue > norm_max) norm_max = curValue;
	}


	var url="http://chart.apis.google.com/chart?cht=ls&chs=200x90&chd=t:"


//	test_min = circularQueue.get(0);
//	test_max = circularQueue.get(0);

	for(var i = 0; i < circularQueueSize; ++i)
	{
		var curValue = circularQueue.get(i);
		var sample = normalizeValue(curValue, norm_min, norm_max);
		url = url + "" + ( (i != 0) ? ("," + sample) : sample );
/*
		if(curValue < test_min)
		{
			test_min = curValue;
			console.log('Last min: ' + curValue);
		}
		if(curValue > test_max)
		{
			test_max = curValue;
			console.log('Last max: ' + curValue);
		}
*/
	}
/*
	if(test_min != min)
	{
		console.error('MIN error. Min is: ' + test_min + ' Used for normalization: ' + min + ' Original Min: ' + originalMin);
	}
	if(test_max != max)
	{
		console.error('MAX error. Max is: ' + test_max + ' Used for normalization: ' + max + ' Original Max: ' + originalMax);
	}
*/
	url = url + "&chco=336699&chls=3,1,0&chm=o,990000,0," + (circularQueueSize-1) + ",4&chxt=r,x,y&chxs=0,990000,40,0,_|1,990000,1,0,_|2,990000,1,0,_&chxl=0:||1:||2:||&chxp=0,42.3&chf=bg,s,cecece";

	min = round(norm_min,2);
	max = round(norm_max,2);

	var s_unit = "";
	if(typeof(unit)!=undefined && unit!=null)
	{
		s_unit="&nbsp;" + unit;
	}
	

	var legend = s$(legendId);
	legend.innerHTML = "<p><span class='mvalue-latest'>" + round(newSample) + s_unit+ "</span></p><p><span class='mlabel'>Min: </span><span class='mvalue'>" + min + "</span>;<span class='mlabel'> Max: </span><span class='mvalue'>" + max + "</span></p>";


	img.src = url;
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...'); }
   });
}

function processGraphAll(queryStr, imgId, legendId, unit, imagesMetadataMap)
{
  new Ajax.Request(queryStr,
   {
    method:'get',
    onSuccess: function(transport){
	var img = s$(imgId);
	var data = transport.responseJSON;
	var min = 0;
	var max = 0;
	
	if(data.length == 0)
		return;

	// determine max ans min	
	// first sample
	var min = parseFloat(data[0].value);
	var max = parseFloat(data[0].value);
	
	for(var i = 1; i < data.length;++i)
	{
		var curValue = parseFloat(data[i].value);
		if(curValue < min) min = curValue;
		if(curValue > max) max = curValue;
	}

	var url="http://chart.apis.google.com/chart?cht=ls&chs=200x90&chd=t:"


	var imgMetadata = new ImageMetadata();
	imgMetadata.init(min, max);
	
	
	var circularQueue = new CircularQueue();
	circularQueue.init(data.length);
	imgMetadata.setCircularQueue(circularQueue); 

	
	for(var i = 0; i < data.length; ++i)
	{
		var parsedValue = parseFloat(data[i].value);
		circularQueue.add(parsedValue);
		
		var sample = normalizeValue(parsedValue, min, max);
			
		url = url + "" + ( (i != 0) ? ("," + sample) : sample );
	}

	url = url + "&chco=336699&chls=3,1,0&chm=o,990000,0," + (data.length-1) + ",4&chxt=r,x,y&chxs=0,990000,40,0,_|1,990000,1,0,_|2,990000,1,0,_&chxl=0:||1:||2:||&chxp=0,42.3&chf=bg,s,cecece";

	min = round(min,2);
	max = round(max,2);
	var latest = round(parseFloat(data[data.length-1].value));

	var s_unit = "";
	if(typeof(unit)!=undefined && unit!=null)
	{
		s_unit="&nbsp;" + unit;
	}
	

	var legend = s$(legendId);
	legend.innerHTML = "<p><span class='mvalue-latest'>" + latest + s_unit+ "</span></p><p><span class='mlabel'>Min: </span><span class='mvalue'>" + min + "</span>;<span class='mlabel'> Max: </span><span class='mvalue'>" + max + "</span></p>";
	
	imagesMetadataMap[imgId] = imgMetadata;

	img.src = url;
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...'); }
   });
}

function processGraph(queryStr, imgId, legendId, unit)
{
  new Ajax.Request(queryStr,
   {
    method:'get',
    onSuccess: function(transport){
	var img = s$(imgId);
	var data = transport.responseJSON;
	var min = 0;
	var max = 0;
	
	if(data.length == 0)
		return;

	// determine max ans min	
	// first sample
	var min = parseFloat(data[0].value);
	var max = parseFloat(data[0].value);
	
	for(var i = 1; i < data.length;++i)
	{
		var curValue = parseFloat(data[i].value);
		if(curValue < min) min = curValue;
		if(curValue > max) max = curValue;
	}

	var url="http://chart.apis.google.com/chart?cht=ls&chs=200x90&chd=t:"
	
	// process remaining samples
	for(var i = 0; i < data.length; ++i)
	{
		var sample = normalizeValue(parseFloat(data[i].value), min, max);
		
		url = url + "" + ( (i != 0) ? ("," + sample) : sample );
	}

	url = url + "&chco=336699&chls=3,1,0&chm=o,990000,0," + (data.length-1) + ",4&chxt=r,x,y&chxs=0,990000,40,0,_|1,990000,1,0,_|2,990000,1,0,_&chxl=0:||1:||2:||&chxp=0,42.3&chf=bg,s,cecece";

	min = round(min);
	max = round(max);
	var latest = round(parseFloat(data[data.length-1].value));

	var s_unit = "";
	if(typeof(unit)!=undefined && unit!=null)
	{
		s_unit="&nbsp;" + unit;
	}
	

	var legend = s$(legendId);
	legend.innerHTML = "<p><span class='mvalue-latest'>" + latest + s_unit+ "</span></p><p><span class='mlabel'>Min: </span><span class='mvalue'>" + min + "</span>;<span class='mlabel'> Max: </span><span class='mvalue'>" + max + "</span></p>";
	
	img.src = url;
    },
    onFailure: function(){ alert('Something went wrong while trying to get queue info...'); }
   });
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
		var content = "";
		var styleIdx = 1;
		for(var queueName in queues)
		{
			var count = queues[queueName].count;
			var previousValue = previousQueueInfo[queueName];
			var pic = getLocalPic(previousValue, count);
			var curDate = queues[queueName].time;
			previousQueueInfo[queueName] = count;
			var rowClass =  ( ((styleIdx++)%2) == 0) ? "evenrow" : "oddrow";
			content = content + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+queueName+"'>"+ queueName+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
		newContent = content;
	}
	panel.innerHTML = newContent;
}

var previousSysMsgInfo = new Object();
// pending sys messages 
function setSysMsgInfo(sysMsgInfo, panel)
{
	var newContent = "";

	if (sysMsgInfo.length == 0)
	{
        	newContent = "<td class=\"oddrow\" colspan=\"3\">No information available.</td>";
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
			
			var count = parseFloat(sysInfo.value);

			var previousValue = previousSysMsgInfo[agentname];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[agentname] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			content = content + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentname+"'>" + agentHostname + "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
		newContent = content;
	}
	panel.innerHTML = newContent;
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
			var count = parseFloat(dropboxInfo[i].value);

			var previousValue = previousDropboxInfo[agentname];
			var pic = getLocalPic(previousValue, count);
			previousDropboxInfo[agentname] = count;

			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./agent.html?agentname="+agentname+"'>"+ agentHostname+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
		}
	}
	panel.innerHTML = newContent;
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
			var count = errorInfo[i].count;
			var previousValue = previousSysMsgInfo[shortMessage];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[shortMessage] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./faulttype.html?type="+shortMessage+"'>"+ shortMessage+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
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
  var queueName = params.queuename;
  var qnPanel =  s$('queue_name'); 
  
  var countPanel = s$('queue_msg_count');

  if (queueName == null)
  {
        qnPanel.innerHTML = "<b>Queue name not specified</b>";
	return;
  }
 qnPanel.innerHTML = queueName;

  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=queuecountrate&window=all&queuename=" + queueName, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=queueinputrate&window=all&queuename=" + queueName, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=queueoutputrate&window=all&queuename=" + queueName, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=queueerrorrate&window=all&queuename=" + queueName, "img_error_rate", "count_error_rate", "m/s", imagesMetadataMapX);
  }

  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=queuecountrate&window=last&queuename=" + queueName, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=queueinputrate&window=last&queuename=" + queueName, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=queueoutputrate&window=last&queuename=" + queueName, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=queueerrorrate&window=last&queuename=" + queueName, "img_error_rate", "count_error_rate", "m/s", imagesMetadataMapX);
  }

  var f_generalInfo = function() {
   new Ajax.Request("/dataquery/queue?queuename=" + queueName,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('general_queue_information');
      var data = transport.responseJSON;      
      setGeneralQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }

  f_rates_all();
  setInterval(f_rates_latest, 5200);

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

			var failedRate = round(parseFloat(queueGeneralInfo[i].failedRate));
			newContent = newContent + "<td style='padding-left:2em'>" + failedRate + "</td>";

			var expiredRate = round(parseFloat(queueGeneralInfo[i].expiredRate));
			newContent = newContent + "<td style='padding-left:2em'>" + expiredRate + "</td>";

			var redeliveredRate = round(parseFloat(queueGeneralInfo[i].redeliveredRate));
			newContent = newContent + "<td style='padding-left:2em'>" + redeliveredRate + "</td>";

			newContent = newContent + "<td>" + parseFloat(queueGeneralInfo[i].subscriptions) + "</td></tr>";
		}
	}

	panel.innerHTML = newContent;
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
  var idPanel = s$('shortmsg');
  var faultId = params.id;
  if (faultId == null)
  {
        idPanel.innerHTML = "Fault id not specified";
	return;
  }
  idPanel.innerHTML = faultId;
  new Ajax.Request('/dataquery/faults?id='+faultId,
   {
    method:'get',
    onSuccess: function(transport){
      var data = transport.responseJSON; 

      var shortMsgPanel = s$('shortmsg');
      shortMsgPanel.innerHTML =  "<a href='./faulttype.html?type=" + data[0].shortMessage + "'>"+ data[0].shortMessage + "</a>"; data[0].shortMessage;

      var datePanel = s$('fault_date');
      datePanel.innerHTML =  getHumanTextDiff(parseISO8601(data[0].time)) + " ago";

      var agentPanel = s$('agent_name');
      agentPanel.innerHTML = "<a href='./agent.html?agentname=" + data[0].agentName + "'>"+ data[0].agentHostname + "</a>";

      var msgPanel = s$('message');
      msgPanel.innerHTML = data[0].message;
    },
    onFailure: function(){ alert('Something went wrong while trying to get fault message...') }
   });
}
//
// ALL AGENT PAGE
//

function allAgentInit()
{
   var panel = s$('agents');
   panel.innerHTML = "<tr><td colspan='10' class='oddrow'>Please wait...</td></tr>";
   new Ajax.Request('/dataquery/agent',
   {
    method:'get',
    onSuccess: function(transport){
	var panel = s$('agents');
	var data = transport.responseJSON;
	setAllAgentGeneralInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get all agent\'s info...') }
   });  
	
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

			newContent = newContent + "<td>" + agentGeneralInfo[i].dropboxCount + "</td></tr>";
		}
	}

	panel.innerHTML = newContent;
}


//
// AGENT PAGE
//
function agentMonitorizationInit() 
{
  var params = SAPO.Utility.Url.getQueryString();
  var idPanel = s$('host_name');
  var agentname = params.agentname;
  if (agentname == null)
  {
        idPanel.innerHTML = "<b>Agent name not specified</b>";
	return;
  }
  idPanel.innerHTML = agentname;
  // queues
  var f_queues = function() {
   new Ajax.Request('/dataquery/queue?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('queue_size');
      var data = transport.responseJSON;      
      setAgentQueueInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s queue info...') }
   });  
  }
  // faults
  var f_faults = function() {
   new Ajax.Request('/dataquery/faults?agentname='+agentname,
   //new Ajax.Request('/dataquery/groupfault?groupby=shortmessage&agent='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('errors');
      var data = transport.responseJSON;      
      setAgentFaultInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s faults info...') }
   });  
  }
  // subscriptions
  var f_subscriptions = function() {
   new Ajax.Request('/dataquery/subscription?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('subscriptions');
      var data = transport.responseJSON;      
      setAgentSubscriptionInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s subscriptions info...') }
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
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s dropbox info...'); }
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
  // misc
  var f_misc = function() {
   new Ajax.Request('/dataquery/agent?agentname='+agentname,
   {
    method:'get',
    onSuccess: function(transport){
      var response = transport.responseText;
      var panel = s$('misc_info');
      var data = response.evalJSON();
      setMiscAgentInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong while trying to get agent\'s misc info...');}
   });  
  }

  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=agentqueuecount&window=all&agentname=" + agentname, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=agentfaultrate&window=all&agentname=" + agentname, "img_error_rate", "count_error_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=agentinputrate&window=all&agentname=" + agentname, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=agentoutputrate&window=all&agentname=" + agentname, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }
  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=agentqueuecount&window=last&agentname=" + agentname, "img_queue_size_rate", "queue_size_rate", undefined, imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=agentfaultrate&window=last&agentname=" + agentname, "img_error_rate", "count_error_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=agentinputrate&window=last&agentname=" + agentname, "img_input_rate", "count_input_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=agentoutputrate&window=last&agentname=" + agentname, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
  }

  f_hostname();

  f_rates_all();
  setInterval(f_rates_latest, 5000);

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
		newContent = newContent + "<tr class=\"" + rowClass +"\"><td>Pending Sys Ack</td><td style='padding-right:2em'>" +  round(parseFloat(miscInfo[0].pendingAckSystemMsg), 0) +"</td></tr>";
	}
	panel.innerHTML = newContent;
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
			var queueCount = parseFloat(queueInfo[i].queueSize);
			
			var previous = previousAgentQueueCount[queueName];
			var pic = getLocalPic(previous, queueCount);
			previousAgentQueueCount[queueName] = queueCount;	

			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			newContent = newContent + "<tr class=\"" + rowClass +"\"><td style='padding-left:2em'><a href='./queue.html?queuename="+queueName+"'>"+ queueName+ "</a></td><td style='padding-right:2em'>" + queueCount +"</td><td style='padding-right:2em'><img src='" + pic + "' /></td></tr>";
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
        	newContent = "<p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != subscriptionsInfo.length; ++i)
		{
			var subscriptionName = subscriptionsInfo[i].subscriptionName;
			var count = parseFloat(subscriptionsInfo[i].subscriptions);
			var isTopic = isPrefix(subscriptionName, TOPIC_PREFIX);
			
			subscriptionName = removePrefix(subscriptionName, isTopic ? TOPIC_PREFIX : QUEUE_PREFIX);
			
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";
			if(isTopic)
			{
//<a href='./topic.html?subscriptionname=" + subscriptionName + "'>" + subscriptionName + "
				
				newContent = newContent + "<tr class=\"" + rowClass +"\"><td style='padding-left:2em'><a href='./topic.html?subscriptionname=" + subscriptionName + "'>" + subscriptionName + "</td><td style='padding-right:2em'>TOPIC</td><td style='padding-right:2em'>" +  count + "</td></tr>";

				//newContent = newContent + "<tr class=\"" + rowClass +"\"><td style='padding-left:2em'>" + subscriptionName + "</td><td style='padding-right:2em'>TOPIC</td><td style='padding-right:2em'>" +  count + "</td></tr>";
			}
			else
			{
				newContent = newContent + "<tr class=\"" + rowClass +"\"><td style='padding-left:2em'><a href='./queue.html?queuename="+subscriptionName+"'>"+ subscriptionName+ "</a></td><td style='padding-right:2em'>QUEUE</td><td style='padding-right:2em'>" + count +"</td></tr>";	
			}
		}
	}
	panel.innerHTML = newContent;
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
			var count = errorInfo[i].count;
			var previousValue = previousSysMsgInfo[shortMessage];
			var pic = getLocalPic(previousValue, count);
			previousSysMsgInfo[shortMessage] = count;
			var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./faulttype.html?type="+shortMessage+"'>"+ shortMessage+ "</a></td><td class=\"countrow\">" +  count + "</td><td><img src='"+ pic + "' /></td></tr>";
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
  var infoPanel = s$('queues_info');
  infoPanel.innerHTML = "<tr><td colspan='9' class='oddrow'>Please wait...</td></tr>";
  var f_allQueues = function(){
	  new Ajax.Request('/dataquery/queue',
	   {
	    method:'get',
	    onSuccess: function(transport){
	      var infoPanel = s$('queues_info');
	      var response = transport.responseText;
	      var data = transport.responseJSON;
	      setAllQueueGeneralInfo(data, infoPanel);
	    },
	    onFailure: function(){ alert('Something went wrong while trying to get all queues general info...') }
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
        	newContent = "<tr><td colspan='9' class='oddrow'>No information available.</td></tr><p>No information available.</P>";
  	}
	else
	{
		for(var i = 0; i != queueGeneralInfo.length; ++i)
		{
			var queueName = removePrefix(queueGeneralInfo[i].queueName, QUEUE_PREFIX);

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

			newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./queue.html?queuename="+ queueName+ "'>" + queueName + "</a></td><td>" + queueSize + "</td><td><img src='" + pic + "' /></td>";


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

	panel.innerHTML = newContent;
}

//
// ALL TOPICS PAGE
//
function allTopicsMonitorizationInit()
{
  var infoPanel = s$('topics');
  infoPanel.innerHTML = "<tr><td colspan='3' class='oddrow'>Please wait...</td></tr>";
  var f_topicInfo = function(){
	  new Ajax.Request('/dataquery/subscription',
	   {
	    method:'get',
	    onSuccess: function(transport){
	      var infoPanel = s$('topics');
	      var response = transport.responseText;
	      var data = transport.responseJSON;
	      setTopicGeneralInfo(data, infoPanel);
	    },
	    onFailure: function(){ alert('Something went wrong while trying to get all subscriptions general info...') }
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
			
				var rowClass =  ( ((i+1)%2) == 0) ? "evenrow" : "oddrow";

				newContent = newContent + "<tr class=\"" + rowClass +"\"><td><a href='./topic.html?subscriptionname=" + subscriptionName + "'>" + subscriptionName + "</a></td><td style='padding-right:2em'>" + outputRate + "</td><td style='padding-right:2em'>" + count +"</td></tr>";
		    	}
		}
	}
	panel.innerHTML = newContent;
}


//
// TOPIC PAGE
//
function topicMonitorizationInit() 
{
  var params = SAPO.Utility.Url.getQueryString();
  var subscriptionname = params.subscriptionname;
  var tnPanel =  s$('topic_name'); 

  if (subscriptionname == null)
  {
        tnPanel.innerHTML = "<b>Queue name not specified</b>";
	return;
  }
 tnPanel.innerHTML = subscriptionname;
 var panel = s$('general_topic_information');
 panel.innerHTML = "<tr><td colspan='5' class='oddrow'>Please wait...</td></tr>";

  var f_rates_all = function() {
	processGraphAll("/dataquery/rate?ratetype=subscriptionoutputrate&window=all&subscriptionname=" + subscriptionname, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
	processGraphAll("/dataquery/rate?ratetype=subscriptiondiscardedrate&window=all&subscriptionname=" + subscriptionname, "img_discarded_rate", "discarded_size_rate", "m/s", imagesMetadataMapX);
  }
  var f_rates_latest = function() {
	processGraphLatest("/dataquery/rate?ratetype=subscriptionoutputrate&window=last&subscriptionname=" + subscriptionname, "img_output_rate", "count_output_rate", "m/s", imagesMetadataMapX);
	processGraphLatest("/dataquery/rate?ratetype=subscriptiondiscardedrate&window=last&subscriptionname=" + subscriptionname, "img_discarded_rate", "discarded_size_rate", "m/s", imagesMetadataMapX);
  }

  var f_generalInfo = function() {
   new Ajax.Request("/dataquery/subscription?subscriptionname=" + subscriptionname,
   {
    method:'get',
    onSuccess: function(transport){
      var panel = s$('general_topic_information');
      var data = transport.responseJSON;      
      setGeneralTopicInfo(data, panel);
    },
    onFailure: function(){ alert('Something went wrong...') }
   });  
  }

  f_rates_all();
  setInterval(f_rates_latest, 5200);
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

	panel.innerHTML = newContent;
}

//
// FAULT TYPE PAGE
//
function faultTypeMonitorizationInit()
{
  var params = SAPO.Utility.Url.getQueryString();
  var faultType = params.type;  
  var ftPanel = s$('fault_type');
  ftPanel.innerHTML = faultType;
  
  var f_faultTypes = function(){
	  new Ajax.Request('dataquery/faults?type='+faultType,
	   {
	    method:'get',
	    onSuccess: function(transport){
	      var infoPanel = s$('agents_messages');
	      var response = transport.responseText;
	      var data = transport.responseJSON;
	      setFaultTypeInfo(data, infoPanel);
	    },
	    onFailure: function(){ alert('Something went wrong while trying to get all subscriptions general info...') }
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
	panel.innerHTML = newContent;
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
function getHumanTextDiff(date)
{
	var now = new Date();
	var dDif = new Date( now - date);

	var tzOffsetHours = (now.getTimezoneOffset() != 0) ? (now.getTimezoneOffset() / 60) : 0;

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
