//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.


package analytics;

import java.io.File;
import java.util.Properties;

import com.hp.hpl.bd4s.wfm.template.Vertica;
import com.hpl.hp.utils.CommonMethods;

/**
 * @author marco casassa mont, yolanta beresna
 *
 */


public class VIS_BuildGUIGraphDGASpecificFile {

	public String buildGUIGraphDGASpecificFile(Vertica ver, String date, String hourParam, String minutesParam, boolean disconnect, String methodName, CommonMethods commonMethods, Properties graphProps) {



		File newFile = new File("");
		String timeStampLiteral = commonMethods.getLocalVariable(methodName, "timeStampLiteral");

		String cfp1 = commonMethods.getLocalVariable(methodName, "cfp1");
		String dbSchema = ver.verticaSchema;


		String query1 = commonMethods.getQuery(methodName, "query1");

		commonMethods.setParameters("<param1>", commonMethods.tzCast);
		commonMethods.setParameters("<param2>", date);
		commonMethods.setParameters("<param3>", cfp1);
		commonMethods.setParameters("<param4>", dbSchema);
		commonMethods.setParameters("<param5>", commonMethods.retrieveIPDNFiltering(ver, date, "dst", "request", disconnect));
		commonMethods.setParameters("<timeStampBuilderAtSpecificTime>", commonMethods.timeStampBuilderAtSpecificTime(date, hourParam, minutesParam, timeStampLiteral,"automatic"));
		commonMethods.setParameters("<timeStampBuilderForTimeInterval>", commonMethods.timeStampBuilderForTimeInterval(date, hourParam, minutesParam, timeStampLiteral,"automatic"));
		query1 = commonMethods.automatedReplaceQueryParameter(query1);


		if(hourParam.length() == 1)
 			hourParam = "0"+hourParam;
 		if(minutesParam.length() == 1)
 			hourParam = "0"+minutesParam;

		String fileName =newFile.getAbsolutePath() + "/webapps/gui/data/dgaspec" + date.replace("/", "")  + "-"+ hourParam + minutesParam;

		String fileType = graphProps.getProperty("fileType");
		if (fileType != null){
			if (fileType.equals("json")){
				fileName += ".json";
			}
		} else {
			fileName += ".gexf";
		}
		//determine the minimal out degree.
		// Highest priority first: GET parameter, config file, hardcoded 20
		if (graphProps.get("minOutDegree") == null) {
			try {
				String temp = commonMethods.getLocalVariable(methodName, "defaultMinOutDegree");
				if (temp.equals("byTime")) {
					// Retrieving minimal out degree from config
					int numberOfTimes = commonMethods.retrieveMethodNumberItems(methodName, "outDegreeByTime");
					for(int i = 1 ; i <= numberOfTimes ; i++){
						
						if ((hourParam + ":" + minutesParam).equals(commonMethods.retrieveMethodArrayItem(methodName, "outDegreeByTime", "Time" + i)))
						{
							graphProps.setProperty("minOutDegree", commonMethods.retrieveMethodArrayItem(methodName, "outDegreeByTime", "outDegree" + i));
						}
						
					}
				} else {
					int minOutDegree = Integer.parseInt(temp);
					graphProps.setProperty("minOutDegree", minOutDegree + "");
				}
			} catch (Exception e) {
				graphProps.setProperty("minOutDegree", "20");
			}
		}

		//determine which layoutalgorithm to use
		String layout = graphProps.getProperty("defaultLayout");
		if (layout == null) {
			//none given via GET parameter
			try {
				//take the one from config
				layout = commonMethods.getLocalVariable(methodName, "defaultLayout");
			} catch (Exception e) {
				//last resort: fruchterman
				layout = "fr";
			}
		}

		String edgeQuery = query1;
		if (!new File(fileName).isFile()) {

			try {
				commonMethods.getTables(ver, methodName);


		    	commonMethods.printMessage("COMPUTATION_FLOW", "Generating graph....");
				commonMethods.printMessage("QUERY", "*** METHOD buildGUIGraphDGASpecificFile: " + query1);
				VIS_GephiGraph gb = new VIS_GephiGraph(ver, edgeQuery, commonMethods, graphProps);

				gb.create_graph();
				gb.process_dga();
				switch (layout){
					case "fr": 
						gb.fr_layout();
						break;
					case "fa2":
						gb.fa2layout();
						break;
					case "yh":
						gb.yh_layout();
						break;
					default:
						gb.fr_layout();
						break;
				}
				//gb.modularity_partition();
				gb.create_graph_file(fileName);

			}
			catch (Exception e)
			{
				System.err.println("Problem with database.");
				e.printStackTrace(); return null;
			}
		}

		return fileName;
	}
}
