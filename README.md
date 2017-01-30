ThreatGraphs

Generation and visualization of threat graphs from the DNS-type record data that is stored in the SQL database.

Extensions to Gephi toolkit code (https://github.com/gephi/gephi-toolkit) to generate the graphs from the annotated DNS data stored in the Vertica database.

Extensions to the Javascript front-end based on https://github.com/jacomyal/sigma.js/ to provide extra user interactions for displaying threat data available as node metadata in the graph.

Create the threat graphs based on the network activity data, stored in databases such as Vertica, and
then visualize and present them in the web application. 

The creation of the initial graph is using Gephi toolkit (https://github.com/gephi/gephi-toolkit) with some extensions for Vertica and 
colouring of nodes based on threat information. For the visuals in the web front end sigma.js (https://github.com/jacomyal/sigma.js ) Javascript library is being used, which was extended to display extra threat and DNS data information.

(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
