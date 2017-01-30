/* ---------------------------------------------------------------------- */
/* 
(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
*/
/* ---------------------------------------------------------------------- */

/**
  * @author Yolanta Beresna
  */

var demo = false;

if( getUrlVars()["demo"] != undefined) {

  demo = getUrlVars()["demo"];

  if (demo == 'true') settings.operational = false;

}

var minNodesz =  3;
var maxNodesz = 20;
var minEdgesz =  0.1;
var maxEdgesz = 2;
var defaultColor = "#717171";


sigma.classes.graph.addMethod('outDegreeToSize', function() {
  var degree = this.degree;
  this.nodesArray.forEach(function(node){
  if (node.attributes["type"] == "Client"){
    node.size = degree(node.id);
  }
  else node.size = minNodesz;
  });

});

var graphId='sigma-graph';
var searchId ='graph-search';
var nodeInfoId = 'node-info'; 

var filter;

var needList = false;

var previousNode ="";

var params = getUrlVars();

var date_time_value = get_date_time();

var date = date_time_value.split(' ')[0];
var time = date_time_value.split(' ')[1];

var type = "bl";

var which = "1" //for controlling the graph. Each can be given a number

var clas = "";

var advancedAttributes = true;
var coloredByClass = true; //true if colored by cs1, false if colored by nx/a
var alertFilterHTML = "";
var alertFilterCF;
var typeDim;
var ipDim;
var colorCounter = -1;

var sigInst
(function () {

  'use strict';

  if (params["which"] != undefined) which = params["which"];
  if (params["graphtype"] != undefined) type = params["graphtype"];
  if (params["class"] != undefined) clas = params["class"];
  
  
  /* Instantiate sigma.js and customize rendering */
  sigInst = new sigma({
    renderer: {
      container: document.getElementById(graphId),
      type: 'canvas'
    },
    settings: {
      autoRescale: true,
      mouseEnabled: true,
      touchEnabled: false,
      nodesPowRatio: 1,
      edgesPowRatio: 1,
      defaultEdgeColor: defaultColor, //'#222222',
      defaultNodeColor: defaultColor, //'#333',
      edgeColor: 'default',
      minNodeSize: minNodesz,
      maxNodeSize: maxNodesz,
      minEdgeSize: minEdgesz,
      maxEdgeSize: maxEdgesz,
      defaultLabelSize: 11,
      defaultLabelColor: '#666666',
      zoomMin: 0.03125
    }
  });

}());

draw_graph();

