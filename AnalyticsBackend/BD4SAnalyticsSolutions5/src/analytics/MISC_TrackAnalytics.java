//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.


package analytics;

import java.sql.Connection;
import java.sql.Statement;

import com.hp.hpl.bd4s.wfm.template.Vertica;
import com.hpl.hp.utils.CommonMethods;

/**
 * @author marco casassa mont
 *
 */

public class MISC_TrackAnalytics {

	public String updateTrackTimeAnalyticsTable(Vertica ver, String date,String hourParam, String minutesParam, boolean disconnect, String methodName,CommonMethods commonMethods) {
		
		Connection conn;
		String res = null;
		
		try {
			commonMethods.getTables(ver, methodName);
			commonMethods.printMessage("COMPUTATION_FLOW", "updating Track Time Analytics Table");
			
			String query1 = commonMethods.getQuery(methodName, "query1");	
			
			commonMethods.setParameters("<param1>", commonMethods.timestampBuilder(date,hourParam,minutesParam));
			
			
			query1 = commonMethods.automatedReplaceQueryParameter(query1);
			
			commonMethods.printMessage("QUERY", "*** METHOD updateTrackTimeAnalyticsTable: " + query1);
			
			conn = ver.connection();
            Statement stmt = conn.createStatement();  
            
            stmt.executeUpdate(query1);
            
            res= "[{\"result\":\"SUCCESS\"}]";
            
            
		    stmt.close();
			if (disconnect) {ver.disconnect(conn);};
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
	}

}
