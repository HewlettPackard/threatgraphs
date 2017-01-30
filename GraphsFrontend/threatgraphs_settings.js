/* ---------------------------------------------------------------------- */
/* 
(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
*/
/* ---------------------------------------------------------------------- */

/**
 	* @author Yolanta Beresna
 */

var settings = { 
	operational:true, 
	restURL: "/threatgraphs/services/query/",
	minDate: "2014-10-26",
	maxBackDaysfromToday: 0,
	optionalBackDay: 0,
	hourly: true,
	hourShown: ["00:59", "01:59", "02:59", "03:59", "04:59", "05:59", "06:59", "07:59", "08:59", "09:59", "10:59","11:59","12:59","13:59","14:59","15:59","16:59","17:59","18:59","19:59","20:59","21:59","22:59","23:59"],
	timeZone: "UTC",
	dataOwner: "",
	refreshRate: 60,
};

var hname = document.location.hostname;

var port = "8080";

var score_colors_specific = [
{name:"80", color:"#677da5"},
{name:"90", color: "#70b77e"}, 
{name:"99", color:"#d79c57"}, 
{name:"100", color:"#e0a890"}
];

var dga_colors_specific = [
{name:"ConfickerAB", color: "#2AD2c9"},
{name:"DGA10", color: "#01A982"},
{name:"Caphaw",color: "#84d2f6"},
{name:"Expiro",color: "#91e5f6"},
{name:"Virut",color: "#8C6694"},
{name:"Pykspa",color: "#617C91"},
{name:"Necurs",color: "#94AbA8"},
{name:"Pitou",color: "#998E88"},
{name:"Ramnit", color: "#CCC"},
{name:"Bedep", color: "#187795"},
{name:"ForbiddenBigrams", color: "#F04953"},
{name:"Elephant",color: "#a3b4a2"},
{name:"HyphenDGA",color: "#cdc6ae"}
];

var response_colors = [
{name:"NXDomain", color: "#FFD144"},
{name:"A", color: "#01A982"}
]
