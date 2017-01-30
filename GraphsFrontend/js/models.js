    /* ---------------------------------------------------------------------- */
    /* 
    (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    */
    /* ---------------------------------------------------------------------- */  

    /**
    * @author Yolanta Beresna
    */

var dateModel = Backbone.Model.extend({
        defaults: {
            id: 'stime',
            value: new Date(),
            time: '23:49'
        },
        initialize: function(){
        	
        	this.on("change:value", function(model){
                var value = model.get("value");
                var time = model.get("time"); 
            });

        	
        }
  });


var statModel = Backbone.Model.extend({
  });

var statsCollection = Backbone.Collection.extend({

	id:'stats',
	model: this.statModel,

	initialize: function () {
		
	},
});

var esmModel = Backbone.Model.extend({
        defaults: {
            date: 'stat',
            numESMAlerts: 0,
        },
        initialize: function(){
            
        }
  });

var esmCollection = Backbone.Collection.extend({

    id:'esm',
    model: this.esmModel,

    initialize: function () {
        
    },
});

var configModel = Backbone.Model.extend({
    defaults: {
            operational: false
        },
        initialize: function(){
            
        }
  });


var whoIsModel = Backbone.Model.extend({
        defaults: {
            createdDate: 'NONE',
            updatedDate: 'NONE',
            expiresDate: 'NONE',
            estimatedDomainAge: 'NONE',
            registrantName: 'NONE',
            registrantOrg: 'NONE',
            registrantCity: 'NONE',
            registrantState: 'NONE',
            nameServers: 'NONE',
            dataError: 'NONE',
            SCORE: 'NONE'

        },
        initialize: function(){
            
        }
  });

var infectedClientModel = Backbone.Model.extend({
    defaults: {
        ip: '',
        hostname: '',
        alertTypes: [],
    },
});

var infectedClientCollection = Backbone.Collection.extend({

    id:'ip',
    model: this.infectedClientCollection,

    initialize: function () {
        
    },
});


var hostNameModel = Backbone.Model.extend({

	defaults: {
		id:'',
	    ipAddress:'NONE',
	    hostname: 'NONE',
	},

});

var timeseriesModel = Backbone.Model.extend({

    defaults: {
        domainName:'',
        values:[],
    },

});

var timeseriesCollection = Backbone.Collection.extend({

    id:'time',
    model: this.timeseriesModel,

    initialize: function () {
        
    },
});

var alertModel = Backbone.Model.extend({

    defaults: {
        type:"none",
        clients: []
    },

});

var alertsCollection = Backbone.Collection.extend({
    id:'',
    model: this.alertModel,

    initialize: function () {
        
    },
});