/*
* Get the file and fill the sigInst. Call additional Functions like setup_graph()
*/
function draw_graph() {

  if (params["search"] != undefined) filter = params["search"];

  /* Create Title */

  var title = "Graph View";

  title += ": DNS Queries to ";

  if (type == "bl") title += "Blacklisted Domains, Score > 80 ";

  if (type == "dgaspec" || type == "dgagen" || type=="nx") {

    if (type == "dgagen") title += "Multiple Specific";

    if (type == "dgaspec" && clas =="") clas = "ForbiddenBigrams";

    if (clas == "ForbiddenBigrams" || type=="nx") title += "Unknown ";
    else title += clas;

    title += " Malware Type DGA Domains";
  }


  /* Empty anything from previous graph */

  $('#' + nodeInfoId).popover('destroy');

  sigInst.unbind('overnodes outnodes upnodes');

  sigInst.graph.clear();

  /* Parse file to fill the graph */

  var fileName = './data/'+ type + clas + date.split('-').join('') + '-' + time.split(':').join('') + '.json';

  console.log('File name of the graph is: ' + fileName);

  $.ajax({
    url:fileName,
    type:'HEAD',
    error: function()
    {
        //file not exists
        console.log('The file: ' + fileName + ' does not exist');

        fileName = './data/'+ type + clas + date.split('-').join('') +'.json';
        console.log('Trying file name: ' + fileName);

        $.ajax({
          url:fileName,
          type:'HEAD',
          error: function()
          {
            console.log('The file: ' + fileName +' does not exist');
            fileName = './data/'+ type + clas + date.split('-').join('') + '-' + time.split(':').join('') + '.gexf';
            console.log('Trying gexf now');

            $.ajax({
              url:fileName,
              type:'HEAD',
              error: function(){
                console.log('The file: ' + fileName +' does not exist');
                fileName = './data/'+ clas + type + date.split('-').join('') +'.gexf';
                console.log('Trying file name: ' + fileName);
                $.ajax({
                  url: fileName,
                  type: 'HEAD',
                  error: function(){
                    if (clas != "ForbiddenBigrams") {
                      title = "<br> <br> <br> NOTE: the graph for this date was not generated";
                      document.getElementById('header-graphs').innerHTML = title;
                      $("#graph").hide();
                    }
                    else {
                      console.log('The file: ' + fileName +' does not exist');
                      fileName = './data/'+ "nx" + date.split('-').join('') + '-' + time.split(':').join('')+'.gexf';
                      console.log('Trying file name: ' + fileName);
                      $.ajax({
                        url: fileName,
                        type: 'HEAD',
                        error: function(){
                          title = "<br> <br> <br> NOTE: the graph for this date was not generated";
                          document.getElementById('header-graphs').innerHTML = title;
                          $("#graph").hide();
                        },
                        success: function(data){
                          document.getElementById('header-graphs').innerHTML = title;
                          sigma.parsers.gexf(
                            fileName,
                            sigInst,
                            function(){
                              if (sigInst.graph.nodes().length > 0) {
                                setup_graph();
                                sigInst.refresh();
                              } else {
                                $("#graph").hide();
                                title += "<br> <br> <br> NOTE: No queries of such type for this date";
                                document.getElementById('header-graphs').innerHTML = title;
                              }
                            });
                        }
                      });
                    }
                  },
                  success: function(data){
                    document.getElementById('header-graphs').innerHTML = title;
                    sigma.parsers.gexf(
                      fileName,
                      sigInst,
                      function(){
                        if (sigInst.graph.nodes().length > 0) {
                          setup_graph();
                          sigInst.refresh();
                        } else {
                          $("#graph").hide();
                          title += "<br> <br> <br> NOTE: No queries of such type for this date";
                          document.getElementById('header-graphs').innerHTML = title;
                        }
                    });
                  }
                });
              },
              success: function(data){
                document.getElementById('header-graphs').innerHTML = title;
                sigma.parsers.gexf(
                  fileName,
                  sigInst,
                  function(){
                    if (sigInst.graph.nodes().length > 0) {
                      setup_graph();
                      sigInst.refresh();
                    } else {
                      $("#graph").hide();
                      title += "<br> <br> <br> NOTE: No queries of such type for this date";
                      document.getElementById('header-graphs').innerHTML = title;
                    }
                });
              }
            });
          },
          success: function(data)
          {
            document.getElementById('header-graphs').innerHTML = title;
            sigma.parsers.json(
              fileName,
              sigInst,
              function() {
                if (sigInst.graph.nodes().length > 0) {
                  setup_graph();
                  sigInst.refresh();
                } else {
                  $("#graph").hide();
                  title += "<br> <br> <br> NOTE: No queries of such type for this date";
                  document.getElementById('header-graphs').innerHTML = title;
                }
              }
            );
          }
        });
    },
    success: function(data)
    {
      //file exists
      document.getElementById('header-graphs').innerHTML = title;
      sigma.parsers.json(
        fileName,
        sigInst,
        function() {
          if (sigInst.graph.nodes().length > 0) {
            setup_graph();
            sigInst.refresh();
          } else {
            $("#graph").hide();
            title += "<br> <br> <br> NOTE: No queries of such type for this date";
            document.getElementById('header-graphs').innerHTML = title;
          }
        }
      );
    }

  });

}   

