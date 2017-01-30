//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.

package restAPI;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import analytics.*;

import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.google.common.html.HtmlEscapers;
import com.hp.hpl.bd4s.wfm.template.*;
import com.hpl.hp.utils.CommonMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * REST API Class
 * @author marco casassa mont
 *
 */
@Path("/query")
public class AnalyticsWSAPIV2 extends BasicRequestHandler{

	// UPDATED TO NEW DATA FEEDS
	ClassLoader classLoader = AnalyticsWSAPIV2.class.getClassLoader();
	HashMap<String, String> queryParams = new HashMap<>();




	@Context ServletContext context;
	//@Context SecurityContext sc;
	public static String configPath;

	@Context
    private SecurityContext security;

		
	
	@GET
	@Path("/buildGUIGraphFile")
	@Produces( MediaType.APPLICATION_JSON )
	public String getGUIGraphFile(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam,@QueryParam("save") String fileType, @QueryParam("layout") String layout)
	{		

		String date = dateParam;

		String finalRes ="";
		
		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphFile; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphFile; User: unkown");			
		}
		String methodName = commonMethods.getMethod("buildGUIGraphFile");
		VIS_BuildGUIGraphFile buildGUIGraphFile = new VIS_BuildGUIGraphFile();
		
		Properties graphProps = new Properties();
		if (layout != null) {
			graphProps.setProperty("defaultLayout", layout);
		}
		if (fileType != null){
			graphProps.setProperty("fileType", fileType);
		}

		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);
			
			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{	  
				date = date.replaceAll("-", "/");	          
				finalRes= buildGUIGraphFile.buildGUIGraphFile(db, date, hourParam, minutesParam, true, methodName, commonMethods, graphProps);
				//db.disconnect();
			}  

			return finalRes;
		} 
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /buildGUIGraphFile)");
			e.printStackTrace();
			return finalRes;
		}

	}    

	// UPDATED TO NEW DATA FEEDS	
	@GET
	@Path("/buildGUIGraphNXDOMAINFile")
	@Produces( MediaType.APPLICATION_JSON )
	public String getGUIGraphNXDOMAINFile(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam,@QueryParam("save") String fileType, @QueryParam("layout") String layout)
	{		

		String date = dateParam;

		String finalRes ="";
		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphNXDOMAINFile; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphNXDOMAINFile; User: unkown");			
		}
		String methodName = commonMethods.getMethod("buildGUIGraphNXDOMAINFile");
		VIS_BuildGUIGraphNXDOMAINFile buildGUIGraphNXDOMAINFile = new VIS_BuildGUIGraphNXDOMAINFile();

		Properties graphProps = new Properties();
		if (layout != null) {
			graphProps.setProperty("defaultLayout", layout);
		}
		if (fileType != null){
			graphProps.setProperty("fileType", fileType);
		}

		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);
			
			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{	  
				date = date.replaceAll("-", "/");	          
				finalRes= buildGUIGraphNXDOMAINFile.buildGUIGraphNXDOMAINFile(db, date, hourParam, minutesParam, true,methodName,commonMethods, graphProps);
				//db.disconnect();
			}  

			return finalRes;
		} 
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /buildGUIGraphNXDOMAINFile)");
			e.printStackTrace();
			return finalRes;
		}

	}    

	// UPDATED TO NEW DATA FEEDS
	@GET
	@Path("/buildGUIGraphRDCATAFile")
	@Produces( MediaType.APPLICATION_JSON )
	public String getGUIGraphRDCATAFile(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam,@QueryParam("save") String fileType, @QueryParam("layout") String layout)
	{		

		String date = dateParam;

		String finalRes ="";
		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphRDCATAFile; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphRDCATAFile; User: unkown");			
		}
		String methodName = commonMethods.getMethod("buildGUIGraphRDCATAFile");
		VIS_BuildGUIGraphRDCATAFile buildGUIGraphRDCATAFile = new VIS_BuildGUIGraphRDCATAFile();

		Properties graphProps = new Properties();
		if (layout != null) {
			graphProps.setProperty("defaultLayout", layout);
		}
		if (fileType != null){
			graphProps.setProperty("fileType", fileType);
		}

		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);
			
			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{	  
				date = date.replaceAll("-", "/");	          
				finalRes= buildGUIGraphRDCATAFile.buildGUIGraphRDCATAFile(db, date, hourParam, minutesParam, true,methodName,commonMethods, graphProps);
				//db.disconnect();
			}  

			return finalRes;
		} 
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /buildGUIGraphRDCATAFile)");
			e.printStackTrace();
			return finalRes;
		}

	}

	@GET
	@Path("/buildGUIGraphDGASpecificFile")
	@Produces( MediaType.APPLICATION_JSON )
	public String getGUIGraphDGAFile(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam,@QueryParam("save") String fileType, @QueryParam("deg") String degree, @QueryParam("layout") String layout)
	{

		String date = dateParam;

		String finalRes ="";

		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphDGAFile; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphDGAFile; User: unkown");
		}
		String methodName = commonMethods.getMethod("buildGUIGraphDGAFile");
		VIS_BuildGUIGraphDGASpecificFile buildGUIGraphFile = new VIS_BuildGUIGraphDGASpecificFile();

		Properties graphProps = new Properties();
		if (degree != null) {
			try {
				int minOutDegree = Integer.parseInt(degree);
				graphProps.setProperty("minOutDegree", minOutDegree + "");
			} catch (Exception e) {
				
			}
		}
		if (layout != null) {
			graphProps.setProperty("defaultLayout", layout);
		}
		if (fileType != null){
			graphProps.setProperty("fileType", fileType);
		}

		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);

			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{
				date = date.replaceAll("-", "/");
				finalRes= buildGUIGraphFile.buildGUIGraphDGASpecificFile(db, date, hourParam, minutesParam, true,methodName,commonMethods, graphProps);
				//db.disconnect();
			}

			return finalRes;
		}
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /buildGUIGraphDGASpecificFile)");
			e.printStackTrace();
			return finalRes;
		}

	}
	
	@GET
	@Path("/buildGUIGraphDGAGenFile")
	@Produces( MediaType.APPLICATION_JSON )
	public String getGUIGraphDGAGenFile(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam,@QueryParam("save") String fileType, @QueryParam("deg") String degree, @QueryParam("layout") String layout)
	{

		String date = dateParam;

		String finalRes ="";

		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphDGAGenFile; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL buildGUIGraphDGAGenFile; User: unkown");
		}
		String methodName = commonMethods.getMethod("buildGUIGraphDGAGenFile");
		VIS_BuildGUIGraphDGAGenFile buildGUIGraphFile = new VIS_BuildGUIGraphDGAGenFile();

		Properties graphProps = new Properties();
		if (degree != null) {
			try {
				int minOutDegree = Integer.parseInt(degree);
				graphProps.setProperty("minOutDegree", minOutDegree + "");
			} catch (Exception e) {
				
			}
		}
		if (layout != null) {
			graphProps.setProperty("defaultLayout", layout);
		}
		if (fileType != null){
			graphProps.setProperty("fileType", fileType);
		}

		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);

			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{
				date = date.replaceAll("-", "/");
				finalRes= buildGUIGraphFile.buildGUIGraphDGAGenFile(db, date, hourParam, minutesParam, true,methodName,commonMethods, graphProps);
				//db.disconnect();
			}

			return finalRes;
		}
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /buildGUIGraphDGAGenFile)");
			e.printStackTrace();
			return finalRes;
		}

	}
	
	
	
	
	
	@GET
	@Path("/TrackAnalytics")
	@Produces( MediaType.APPLICATION_JSON )
	public String updateTrackTimeAnalyticsTable(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam)
	{		
		MISC_TrackAnalytics trackAnalytics = new MISC_TrackAnalytics();
		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL TrackAnalytics; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL TrackAnalytics; User: unkown");			
		}
		String date = dateParam;

		String finalRes ="";		
		
		String methodName = commonMethods.getMethod("TrackAnalytics");
		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);
			
			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{	  
				date = date.replaceAll("-", "/");	          
				finalRes= trackAnalytics.updateTrackTimeAnalyticsTable(db, date, hourParam, minutesParam, true, methodName,commonMethods);	 
				//db.disconnect();
			}  

			return finalRes;
		} 
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /TrackAnalytics)");
			e.printStackTrace();
			return finalRes;
		}

	}
	

	
		@GET
	@Path("/getHiveplotDGA")
	@Produces( MediaType.APPLICATION_JSON )
	public String getHiveplotDGA(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam,@QueryParam("deg") String degree)
	{

		String date = dateParam;

		String finalRes ="";

		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL getHiveplotDGA; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL getHiveplotDGA; User: unkown");
		}
		String methodName = commonMethods.getMethod("getHiveplotDGA");
		VIS_BuildHiveplotDGA buildHiveplotDGA = new VIS_BuildHiveplotDGA();
		
		Properties filterProps = new Properties();
		if (degree != null) {
			try {
				int minOutDegree = Integer.parseInt(degree);
				filterProps.setProperty("minOutDegree", minOutDegree + "");
			} catch (Exception e) {
				
			}
		}


		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);

			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{
				date = date.replaceAll("-", "/");
				finalRes= buildHiveplotDGA.buildHiveplotDGA(db, date, hourParam, minutesParam, true, methodName, commonMethods, filterProps);
				//db.disconnect();
			}

			return finalRes;
		}
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /getHiveplotDGA)");
			e.printStackTrace();
			return finalRes;
		}

	}

	@GET
	@Path("/getHiveplotBL")
	@Produces( MediaType.APPLICATION_JSON )
	public String getHiveplotBL(@QueryParam("date") String dateParam,@QueryParam("hour") String hourParam,@QueryParam("minutes") String minutesParam,@QueryParam("deg") String degree)
	{

		String date = dateParam;

		String finalRes ="";

		CommonMethods commonMethods = new CommonMethods();
		try {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL getHiveplotBL; User: " + security.getUserPrincipal().getName());
		} catch (Exception printException) {
			commonMethods.printMessage("RESTAPICALL", "*** RESTAPICALL getHiveplotBL; User: unkown");
		}
		String methodName = commonMethods.getMethod("getHiveplotBL");
		VIS_BuildHiveplotBL buildHiveplotBL = new VIS_BuildHiveplotBL();

		Properties filterProps = new Properties();
		if (degree != null) {
			try {
				int minOutDegree = Integer.parseInt(degree);
				filterProps.setProperty("minOutDegree", minOutDegree + "");
			} catch (Exception e) {
				
			}
		}

		try {
			commonMethods.processTimeFrame(dateParam, hourParam, minutesParam);

			ServletContext ctx = context;
			super.init(ctx);

			if (date!=null)
			{
				date = date.replaceAll("-", "/");
				finalRes= buildHiveplotBL.buildHiveplotBL(db, date, hourParam, minutesParam, true, methodName, commonMethods, filterProps);
				//db.disconnect();
			}

			return finalRes;
		}
		catch (Exception e)
		{
			System.err.println("AnalyticsWSAPI - Error (REST API: /getHiveplotBL)");
			e.printStackTrace();
			return finalRes;
		}

	}
	
}
