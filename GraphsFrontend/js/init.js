	/* ---------------------------------------------------------------------- */
	/* 
	(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	*/
	/* ---------------------------------------------------------------------- */  

	/**
 	* @author Yolanta Beresna
 	*/

	var date_value; var time_value;

	var dModel = new dateModel();

	var dashboardShow = true; 

	var alertsShow = false;

	var graphsShow = true; 

	var demo = false;

	var dateMax = new Date(); var showDateTime = new Date(); 

	var date_time_value;

	var packetsRest = "DNSPacketStatistics";
	var periodsRest = "RetrieveComputedTimePeriods";



    /* ---------------------------------------------------------------------- */
	/*	On Page Load
	/* ---------------------------------------------------------------------- */
	
    

		$(document).ready( function() {

			$('#chevron').click(function(e) {
	          e.preventDefault();
	          $('aside').toggle('slow');
	          $('#page-content').toggleClass('extended-panel'); 
	      	});

			//$('#node-data-title').hide();

			resetIframes();


			alertId = getUrlVars()["alertID"];

			if (alertId !== undefined) {
				dashboardShow = false;
				alertsShow = true;
			}
			

			if( getUrlVars()["demo"] != undefined) {

				demo = getUrlVars()["demo"];
				if (demo == 'true') settings.operational = false;

			}


			dateMax.setDate(dateMax.getDate() - settings.maxBackDaysfromToday);

			showDateTime.setDate(showDateTime.getDate() - (settings.maxBackDaysfromToday + settings.optionalBackDay));

			date_value= moment(showDateTime).format('YYYY-MM-DD'); 

			
		set_showTime(settings.hourShown); pick_date(settings.hourShown); set_up_navigation();

	  	window.addEventListener('message', function(e) {
		  var eventName = e.data[0];
		  var data = e.data[1];
		  switch(eventName) {
		    case 'setHeight':
		      //$iframe.height(data);
		      $("body").height(data);
		      //window.parent.postMessage(["setHeight", data], "*");
		      break;
		  }
		}, false);


	}); 
		

	/* end on page load */

	function set_showTime(time_array) {

		var nrHours = time_array.length; 

		showDateTime.setHours(time_array[nrHours-1].split(":")[0]); showDateTime.setMinutes(time_array[nrHours-1].split(":")[1]);

		time_value = moment(showDateTime).format('HH:mm');

		dModel.set({value:date_value, time:time_value});

		date_time_value = date_value + " " + time_value;
					
		$("#dateTitle").html(moment(date_value).format("dddd, MMMM Do YYYY"));
					
		$("#timeTitle").html("00:00-" + time_value + " " + settings.timeZone);

	}

	function set_up_navigation() {

		setup_b_graphs();

		setup_specific_graphs();

		setup_unknownDGA_graphs();

	}

	/* ---------------------------------------------------------------------- */
	/*	initialise date picker
	/* ---------------------------------------------------------------------- */	

	function pick_date (hourShown) {

		Date.parseDate = function( input, format ){
  			return moment(input,format).toDate();
		};
		Date.prototype.dateFormat = function( format ){
		  return moment(this).format(format);
		};

	
		jQuery('#datepicker').datetimepicker({

			timepicker: settings.hourly,
			format: 'YYYY-MM-DD HH:mm',
			minDate: settings.minDate,
	        maxDate: dateMax,
	        formatDate: 'YYYY-MM-DD',
	        formatTime: 'HH:mm',
	        dayOfWeekStart: 1,
	        inline: true,
	        opened: true,
	        allowTimes: hourShown,
    
	        onSelectDate:function(dp,$input) {

    			date_value = moment($input.val()).format('YYYY-MM-DD');

    			
	    			
  			},

  			onSelectTime: function(dp,$input) {


  				time_value = moment($input.val()).format('HH:mm');

  				dModel.set({value:date_value, time:time_value});

  				$("#dateTitle").html(moment(date_value).format("dddd, MMMM Do YYYY"));
  				$("#timeTitle").html("00:00-" + time_value + " " + settings.timeZone);
  			}


		});

		jQuery('#datepicker').datetimepicker({value: moment(showDateTime).format('YYYY-MM-DD HH:mm')});

	}

	

	/* ---------------------------------------------------------------------- */
	/*	set up b-graphs menu tab navigation
	/* ---------------------------------------------------------------------- */

	function setup_b_graphs(){

		$('#main-tabs a[href="#blgraphs"]').tab('show'); 
		$("#blgraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=bl'+'&demo='+ demo);

		$('a[href="#blgraphs"]').on('shown.bs.tab', function (e) {
			$("#blgraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=bl'+'&demo='+ demo);
		});

		dModel.on("change", function() {

			if ($("#blgraphs").hasClass('active')) {

				$("#blgraphs-iframe").attr("src", 'http://' + hname +':8080/gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=bl'+'&demo='+ demo);
			}

		});

	}

	/* ---------------------------------------------------------------------- */
	/*	set up nx-graphs menu tab navigation
	/* ---------------------------------------------------------------------- */

	function setup_specific_graphs(){

		$('a[href="#necursgraphs"]').on('shown.bs.tab', function (e) {
			$("#necursgraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgaspec&class=Necurs'+'&demo='+ demo);
		});

		dModel.on("change", function() {

			if ($("#necursgraphs").hasClass('active')) {

				$("#necursgraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgaspec&class=Necurs'+'&demo='+ demo);
			}

		});

		$('a[href="#confickergraphs"]').on('shown.bs.tab', function (e) {
			$("#confickergraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgaspec&class=ConfickerAB'+'&demo='+ demo);
		});

		dModel.on("change", function() {

			if ($("#confickergraphs").hasClass('active')) {

				$("#confickergraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgaspec&class=ConfickerAB'+'&demo='+ demo);
			}

		});

		$('a[href="#multigraphs"]').on('shown.bs.tab', function (e) {
			$("#multigraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgagen'+'&demo='+ demo);
		});

		dModel.on("change", function() {

			if ($("#multigraphs").hasClass('active')) {

				$("#multigraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgagen'+'&demo='+ demo);
			}

		});

	}

	/* ---------------------------------------------------------------------- */
	/*	set up rd-graphs menu tab navigation
	/* ---------------------------------------------------------------------- */

	function setup_unknownDGA_graphs(){

		$('a[href="#unknowngraphs"]').on('shown.bs.tab', function (e) {
			$("#unknowngraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgaspec&class=ForbiddenBigrams'+'&demo='+ demo);
		});

		dModel.on("change", function() {

			if ($("#unknowngraphs").hasClass('active')) {

				$("#unknowngraphs-iframe").attr("src", 'http://' + hname +':8080/graphs_gui/graphs.html?date=' + date_value + '&time=' + time_value + '&graphtype=dgaspec&class=ForbiddenBigrams'+'&demo='+ demo);
			}

		});

	}

	