/*
* Called by draw_graph() for additional 
*/
function setup_graph() {

  resizeBody();

  $("#graph").show();

  if (sigInst.graph.nodes()[1].attributes["class"] == undefined) advancedAttributes = false;
  

  /* Draw the graph */

  /* Censor the label if needed and save original color and label. Also extract attributes*/
  sigInst.graph.nodes().forEach(function(n) {
    if (!settings.operational) {
      if(!isNaN(n.label.trim().split('.').join(''))) {
        n.label = n.label.split('.')[0] +"." + n.label.split('.')[1] + ".*.*";
      }
    }
    if (advancedAttributes) {
      if (n.attributes["cat"] == "Client") {
        n.color = defaultColor;
        //n.color = "#717171"           //color nodes grey
      } else {
        if (type == "dgaspec" || type == "dgagen") {
          n.color = get_color_by_dga(n.attributes["class"].trim());
        }
        else if (type == "bl") {
          n.color = get_color_by_score(n.attributes["class"].trim());
        }
      }
    }
    n.attributes.originalColor = n.color;
    n.attributes.originalLabel = n.label;

  });

  //TODO: REMOVE THIS see https://github.com/jacomyal/sigma.js/issues/581
  sigInst.graph.edges().forEach(function(e){
    e.type = 'arrow'
  });

  //graph_showNodeInfo();

  setup_graph_events();

  /* Set up search list if required */

  if (needList) setup_search_list();

  if (!advancedAttributes){
    $("#color-cs1-button").remove();
    $("#color-cat-button").remove();
  }
  setup_filters();

  graph_show_all();

  /* Enable search */

  $('#' + searchId).change(function () {
    find_the_node (document.getElementById(searchId).value);
  });

  if (filter !== undefined) {
    document.getElementById(searchId).value = filter;
    find_the_node(filter);
  }

}

/*
* Define and bind various events for the nodes in the graph
*/
function setup_graph_events() {
  sigInst.unbind('overNode outNode clickNode doubleClickNode rightClickNode');
  sigInst.bind('overNode',function(event){
    var nodes = event.data.node.id;
    var neighbors = {};
    var nrEdges = 0;
    sigInst.graph.edges().forEach(function(e){
      if(nodes.indexOf(e.source)>=0 || nodes.indexOf(e.target)>=0){
      neighbors[e.source] = 1;
      neighbors[e.target] = 1;
      nrEdges++;
      }
    });
    if (nrEdges>0){
      sigInst.graph.nodes().forEach(function(n){
        if(!neighbors[n.id]){
          n.color = "#D8D8D8";
          n.label = "";
        }
      });
    }
    sigInst.refresh();
  });

  sigInst.bind('outNode', graph_color_all);

}

/*
* Return all nodes to default color
*/
function graph_color_all() {
  if (coloredByClass) {
    sigInst.graph.nodes().forEach(function(n){
      n.color = n.attributes.originalColor;
      n.label = n.attributes.originalLabel;
    });
    sigInst.refresh();
    $('#reset-colors-button').remove();
    setup_filters();
  } else {
    sigInst.graph.nodes().forEach(function(n) {
      n.label = n.attributes.originalLabel;
      if (n.attributes["cat"] == "NXDOMAIN") {
        //n.color = "#FF0000";
        n.color = response_colors[0].color;
      } else if (n.attributes["cat"] == "A") {
        //n.color = "#0000FF";
        n.color = response_colors[1].color;
      } else if (n.attributes["cat"] == "Client") {
        n.color = defaultColor;
        //n.color = "#717171";
      }
    });
    sigInst.refresh();
    $('#reset-colors-button').remove();
    setup_filters();
  }
}


/*
* Show all nodes in the graph
*/
function graph_show_all() {

  sigInst.graph.outDegreeToSize();

  sigInst.graph.edges().forEach(function(e){
      e.hidden = 0;
    });
  sigInst.graph.nodes().forEach(function(n){
      n.hidden = 0;
    });
  sigInst.refresh();

  $('#reset-button').remove();
  $('#reset-alert-button').remove();
  setup_filters();

}

/*
* These two functions are for the case that a list of ip addresses is enabled to be searched in the graph
*/
function setup_search_list(){

  var contentIP =""; 

  infectedIPs.forEach(function(item){

    contentIP += "<li><p onClick=\"javascript:set_node_forSearch('"+ item + "');\"style='cursor: pointer'>" + item + "</p></li>"

  });

  document.getElementById('g-list-ips').innerHTML = contentIP;
}

