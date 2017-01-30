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

public class VIS_BuildGUIGraphNXDOMAINFile {


	public String buildGUIGraphNXDOMAINFile(Vertica ver, String date,String hourParam, String minutesParam, boolean disconnect, String methodName, CommonMethods commonMethods, Properties graphProps) {
		
		
         String filtering;
    
         File newFile = new File("");
         String timeStampLiteral = commonMethods.getLocalVariable(methodName, "timeStampLiteral");
         String query1 = commonMethods.getQuery(methodName, "query1");
 		 String dbSchema = ver.verticaSchema;
         
 		commonMethods.setParameters("<param1>", commonMethods.tzCast);
 		commonMethods.setParameters("<param2>", date);
		commonMethods.setParameters("<param4>", dbSchema);
 		commonMethods.setParameters("<timeStampBuilderAtSpecificTime>", commonMethods.timeStampBuilderAtSpecificTime(date, hourParam, minutesParam, timeStampLiteral,"automatic"));
 		commonMethods.setParameters("<timeStampBuilderForTimeInterval>", commonMethods.timeStampBuilderForTimeInterval(date, hourParam, minutesParam, timeStampLiteral,"automatic"));
 		query1 = commonMethods.automatedReplaceQueryParameter(query1);
         
 		if(hourParam.length() == 1)
 			hourParam = "0"+hourParam;
 		if(minutesParam.length() == 1)
 			hourParam = "0"+minutesParam;
 			
        String fileName =newFile.getAbsolutePath() + "/webapps/gui/data/nx" + date.replace("/", "") + "-"+ hourParam + minutesParam;

        String fileType = graphProps.getProperty("fileType");
        if (fileType != null){
 			if (fileType.equals("json")){
 				fileName += ".json";
 			}
 		} else {
 			fileName += ".gexf";
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
		    	
		    	
		    	filtering = commonMethods.retrieveIPDNFiltering(ver, date, "srcip", "request", disconnect);
		    	
		    	commonMethods.printMessage("COMPUTATION_FLOW", "Generating graph....");
				commonMethods.printMessage("QUERY", "*** METHOD buildGUIGraphNXDOMAINFile: " + query1+filtering);
				VIS_GephiGraph gb = new VIS_GephiGraph(ver, edgeQuery + filtering, commonMethods, graphProps);
				
				gb.create_graph();
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
				gb.modularity_partition();
				
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
