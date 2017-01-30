//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.

package analytics;

import java.util.Properties;

import com.hp.hpl.bd4s.wfm.template.Vertica;
import com.hpl.hp.utils.CommonMethods;

/**
 * @author marco casassa mont, yolanta beresna
 *
 */

public class VIS_BuildHiveplotDGA {

	public String buildHiveplotDGA(Vertica ver, String date, String hourParam,
			String minutesParam, boolean disconnect, String methodName,
			CommonMethods commonMethods, Properties filterProps) {

		String timeStampLiteral = commonMethods.getLocalVariable(methodName,
				"timeStampLiteral");

		String cfp1 = commonMethods.getLocalVariable(methodName, "cfp1");
		String dbSchema = ver.verticaSchema;

		String query1 = commonMethods.getQuery(methodName, "query1");

		commonMethods.setParameters("<param1>", commonMethods.tzCast);
		commonMethods.setParameters("<param2>", date);
		commonMethods.setParameters("<param3>", cfp1);
		commonMethods.setParameters("<param4>", dbSchema);
		commonMethods
				.setParameters("<param5>", commonMethods.retrieveIPDNFiltering(
						ver, date, "dst", "request", disconnect));
		commonMethods.setParameters("<timeStampBuilderAtSpecificTime>",
				commonMethods.timeStampBuilderAtSpecificTime(date, hourParam,
						minutesParam, timeStampLiteral, "automatic"));
		commonMethods.setParameters("<timeStampBuilderForTimeInterval>",
				commonMethods.timeStampBuilderForTimeInterval(date, hourParam,
						minutesParam, timeStampLiteral, "automatic"));
		query1 = commonMethods.automatedReplaceQueryParameter(query1);

		if (hourParam.length() == 1)
			hourParam = "0" + hourParam;
		if (minutesParam.length() == 1)
			hourParam = "0" + minutesParam;

		String edgeQuery = query1;
		
		//determine the minimal out degree.
		// Highest priority first: GET parameter, config file, hardcoded 20
		if (filterProps.get("minOutDegree") == null) {
			try {
				String temp = commonMethods.getLocalVariable(methodName, "defaultMinOutDegree");
				if (temp.equals("byTime")) {
					// Retrieving minimal out degree from config
					int numberOfTimes = commonMethods.retrieveMethodNumberItems(methodName, "outDegreeByTime");
					for(int i = 1 ; i <= numberOfTimes ; i++){
						
						if ((hourParam + ":" + minutesParam).equals(commonMethods.retrieveMethodArrayItem(methodName, "outDegreeByTime", "Time" + i)))
						{
							filterProps.setProperty("minOutDegree", commonMethods.retrieveMethodArrayItem(methodName, "outDegreeByTime", "outDegree" + i));
						}
						
					}
				} else {
					int minOutDegree = Integer.parseInt(temp);
					filterProps.setProperty("minOutDegree", minOutDegree + "");
				}
			} catch (Exception e) {
				filterProps.setProperty("minOutDegree", "20");
			}
		}


		try {
			commonMethods.getTables(ver, methodName);

			commonMethods.printMessage("COMPUTATION_FLOW",
					"Generating hiveplot....");
			commonMethods.printMessage("QUERY", "*** METHOD buildHiveplotDGA: "
					+ query1);
			VIS_Hiveplot hp = new VIS_Hiveplot(ver, edgeQuery, commonMethods, filterProps);
			hp.getData();
			hp.processDGA();
			return hp.toJSON();

		} catch (Exception e) {
			System.err.println("Problem with database.");
			e.printStackTrace();
			return null;
		}

	}
}