/*
* Called by onClick in setup_search_list
*/
function set_node_forSearch(nodeName) {

  document.getElementById(searchId).value = nodeName;

  find_the_node (nodeName);

  enable_popover (nodeName);

}

/*
* Setup the filter list
*/
function setup_filters() {
  var html = ''
  if (advancedAttributes) {
    //Filters based on already available data
    var possibleFilters = new Set();
    var possibleFiltersCat = new Set();

    
    if (coloredByClass) { // name + color
      sigInst.graph.nodes().forEach(function (n) {
        if (n.attributes["type"] == "Domain") {
          possibleFilters.add(n.attributes["class"] + "@c:" + n.color);
          possibleFiltersCat.add(n.attributes["cat"]);
        };
      });
    } 
    else { // name
      sigInst.graph.nodes().forEach(function (n) {
        if (n.attributes["type"] == "Domain") {
          possibleFilters.add(n.attributes["class"]);
          possibleFiltersCat.add(n.attributes["cat"]);
        };
      });
    }
    
    if (possibleFilters.size > 0){
      var html1 = "<ul>";
      if (coloredByClass) {
        possibleFilters.forEach(function (s) {
          var split = s.split("@c:");
          html1 += "<li id='" + split[0] + "'style='cursor: pointer' onClick='filter_by_class(this.id)'>" + split[0] + "<div class='color-box' style='background-color:" + split[1] + ";'></div>" + "</li>";
        });
      } else {
        possibleFilters.forEach(function (s) {
          html1 += "<li id='" + s + "'style='cursor: pointer' onClick='filter_by_class(this.id)'>" + s + "</li>";
        });
      }
      html1 += "</ul>";
    }
    $("#filters_malware").html(html1);

    if (possibleFiltersCat.size >0){
      var html2 = "<ul>";
      if (coloredByClass){
        possibleFiltersCat.forEach(function (s) {
          html2 += "<li id='" + s + "'style='cursor: pointer' onClick='filter_by_cat(this.id)'>" + s + "</li>"
        });
      } else {
        possibleFiltersCat.forEach(function (s) {
          if (s == "NXDOMAIN") {
            html2 += "<li id='" + s + "'style='cursor: pointer' onClick='filter_by_cat(this.id)'>" + s + "<div class='color-box' style='background-color:" + response_colors[0].color + ";'></div>" + "</li>"
          } else if (s == "A") {
            html2 += "<li id='" + s + "'style='cursor: pointer' onClick='filter_by_cat(this.id)'>" + s + "<div class='color-box' style='background-color:" + response_colors[1].color + ";'></div>" + "</li>"
          }
        });
      }
      html2 += "</ul>";
    }

    $("#filters_class").html(html2);

  }
  
    var html3 = alertFilterHTML;
   
  
}

/*
* Filter domains by cs1
*/
function filter_by_class(cs1) {
  graph_show_all();
  console.log("Filtering class by " + cs1);
  $(document.getElementById(cs1)).html("<b>" + $(document.getElementById(cs1)).html() + "</b>"); // ; caused problems. This is more performant than escaping http://stackoverflow.com/questions/5552462/handling-colon-in-element-id-with-jquery
  var nodes = []
  sigInst.graph.nodes().forEach(function (n) {
    if(n.attributes["class"] == cs1){
      nodes.push(n.id);
    }
  });
  graph_show_nodes(nodes);
  if(! $('#reset-button').length > 0) {
    $("#buttons").append("<button id='reset-button' class='btn btn-default' onClick='graph_show_all()'>Clear classification filter</button>");
  }
}

/*
* Filter nodes by cat
*/
function filter_by_cat (cat) {
  graph_color_all();
  console.log("Filtering by cat " + cat);
  $('#' + cat).html("<b>" + $('#'+cat).html() + "</b>");
  var nodes = []
  sigInst.graph.nodes().forEach(function (n) {
    if(n.attributes["cat"] != cat){
      n.color = "#D8D8D8";
      n.label = "";
    }
  });
  sigInst.refresh();
  if(! $('#reset-colors-button').length > 0) {
    $("#buttons").append("<button id='reset-colors-button' class='btn btn-default' onClick='graph_color_all()'>Clear type filter</button>");
  }
}

/*
* Filter nodes by alert
*/
function filter_by_alerttype (type) {
  graph_show_all();
  console.log("Filtering alert type by " + type);
  $(document.getElementById(type)).html("<b>" + $(document.getElementById(type)).html() + "</b>");
  typeDim.filterAll();
  ipDim.filterAll();
  typeDim.filter(type);
  ips = typeDim.top(alertFilterCF.size());
  var nodes = []
  ips.forEach(function(ip){
    nodes.push(ip.srcip);
  });
  console.log(nodes);
  graph_show_nodes(nodes);

  if(! $('#reset-alert-button').length > 0) {
    $("#buttons").append("<button id='reset-alert-button' class='btn btn-default' onClick='graph_show_all()'>Clear alert filter</button>");
  }
}

/*
* Color domains based on cs1
*/
function color_by_class() {
  coloredByClass = true;
  graph_color_all();
}

/*
* Color domains based on cat
*/
function color_by_cat () {
  coloredByClass = false;
  graph_color_all();
}


/*
* Show only nodes in nodesSelected
*/
function graph_show_nodes(nodesSelected) {

  var neighbors = {};

  sigInst.graph.edges().forEach(function(e){
    if(nodesSelected.indexOf(e.source)>=0 || nodesSelected.indexOf(e.target)>=0){
      neighbors[e.source] = 1;
      neighbors[e.target] = 1;
      }
  });
  sigInst.graph.nodes().forEach(function(n){
    if(!neighbors[n.id] ){
      n.hidden = 1;
    }else{
      n.hidden = 0;
    }
  });
  sigInst.refresh();

}

/*
* Show popover on the side with node info when a node in the graph is clicked
*/    
function graph_showNodeInfo() {

    sigInst.bind('upnodes', function(event) {
      var node;
      sigInst.graph.nodes().forEach(function(n){
        node = n;
      },[event.content[0]]);

      $('#' +nodeInfoId).popover('hide');

      if (node.id.trim() != previousNode) {
        $('#' + nodeInfoId).popover('destroy');
        get_node_info(node.id.trim(), function(nodeContent) {
          $('#' + nodeInfoId).popover({title:'Detailed info', html : true, content: nodeContent, placement: 'bottom', trigger:'manual'} );
          $('#' + nodeInfoId).popover('show');
          previousNode = node.id.trim();
        });
      } else {
        previousNode = "";
      }

    });
    sigInst.refresh();
}

/*
* Get node info via REST call and format the content
*/
function get_node_info(nodeName, fn) {


  var content = "";

  fn(content);
}

/*
* Find a specific node in the graph based on node ID and filter out the graph
*/
function find_the_node(nodeId){

  var nodeForSearch =[];

  var nodeExists = false; 

  var nodePattern = new RegExp(nodeId);

  sigInst.graph.nodes().forEach(function(n){

    if (nodePattern.test(n.id.trim(" "))) {

    //if (n.id.trim(" ") == nodeId) {
      nodeExists = true; 
      nodeForSearch.push(n.id);
    }


  });

  if (nodeExists) {       //if node is in the graph show only that nodes and its connections
    graph_show_nodes(nodeForSearch);
  }
  else graph_show_all(); //if not show all node (this applies also when nodeId = "")

}

/*
* Checks whether a node is in the graph
*/
function node_in_graph (id) {
  var is_in = false;
  sigInst.graph.nodes().forEach(function(node){
    if (node.id.trim() == id) is_in = true;
  });
  return is_in;
}

/*
* Return the color specific for this dga
*/
function get_color_by_dga (dga) {
  for (var i=0; i < dga_colors_specific.length; i++) {
    if(dga_colors_specific[i].name == dga) {
      return dga_colors_specific[i].color;
    }
  }
}

/*
* Return the color specific for this score
*/
function get_color_by_score(score) {
  for (var i=0; i < score_colors_specific.length; i++) {
    if(score_colors_specific[i].name == score) {
      return score_colors_specific[i].color;
    }
  }
  
}