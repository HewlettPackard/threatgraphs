//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.


package com.hpl.hp.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.net.InternetDomainName;
import com.hp.hpl.bd4s.wfm.template.Vertica;

/**
 * Class to store and transfer data about a service
 * @author marco casassa mont
 *
 */

public class CommonMethods {

	String configString = null;
	private static String source = System.getenv("BD4S_CONFIG_PATH");
	public ServiceStruct serviceConfig = new ServiceStruct();
	public String tzCast;

	public String methodMapping = null;
	String methodConfigString = null;
	HashMap<String, ServiceStruct> mapMethodConfigs = new HashMap<>();

	public String timeFrameBasedProcessingType = null;


	/*public String timestampBuilder(String date) {
		String hmsGlobal = getGlobalVariable("hmsGlobal");
		String timeZoneGlobal = getGlobalVariable("Timezone");
		return date + " " + hmsGlobal + " " + timeZoneGlobal;	
	}*/


	public String retrieveIPDNFiltering(Vertica ver, String date, String constraintIP, String constraintDN, boolean disconnect) {

		String res = "";
		ResultSet rs = null;
		Connection conn;

		try { 
			printMessage("COMPUTATION_FLOW", "retrieveIPDNFiltering");

			conn = ver.connection();
			Statement stmt = conn.createStatement();  

			String methodName = getMethod("Internal-CommonMethods");
			String query1 = getQuery(methodName, "query1");
			
			res = "AND "+ constraintIP + " not in (" + query1 + ")";
			
			
			// check presence of IP addresses to filter out 
			/*rs = stmt.executeQuery("select srcip from IPFiltering");

			while (rs.next())
			{
				res = res + " AND "+ constraintIP + " != '"+rs.getString(1).trim() +"' ";	  						  	  
			}*/

			// check presence of domain names to filter out 
			String query2 = getQuery(methodName, "query2");
			
			rs = stmt.executeQuery(query2);
			
			

			while (rs.next())
			{
				res = res + " AND "+ constraintDN + " not like '%"+rs.getString(1).trim() +"' ";	  						  	  
			}		  


			//System.out.println("Constraint Result: " + res);
			printMessage("RESULT", "Constraint Result: " + res);
			rs.close();
			stmt.close();
			if (disconnect) {ver.disconnect(conn);};       

		} 

		catch (Exception e) 
		{ 
			System.err.println("Problem with database."); 
			e.printStackTrace(); return ""; // Bail out. We cannot do anything further. 
		} 

		return res;
	}
	private HashMap<String, String> parameters = new HashMap<>();

	/**
	 * Constructor to load the json file in the service struct format
	 */
	public CommonMethods(){


		try {

			BufferedReader br = new BufferedReader(new FileReader(source));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					sb.append(line);
					line = br.readLine();
				}
				this.configString = sb.toString();
				JSONArray config = new JSONObject(configString).
						getJSONArray("BD4SanalyticConfig");
				for (int i=0; i < config.length(); i++) {
					this.loadConfig(new JSONObject (config.get(i).toString()));
				}

			} finally {
				br.close();
			}
		} catch (Exception e) {
			System.err.println("Config File '"+source+"' " +
					"not found on local system or parsing error");
			e.printStackTrace();
		}

		if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
		{
		  this.tzCast =  " ";
		}
		else
		{
		  this.tzCast =  " at time zone '"+getGlobalVariable("Timezone")+"' ";
		}
	}

	private void loadConfig(JSONObject config) {

		Iterator<?> keys = config.keys();

		try {
			while (keys.hasNext()) {
				String key = (String) keys.next();
				serviceConfig.add(key, config.get(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void loadMethod(){

		try {
			String staticPath = getGlobalVariable("ConfigFilesPath");
			methodMapping = staticPath+methodMapping;
			BufferedReader br = new BufferedReader(new FileReader(methodMapping));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					sb.append(line);
					line = br.readLine();
				}
				this.methodConfigString= sb.toString();
				JSONArray config = new JSONObject(methodConfigString).
						getJSONArray("MethodConfigurationFile");
				for (int i=0; i < config.length(); i++) {
					this.loadMethodConfig(new JSONObject (config.get(i).toString()));
				}

			} finally {
				br.close();
			}
		} catch (Exception e) {
			System.err.println("Config File '"+source+"' " +
					"not found on local system or parsing error");
			e.printStackTrace();
		}
	}

	private ArrayList<String> getKeys(JSONObject methodObject){

		ArrayList<String> resultKeys = new ArrayList<>();

		Iterator<?> keys = methodObject.keys();

		while(keys.hasNext()){
			String key = (String) keys.next();
			resultKeys.add(key);
		}
		return resultKeys;

	}

	private void loadMethodConfig(JSONObject config) {

		ArrayList<String> methodKeys = new ArrayList<>();

		JSONArray methodArray = config.getJSONArray("MethodDefinition");

		for(int i= 0 ; i < methodArray.length(); i++)
		{
			methodKeys = this.getKeys(new JSONObject(methodArray.get(i).toString()));
		}

		ServiceStruct methodConfig = new ServiceStruct();
		Iterator<?> keys = config.keys();

		try {
			while (keys.hasNext()) {
				String key = (String) keys.next();
				methodConfig.add(key, config.get(key));
			}
			for(String key : methodKeys){
				mapMethodConfigs.put(key, methodConfig);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * given the restAPI returns the method that is associated with the rest API
	 * @param restAPI
	 * @return the method assocaited with the restAPI
	 */
	public String getMethod(String parameter){
		JSONArray mapRestAPI = new JSONArray(serviceConfig.getString("MapRestAPI"));
		for(int i = 0; i < mapRestAPI.length() ; i++){
			JSONObject obj = mapRestAPI.getJSONObject(i);
			if(obj.has("RestAPI"))
				if(obj.getString("RestAPI").equalsIgnoreCase(parameter) && obj.has("AnalyticMethod")){
					methodMapping = obj.getString("ConfigFile");
					loadMethod();
					return obj.getString("AnalyticMethod");
				}
		}
		throw new IllegalArgumentException(parameter + "not found");
		//return "mehod not found";
	}


	/**
	 * given the name of the global variable that is needed, it searches the json file and returns the global variable if it finds it.
	 * @param the name of the global variable that is needed
	 * @return the global variable
	 */
	public String getGlobalVariable(String parameter){

		JSONArray globalVaiables = new JSONArray(serviceConfig.getString("GlobalVariable"));
		for(int i = 0; i < globalVaiables.length() ; i++){
			JSONObject obj = globalVaiables.getJSONObject(i);

			if(obj.has(parameter))
				return obj.getString(parameter);
		}
		throw new IllegalArgumentException(parameter + "not found");
		//return "global variable not found";
	}	



	/**
	 * Method used to get the local parameters which are variables .
	 * @param methodName
	 * @param parameterName
	 * @return The variable paramater
	 */
	public String getLocalVariable(String methodName , String parameterName){
		JSONArray methodContents;
		JSONArray methodDefination = new JSONArray(mapMethodConfigs.get(methodName).getString("MethodDefinition"));

		for(int i= 0 ; i < methodDefination.length() ; i++){

			JSONObject jsonObject = methodDefination.getJSONObject(i);


			//this will load the correct method in the array
			methodContents = jsonObject.getJSONArray(methodName);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject type = methodContents.getJSONObject(j);
				if(type.has("type"))
					if(type.getString("type").equalsIgnoreCase("LocalVariables") && type.has(parameterName))
						return type.getString(parameterName);
			}
		}
		throw new IllegalArgumentException(parameterName + "not found");
		//return "local variable cannot be found";
	}

	/*public String getDynamicMethodInformation(int loop, String parameterName){
		JSONArray methodContents;
		JSONArray dynamicMethodDefination = new JSONArray(serviceConfig.getString("DynmaicMethodsInformation"));

		for(int i= 0 ; i < dynamicMethodDefination.length() ; i++){

			JSONObject jsonObject = dynamicMethodDefination.getJSONObject(i);


			//this will load the correct method in the array
			methodContents = jsonObject.getJSONArray("dynamicMethod"+loop);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject type = methodContents.getJSONObject(j);

				//if(type.getString("type").equalsIgnoreCase("dynamicMethod"+loop))
				return type.getString(parameterName);
			}
		}
		return "Information cannot be found";
	}*/


	/**
	 * Method used to set the parameter in the hashmap that will replace the parameters in the 
	 * actual query
	 * @param parameter
	 * @param parameterValue
	 */
	public void setParameters(String parameter , String parameterValue){

		parameters.put(parameter, parameterValue);

	}

	public void printHashmapParameters(String parameter){

		System.out.println(parameters.get(parameter));
	}

	/**
	 * Method used to get a query from the given method name
	 * @param methodName
	 * @param requiredQuery
	 * @return The expected query
	 */
	public String getQuery(String methodName , String requiredQuery){

		JSONArray methodDefination = new JSONArray(mapMethodConfigs.get(methodName).getString("MethodDefinition"));
		for(int i = 0 ; i < methodDefination.length() ; i++){

			JSONObject jsonObject = methodDefination.getJSONObject(i);

			JSONArray methodContents = jsonObject.getJSONArray(methodName);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject type = methodContents.getJSONObject(j);
				if(type.has("type"))
					if(type.getString("type").equalsIgnoreCase("query") && type.has(requiredQuery)){
						return type.getString(requiredQuery);
					}
			}
		}
		throw new IllegalArgumentException(requiredQuery + "not found");
	}
	
	public String getQueryMultiline(String methodName, String requiredQuery) {
		JSONArray methodDefination = new JSONArray(mapMethodConfigs.get(methodName).getString("MethodDefinition"));
		for(int i = 0 ; i < methodDefination.length() ; i++){

			JSONObject jsonObject = methodDefination.getJSONObject(i);

			JSONArray methodContents = jsonObject.getJSONArray(methodName);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject type = methodContents.getJSONObject(j);
				if(type.has("type"))
					if(type.getString("type").equalsIgnoreCase("query") && type.has(requiredQuery)){
						
						JSONArray queryArray = type.getJSONArray(requiredQuery);
						StringBuilder sb = new StringBuilder();
						for (int k = 0; k < queryArray.length(); ++k) {
							sb.append(queryArray.getString(k)).append("\n");
						}
						return sb.toString();
						
					}
			}
		}
		throw new IllegalArgumentException(requiredQuery + "not found");
	}


	/**
	 * Method used to get the specified parameter from a specified query and method
	 * @param methodName
	 * @param query
	 * @param paramRequired
	 * @return The query parameter
	 */
	public String getQueryParam(String methodName, String query , String paramRequired){

		JSONArray methodDefination = new JSONArray(mapMethodConfigs.get(methodName).getString("MethodDefinition"));
		for(int i = 0 ; i < methodDefination.length() ; i++){

			JSONObject jsonObject = methodDefination.getJSONObject(i);

			JSONArray methodContents = jsonObject.getJSONArray(methodName);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject type = methodContents.getJSONObject(j);
				if(type.has("type"))
					if(type.getString("type").equalsIgnoreCase("query") && type.has(query) && type.has("param")){

						JSONArray queryParameters = type.getJSONArray("param");
						for(int k = 0 ; k < queryParameters.length() ; k++){
							JSONObject param = queryParameters.getJSONObject(k);
							if(param.has(paramRequired))
								return param.getString(paramRequired);
						}

					}
			}
		}
		throw new IllegalArgumentException(paramRequired + "not found");
	}

	/**
	 * Given a query, if the parameters are set in the hashmap, it will replave the parameters
	 * in the query with the appropriate values.
	 * @param query
	 * @return The query with the correct parameter.
	 */
	public String automatedReplaceQueryParameter( String query){

		for (Object key : parameters.keySet()) {
			if(query.contains(key.toString()) && parameters.get(key) != null)
				query = query.replaceAll(key.toString(), parameters.get(key).toString());

		}
		return query;
	}




	/**
	 * Replaces a single parameter in the query.
	 * @param query
	 * @param parameterToBeReplaced
	 * @param parameterValue
	 * @return The query with the specified parameter replaced
	 */
	public String replaceQueryParam(String query, String parameterToBeReplaced,
			String parameterValue) {
		if(query.contains(parameterToBeReplaced))
			query = query.replaceAll(parameterToBeReplaced, parameterValue);
		return query;
	}


	public int arrayLength(String arrayName){

		JSONArray requiredArray = new JSONArray(serviceConfig.getString(arrayName));
		return requiredArray.length();
	}

	/*public void executeAnonymousArray(String arrayName, int index, String typeDefination, String objectName){
		JSONArray requiredArray = new JSONArray(serviceConfig.getString(arrayName));
		if(index == 0){
			for(int i = 0 ; i < requiredArray.length() ; i++){

				JSONObject type = requiredArray.getJSONObject(i);
				if(type.has("type"))
					if(type.getString("type").equalsIgnoreCase(typeDefination) && type.has(objectName)){

						String value = type.getString(objectName);

						//ver.connect();   
						// Statement stmt = ver.conn.createStatement();
						//stmt.executeUpdate(query);

						System.out.println(value);

					}
			}
		}
		else{
			JSONObject jsonObject = requiredArray.getJSONObject(index-1);
			String value = jsonObject.getString(objectName);
			//ver.connect();   
			// Statement stmt = ver.conn.createStatement();
			//stmt.executeUpdate(query);
			System.out.println(value);
		}

	}*/

	public int retrieveMethodNumberItems(String methodName , String arrayName){
		JSONArray methodDefination = new JSONArray(mapMethodConfigs.get(methodName).getString("MethodDefinition"));

		for(int i = 0 ; i < methodDefination.length() ; i++){

			JSONObject jsonObject = methodDefination.getJSONObject(i);

			JSONArray methodContents = jsonObject.getJSONArray(methodName);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject obj = methodContents.getJSONObject(j);
				if(obj.has(arrayName)){
					JSONArray requiredArray = obj.getJSONArray(arrayName);
					return requiredArray.length();
				}
			}

		}
		throw new IllegalArgumentException(arrayName + "not found");

	}

	public int retrieveGVNumberItems( String arrayName){
		JSONArray globalDefination = new JSONArray(serviceConfig.getString("GlobalVariable"));

		for(int i = 0 ; i < globalDefination.length() ; i++){

			JSONObject jsonObject = globalDefination.getJSONObject(i);

			if(jsonObject.has(arrayName)){
				JSONArray requiredArray = jsonObject.getJSONArray(arrayName);
				return requiredArray.length();
			}

		}
		throw new IllegalArgumentException(arrayName + "not found");

	}

	public String retrieveGVArrayItem(String arrayName , String itemName){

		JSONArray globalDefination = new JSONArray(serviceConfig.getString("GlobalVariable"));

		for(int i = 0 ; i < globalDefination.length() ; i++){

			JSONObject jsonObject = globalDefination.getJSONObject(i);

			if(jsonObject.has(arrayName)){
				JSONArray requiredArray = jsonObject.getJSONArray(arrayName);

				for(int j = 0;j < requiredArray.length() ; j++){
					JSONObject obj = requiredArray.getJSONObject(j);
					if(obj.has(itemName)){

						return obj.getString(itemName);
					}
				}

			}
		}
		throw new IllegalArgumentException(itemName + "not found");

	}

	public String retrieveMethodArrayItem( String methodName, String arrayName , String itemName){

		JSONArray methodDefination = new JSONArray(mapMethodConfigs.get(methodName).getString("MethodDefinition"));
		for(int i = 0 ; i < methodDefination.length() ; i++){

			JSONObject jsonObject = methodDefination.getJSONObject(i);

			JSONArray methodContents = jsonObject.getJSONArray(methodName);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject type = methodContents.getJSONObject(j);
				if(  type.has(arrayName)){

					JSONArray queryParameters = type.getJSONArray(arrayName);
					for(int k = 0 ; k < queryParameters.length() ; k++){
						JSONObject param = queryParameters.getJSONObject(k);
						if(param.has(itemName))
							return param.getString(itemName);
					}

				}
			}
		}
		throw new IllegalArgumentException(itemName + "not found");

	}

	/**
	 * Method to print the messages based on the label stored in the configuration file.
	 * 
	 * @param label
	 * @param message
	 */
	public void printMessage(String label,String message){
		String labelValue = null;
		try{
			for(int i= 1 ; i <= retrieveGVNumberItems("BD4SMessagePrinting");i++){
				if(retrieveGVArrayItem("BD4SMessagePrinting", "type" + i).equalsIgnoreCase(label)){
					labelValue = retrieveGVArrayItem("BD4SMessagePrinting", "print" + i);
				}
			}

			if(labelValue.equalsIgnoreCase("yes"))
				System.out.println(message);
		}
		catch(Exception e){
			System.out.println("Invalid Label name, please check!");
		}
	}


	/**
	 * Method used to get the tables that are specified in the json file which will in turn 
	 * call the create table method in order to create the  tables if they do not exist
	 * @param ver
	 * @param methodName
	 */
	public void getTables(Vertica ver,String methodName){


		String table = null;
		int counter = 1;
		JSONArray methodDefination = new JSONArray(mapMethodConfigs.get(methodName).getString("MethodDefinition"));
		for(int i = 0 ; i < methodDefination.length() ; i++){

			JSONObject jsonObject = methodDefination.getJSONObject(i);

			JSONArray methodContents = jsonObject.getJSONArray(methodName);

			for(int j = 0;j < methodContents.length() ; j++){
				JSONObject type = methodContents.getJSONObject(j);
				if(type.has("type"))
					if(type.getString("type").equalsIgnoreCase("tableNames")){


						while(type.has("table" + counter)){
							table = type.getString("table" + counter);
							counter++;
							printMessage("COMPUTATION_FLOW", "***METHOD getTables : " + table);
							//to do - write code to create the tables
							Createtable(ver,table);
						}
					}
			}
		}
	}


	/**
	 * Method will try to create the tables if they do not exsist in the database. Only the 
	 * tablename has to be supplied
	 * @param ver
	 * @param table
	 */
	public void Createtable(Vertica ver, String table) {

		JSONArray methodDefination = new JSONArray(serviceConfig.getString("GlobalVariable"));
		JSONArray methodContents;
		Connection conn;


		try {


			conn = ver.connection();
			Statement stmt = conn.createStatement();  

			for(int i = 0; i < methodDefination.length() ; i++){

				JSONObject jsonObject = methodDefination.getJSONObject(i);

				//this will load the correct method in the array
				if(jsonObject.has("createTables")){
					methodContents = jsonObject.getJSONArray("createTables");

					for(int j = 0;j < methodContents.length() ; j++){
						JSONObject type = methodContents.getJSONObject(j);
						if(type.has(table)){

							JSONObject obj = type.getJSONObject(table);
							String query = obj.getString("createQuery");

							//System.out.println("*** COMMON METHOD getTable/Createtable: " + query);
							printMessage("COMPUTATION_FLOW", "*** COMMON METHOD getTable/Createtable: " + query);
							stmt.executeUpdate(query);

						}


					}
				}

			}
			stmt.close();
			ver.disconnect(conn);	

		} catch (Exception e) {
			e.printStackTrace();		
		}

	}


	public void deleteTables(Vertica ver, String methodName, String date){


        Connection conn;
        String executingQuery = "";

		//To UNCOMMENT	
		//conn = ver.connection();
	    //Statement stmt = conn.createStatement();
		
		System.out.println(retrieveGVNumberItems("deleteArray"));

		setParameters("<param1>", date);
		setParameters("<param2>", tzCast);
		for(int i =1 ; i <= retrieveGVNumberItems("deleteArray") ; i++){

			System.out.println(retrieveGVArrayItem( "deleteArray", "deleteQuery" + i));
			//To UNCOMMENT
			//executingQuery = automatedReplaceQueryParameter(retrieveGVArrayItem( "deleteArray", "deleteQuery" + i));
			//stmt.executeUpdate(executingQuery);
		}

	}



	public String getIPAddr(String dn)
	{
		String result ="";
		InetAddress addr; 
		try
		{
			addr = InetAddress.getByName(dn);
			result = addr.getHostAddress();
		}
		catch(Exception e)
		{
			//System.err.println("Domain Address not resolving in IP address");
			//e.printStackTrace(); 
			result = "NXDOMAIN";
		}


		return result;

	}

	public String getHostname(String ipAddress)
	{
		String result ="";
		String host="";
		InetAddress addr; 
		try
		{
			addr = InetAddress.getByName(ipAddress);
			host = addr.getHostName();
			//System.out.println(host);  
			result= host;
		}
		catch(Exception e)
		{
			System.err.println("Error whilst retrieving hostname from IP address");
			e.printStackTrace();  
			result=ipAddress;
		}


		return result;

	}


	public String retrieveDNFiltering(Vertica ver, String date, String constraintDN, boolean disconnect) {

		String res = "";
		ResultSet rs = null;
		Connection conn;

		try { 
			printMessage("COMPUTATION_FLOW", "retrieveDNFiltering");

			conn = ver.connection();
			Statement stmt = conn.createStatement();  

			// check presence of domain names to filter out 
			rs = stmt.executeQuery("select DNname from DNFiltering");

			while (rs.next())
			{
				res = res + " AND "+ constraintDN + " not like '%"+rs.getString(1).trim() +"' ";	  						  	  
			}		  

			printMessage("RESULT", "Constraint Result: " + res);

			rs.close();
			stmt.close();
			if (disconnect) {ver.disconnect(conn);};       

		} 

		catch (Exception e) 
		{ 
			System.err.println("Problem with database."); 
			e.printStackTrace(); return ""; // Bail out. We cannot do anything further. 
		} 

		return res;
	}

	public int checkForRandomnessPatterns(String name) {

		int counter = 0;
		int numEntries = 117;
		String[] entry = new String[numEntries]; 


		// Setting entries with prob=0 for english language

		entry[0]="bk";
		entry[1]="bq";	
		entry[2]="bx";	
		entry[3]="bz";	
		entry[4]="cg";	
		entry[5]="cj";	
		entry[6]="cm";	
		entry[7]="cv";	
		entry[8]="cw";	
		entry[9]="cx";	
		entry[10]="dx";	
		entry[11]="dz";	
		entry[12]="dj";	
		entry[13]="dx";	
		entry[14]="dz";	
		entry[15]="gq";	
		entry[16]="gx";		
		entry[17]="hj";	
		entry[18]="hv";	
		entry[19]="hx";	
		entry[20]="hz";	
		entry[21]="iy";	
		entry[22]="jb";	
		entry[23]="jc";	
		entry[24]="jd";	
		entry[25]="jf";		
		entry[26]="jg";	
		entry[27]="jh";	
		entry[28]="jj";	
		entry[29]="jk";	
		entry[30]="jl";	
		entry[31]="jm";	
		entry[32]="jn";	
		entry[33]="jp";		
		entry[34]="jq";	
		entry[35]="jr";	
		entry[36]="js";	
		entry[37]="jt";		
		entry[38]="jv";	
		entry[39]="jw";	
		entry[40]="jx";		
		entry[41]="jz";
		entry[42]="kj";		
		entry[43]="kq";	
		entry[44]="kv";	
		entry[45]="kx";		
		entry[46]="kz";	
		entry[47]="lx";		
		entry[48]="mk";	
		entry[49]="mq";		
		entry[50]="mx";	
		entry[51]="mz";	
		entry[52]="pq";	
		entry[53]="pv";		
		entry[54]="px";	
		entry[55]="pz";		
		entry[56]="qa";	
		entry[57]="qb";	
		entry[58]="qc";	
		entry[59]="qd";		
		entry[60]="qe";	
		entry[61]="qf";	
		entry[62]="qg";		
		entry[63]="qh";
		entry[64]="qi";		
		entry[65]="qj";	
		entry[66]="qk";	
		entry[67]="ql";		
		entry[68]="qm";	
		entry[69]="qn";		
		entry[70]="qo";	
		entry[71]="qp";		
		entry[72]="qq";	
		entry[73]="qt";	
		entry[74]="qv";	
		entry[75]="qw";		
		entry[76]="qx";	
		entry[77]="qy";	
		entry[78]="qz";	
		entry[79]="sx";	
		entry[80]="sz";	
		entry[81]="tx";		
		entry[82]="vb";		
		entry[83]="vc";	
		entry[84]="vf";		
		entry[85]="vg";	
		entry[86]="vj";	
		entry[87]="vk";	
		entry[88]="vm";		
		entry[89]="vp";	
		entry[90]="vq";	
		entry[91]="vt";	
		entry[92]="vw";	
		entry[93]="vx";	
		entry[94]="vz";	
		entry[95]="vq";	
		entry[96]="wj";	
		entry[97]="wv";	
		entry[98]="wx";	
		entry[99]="wz";		
		entry[100]="xa";	
		entry[101]="xf";	
		entry[102]="xj";	
		entry[103]="xk";	
		entry[104]="xn";	
		entry[105]="xr";	
		entry[106]="xz";		
		entry[107]="yq";	
		entry[108]="zf";		
		entry[109]="zg";	
		entry[110]="zk";	
		entry[111]="zm";	
		entry[112]="zn";	
		entry[113]="zp";	
		entry[114]="zq";	
		entry[115]="zw";		
		entry[116]="zz";	

		try { 


			for (int i=0;i<numEntries;i++)
			{
				if (name.contains(entry[i]))
				{
					counter++;
				}		  
			}

			//System.out.println("Name: " + name +", "+ counter);


		} 

		catch (Exception e) 
		{ 

			e.printStackTrace(); return counter; // Bail out. We cannot do anything further. 
		} 

		return counter;
	}



	public ParserOutcome domainProcessing(String name) 

	{
		ParserOutcome po = new ParserOutcome();	
		int num_COLUMNS = po.num_COLUMNS;   //1 additional Overflow column is available
		InternetDomainName idn; 
		String ps = "";
		String leftPart = "";
		String dn = "";

		int dLen = 0;
		int numColFill;
		int numCol;			  
		int numDNComp;
		int overflowFlag;

		po.request=name;

		for (int i=0; i<num_COLUMNS;i++)
		{po.dn[i] = " ";};


		if (po.request !="")
		{

			//--------- START DN Processing 

			try{

				dn = po.request;
				ps = "";
				leftPart= "";



				//leftPart = dn;

				// checking validity of ND name
				if(InternetDomainName.isValid(dn))
				{
					//DN name VALID
					idn = InternetDomainName.from(dn); 

					// Checking public Suffix
					if (idn.hasPublicSuffix())
					{
						ps = idn.publicSuffix().toString();

						// Checking if the DN is just a public suffix
						if (idn.isPublicSuffix())
						{
							leftPart = "";
						}
						else 
						{
							leftPart = dn.substring(0, (dn.length() - ps.length()-1));
						}
					}
					else
					{
						// No Public Suffix (e.g. IP address, localhost, etc.
						ps = "";
						leftPart = dn;
					}
				}
				else
				{
					// Invalid DN
					//System.out.println("NOT Valid DN");
					ps = "";
					leftPart = dn;
				} 

			}
			catch (Exception e)
			{
				// System.out.println("ERROR in DN Name" );
				e.printStackTrace();
				ps = "";
				leftPart = dn;
			}


			//------- Debugging
			// System.out.println("domain name: " + dn); 
			//System.out.println("public suffix: " + ps); 
			//System.out.println("leftpart: " + leftPart); 

			String[] domainsFull = new String[num_COLUMNS];
			String overflow_Column = " ";   //content of overflow column

			numColFill = num_COLUMNS;
			numCol = 0;			  
			numDNComp = 0;

			if (ps !="")
			{
				numColFill = numColFill - 1;
				domainsFull[0] = ps;
				numCol = 1;
				numDNComp = 1;
			}

			if (leftPart!="")
			{
				leftPart = leftPart.replaceAll("\\s","");

				String[] domains = leftPart.split("\\.");

				dLen = domains.length;
				numDNComp = numDNComp + dLen;
				//System.out.println("Num sub domains: " + dLen);


				for (int i=0;i<numColFill;i++)
				{ 
					if ((dLen-1-i)<0)
					{
						domainsFull[numCol] = " ";
					}
					else
					{
						domainsFull[numCol] = domains[dLen-1-i];
					}

					//System.out.println(domainsFull[numCol]+ "\n");
					numCol++;
				}	

				if (dLen>numColFill)
				{
					for (int i =0; i<(dLen-numColFill); i++)
					{
						if (i==0)
						{
							overflow_Column = domains[i];
						}
						else
						{
							overflow_Column = overflow_Column + "."+ domains[i];
						}			 
					}		  			  
				}
				//System.out.println("OVERFLOW: " + overflow_Column);
			}

			else
			{
				for (int i=0;i<numColFill;i++)
				{ 	
					domainsFull[numCol] = " ";
					numCol++;
					//System.out.println(domainsFull[numCol]+ "\n");
				}	

			}

			overflowFlag = 0;
			if (overflow_Column != " ")
			{
				overflowFlag = 1;
			}

			// Filling in the ParserOutput data Structure
			//po.valid = true;


			for (int i=0; i<num_COLUMNS;i++)
			{po.dn[i] = domainsFull[i];};


			po.overflow_Column = overflow_Column;
			po.overflowFlag = overflowFlag;
			po.numDNComp = numDNComp;


		}
		else
		{
			po.request = "***NONE***";
		}





		return po; 
	}

	//*******************************
	//Function: checkAbsenceFalsePositivePatterns
	//Purpose: checking for FalsePositive patters within a string
	//
	//History: UPDATED TO NEW DNS DATA FEEDS
	//********************************


	public boolean checkAbsenceFalsePositivePatterns(String d1, String req) 
	{
		boolean flag = true;

		try
		{
			if (d1.toLowerCase().contains("xn--") || req.toLowerCase().contains(".pl") || req.toLowerCase().contains(".hu") || req.toLowerCase().contains(".cz") || req.toLowerCase().contains(".kz")) 
			{
				flag=false;
			}
		}
		catch (Exception e)
		{
			System.err.println("Problem with RG string pattern detection."); 
			e.printStackTrace(); return false;  
		}

		return flag;	
	}



	/**
	 * @param dateParameter
	 * @param hourParameter
	 * @param minuteParameter
	 */
	public void processTimeFrame(String dateParameter, String hourParameter, String minuteParameter){

		String timeFrameFlag = getGlobalVariable("TIMEFRAME");
		if(dateParameter==null)
			throw new IllegalArgumentException("Invalid Parameter, date not given");

		if(hourParameter  == null && minuteParameter != null){
			System.out.println("invalid parameters, hour value not present while minutes values available");
			throw new IllegalArgumentException("invalid parameters, hour value not present while minutes values available");
		}

		if( hourParameter != null && minuteParameter!=null){
			if(timeFrameFlag.equalsIgnoreCase("finegrain")){
			
					timeFrameBasedProcessingType = "finegrain";
					}
			else if(timeFrameFlag.equalsIgnoreCase("daily"))
				timeFrameBasedProcessingType = "daily";
			else 
				throw new IllegalArgumentException("Mismatch between parameter passed and flag set in configuration file");
	
		}
		else if( hourParameter != null && minuteParameter==null){
			
			if(timeFrameFlag.equalsIgnoreCase("finegrain")){
							
				timeFrameBasedProcessingType = "finegrain";
				
			}
			else if(timeFrameFlag.equalsIgnoreCase("daily"))
				timeFrameBasedProcessingType = "daily";
			
			else 
				throw new IllegalArgumentException("Mismatch between parameter passed and flag set in configuration file");
	
		}
		else if(timeFrameFlag.equalsIgnoreCase("daily"))
			timeFrameBasedProcessingType = "daily";
		else 
			throw new IllegalArgumentException("Mismatch between parameter passed and flag set in configuration file");
	}

	/**
	 * creates the date parameter for the query based on the computation that has to be carried out.
	 * 
	 * In finegrain mode, This method creates date parameter for time range which in between the starting of the day and the time 
	 * that is passed, i.e the time upto which the computation is to be carried out. 
	 * Sample result in fine grain mode : (timestamp at time zone 'PDT' ) >='2014/10/14 00:00:00' AND (timestamp at time zone 'PDT' ) <='2014/10/14 01:59:59'
	 *
	 * In daily mode, if the dailyTimeStampType is set to automatic, it will generate the time parameter to incorporate the date,
	 * Sample result in daily mode: date(timestamp at time zone) = '2014/10/14 01:59:59'
	 * 
	 * @param date
	 * @param hour
	 * @param minutes
	 * @param dailyTimeStampType
	 * @param type - finegrain or daily to make the parameter accordingly
	 * @return the date based on the query that has to be executed
	 */
	public String timeStampBuilderForTimeInterval(String date, String hourParam, String minutesParam, String timeStampLiteral,String dailyTimeStampType){
		String result = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			if(timeFrameBasedProcessingType.equals("finegrain")){

				if(minutesParam ==null)
					minutesParam = "00";

				int hour = Integer.parseInt(hourParam);
				int minutes = Integer.parseInt(minutesParam);

				if(hour>24 || minutes > 60 || hour < 0 || minutes < 0)
					throw new IllegalArgumentException("Value of parameter passed must be in the range of hours <= 24, minutes <= 60");

				String startDate = date + " 00:00:00";
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFormat.parse(startDate));

				startDate = dateFormat.format(cal.getTime());
				cal.add(Calendar.MINUTE, minutes);
				//cal.add(Calendar.HOUR_OF_DAY,(hour-1));
				cal.add(Calendar.HOUR_OF_DAY,(hour));
				//cal.add(Calendar.MINUTE,59);
				cal.add(Calendar.SECOND,59);
				String endDate = dateFormat.format(cal.getTime());
				if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
					result = "(" + timeStampLiteral + ") >='" + startDate + "' AND (" + timeStampLiteral + ") <='" + endDate + "'";
				else
					result = "(" + timeStampLiteral + tzCast + ") >='" + startDate + "' AND (" + timeStampLiteral + tzCast + ") <='" + endDate + "'";
			}
			else
				if(timeFrameBasedProcessingType.equals("daily")){
					if(dailyTimeStampType.equalsIgnoreCase("automatic"))
						result = formDailTimeStampBasedOnQuery(date, "daily", timeStampLiteral);
					else
						result = formDailTimeStampBasedOnQuery(date, dailyTimeStampType,timeStampLiteral);
				}
		} catch (ParseException e) {
			System.out.println("The input date was in wrong format");
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 * creates the date parameter for the query based on the computation that has to be carried out.
	 * 
	 * In finegrain mode, This method creates date parameter for multi day time range which in between the starting day and the time 
	 * that is passed, i.e the time upto which the computation is to be carried out. 
	 * Sample result in fine grain mode : (timestamp at time zone 'PDT' ) >='2014/10/12 07:00:00' AND (timestamp at time zone 'PDT' ) <='2014/10/10 01:50:59'
	 *
	 * NOTE: the daily mode setting here is ignored here to allow ranging across multiple days 
	 * 
	 * @param date
	 * @param hour
	 * @param minutes
	 * @param startDate
	 * @param startHour
	 * @param startMinutes
	 * @param dailyTimeStampType
	 * @param type - finegrain or daily to make the parameter accordingly
	 * @return the date based on the query that has to be executed
	 */
	public String timeStampBuilderForMultiDayTimeInterval(String date, String hourParam, String minutesParam, String startDateParam, String startHourParam, String startMinutesParam, String timeStampLiteral,String dailyTimeStampType){
		String result = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
		    // NOTE: bypassing the daily setting and related check to span across multiple days
			//	if(timeFrameBasedProcessingType.equals("finegrain")){

				if(minutesParam ==null)
					minutesParam = "00";

				int hour = Integer.parseInt(hourParam);
				int minutes = Integer.parseInt(minutesParam);
				int startHour = Integer.parseInt(startHourParam);
				int startMinutes = Integer.parseInt(startMinutesParam);

				if(hour>24 || minutes > 60 || hour < 0 || minutes < 0)
					throw new IllegalArgumentException("Value of parameter passed must be in the range of hours <= 24, minutes <= 60");

				if(startHour>24 || startMinutes > 60 || startHour < 0 || startMinutes < 0)
					throw new IllegalArgumentException("Value of start parameter passed must be in the range of hours <= 24, minutes <= 60");

				
				// Start date set from parameters
				// Input assumes that all parameters are properly formatted
				String startDate = startDateParam + " " +startHourParam +":" +startMinutesParam+ ":00";
				
				// Formatting startDate
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFormat.parse(startDate));
				startDate = dateFormat.format(cal.getTime());
				
				// Formatting endDate
				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(dateFormat.parse(date + " 00:00:00"));
				cal1.add(Calendar.MINUTE, minutes);
				//cal.add(Calendar.HOUR_OF_DAY,(hour-1));
				cal1.add(Calendar.HOUR_OF_DAY,(hour));
				//cal.add(Calendar.MINUTE,59);
				cal1.add(Calendar.SECOND,59);
				String endDate = dateFormat.format(cal1.getTime());
				if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
					result = "(" + timeStampLiteral + ") >='" + startDate + "' AND (" + timeStampLiteral + ") <='" + endDate + "'";
				else
					result = "(" + timeStampLiteral + tzCast + ") >='" + startDate + "' AND (" + timeStampLiteral + tzCast + ") <='" + endDate + "'";
		/*	}
			else
				if(timeFrameBasedProcessingType.equals("daily")){
					if(dailyTimeStampType.equalsIgnoreCase("automatic"))
						result = formDailTimeStampBasedOnQuery(date, "daily", timeStampLiteral);
					else
						result = formDailTimeStampBasedOnQuery(date, dailyTimeStampType,timeStampLiteral);
				}
	    */
		} catch (ParseException e) {
			System.out.println("The input date was in wrong format");
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * creates the date parameter for the query based on the computation that has to be carried out.
	 * 
	 * In finegrain mode, This method creates date parameter for multi day time range which in between the starting day and the time 
	 * that is passed, i.e the time upto which the computation is to be carried out. 
	 * Sample result in fine grain mode : (timestamp at time zone 'PDT' ) >='2014/10/12 07:00:00' AND (timestamp at time zone 'PDT' ) <='2014/10/10 01:50:59'
	 *
	 * NOTE: the daily mode setting here is ignored here to allow ranging across multiple days 
	 * 
	 * @param date
	 * @param hour
	 * @param minutes
	 * @param startDate
	 * @param startHour
	 * @param startMinutes
	 * @param type - finegrain or daily to make the parameter accordingly
	 * @return the date based on the query that has to be executed
	 */
	public String timeStampBuilderForFinegrainTimeInterval(String date, String hourParam, String minutesParam, String hoursBackParam, String minutesBackParam, String timeStampLiteral) {
		String result = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			if(minutesParam == null)
				minutesParam = "00";

			int hour = Integer.parseInt(hourParam);
			int minutes = Integer.parseInt(minutesParam);
			int hoursBack = Integer.parseInt(hoursBackParam);
			int minutesBack = Integer.parseInt(minutesBackParam);

			if(hour>24 || minutes > 60 || hour < 0 || minutes < 0)
				throw new IllegalArgumentException("Value of parameter passed must be in the range of hours <= 24, minutes <= 60");
			
			// Parsing date at midnight
			Date timeDate = dateFormat.parse(date + " 00:00:00");
			
			// Constructing startDate, same like endDate, but with hoursBack and minutesBack subtracted
			// Cloning calendars seems not to work.
			Calendar calStartDate = Calendar.getInstance();
			calStartDate.setTime(timeDate);
			calStartDate.add(Calendar.MINUTE, minutes);
			calStartDate.add(Calendar.HOUR_OF_DAY, hour);
			
			calStartDate.add(Calendar.HOUR_OF_DAY, -hoursBack);
			calStartDate.add(Calendar.MINUTE, -minutesBack);
			
			
			// Constructing endDate
			Calendar calEndDate = Calendar.getInstance();
			calEndDate.setTime(timeDate);
			calEndDate.add(Calendar.MINUTE, minutes);
			calEndDate.add(Calendar.HOUR_OF_DAY, hour);
			calEndDate.add(Calendar.SECOND, 59);
			
			// Formatting both startDate and endDate
			String startDate = dateFormat.format(calStartDate.getTime());
			String endDate = dateFormat.format(calEndDate.getTime());
			if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
				result = "(" + timeStampLiteral + ") >='" + startDate + "' AND (" + timeStampLiteral + ") <='" + endDate + "'";
			else
				result = "(" + timeStampLiteral + tzCast + ") >='" + startDate + "' AND (" + timeStampLiteral + tzCast + ") <='" + endDate + "'";

		} catch (ParseException e) { 
			// Breaking Bad: Common Meth...
			System.out.println("The input date was in wrong format");
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * creates the date parameter for the query based on the computation that has to be carried out.
	 * 
	 * Create constraint to check for timestamp based on the entire day, independently for settings, only based on operators and timezones
	 * Sample result in daily mode -operator "=": date(timestamp at time zone) = '2014/10/14 01:59:59'
	 * Sample result in daily mode -operator "<": date(timestamp at time zone) < '2014/10/14 01:59:59'
	 * 
	 * @param date
	 * @param operator
	 * @param dailyTimeStampType
	 * @return the date based on the query that has to be executed
	 */
	public String timeStampBuilderForDayAndOperator(String date, String operator, String timeStampLiteral){
		String result = null;

		try {

	
			if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
				result =  "date(" + timeStampLiteral + ") "+ operator +" '" + date +"'";
			else 
				result =  "date(" + timeStampLiteral + tzCast + ") " + operator +" '" + date +"'";

			
		} catch (Exception e) {
			System.out.println("The input date was in wrong format");
			e.printStackTrace();
		}
		return result;
	}
	
	

	/**
	 * @param date
	 * @param timeStampType
	 * @param timeStampLiteral
	 * @return
	 */
	private String formDailTimeStampBasedOnQuery(String date, String timeStampType,String timeStampLiteral){
		String result = null;
		if(timeStampType.equalsIgnoreCase("fullTime")){
			if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
				result = "(" + timeStampLiteral + ") = '" + date +" 23:59:59'";
			else
				result = "(" + timeStampLiteral + tzCast + ") = '" + date +" 23:59:59'";
		}
		else if(timeStampType.equalsIgnoreCase("daily")){
			if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
				result =  "date(" + timeStampLiteral + ") = '" + date +"'";
			else 
				result =  "date(" + timeStampLiteral + tzCast + ") = '" + date +"'";
		}
		return result;
	}

	
	
	/**
	 *  creates the date parameter for the query based on the computation that has to be carried out.
	 * In finegrain mode, This method creates time parameter which corresponds to the exact time that has to be queried. 
	 * Sample result in fine grain mode :  (timestamp at time zone 'PDT' ) ='2014/10/13 01:59:59'
	 *
	 * In daily mode, if the dailyTimeStampType is set to automatic, it will generate the time parameter which does not incorporate the date
	 * but has the time pointinf to the value of hmsglobal variable set in the configuratin file which is 23:59:59
	 * Sample result in daily mode: date(timestamp at time zone) = '2014/10/14 23:59:59'
	 * 
	 * @param date
	 * @param hourParam
	 * @param minutesParam
	 * @param timeStampLiteral
	 * @param dailyTimeStampType
	 * @return
	 */
	public String timeStampBuilderAtSpecificTime(String date, String hourParam, String minutesParam, String timeStampLiteral,String dailyTimeStampType){
		String result = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			if(timeFrameBasedProcessingType.equals("finegrain")){

				if(minutesParam ==null)
					minutesParam = "00";

				int hour = Integer.parseInt(hourParam);
				int minutes = Integer.parseInt(minutesParam);

				if(hour>24 || minutes > 60 || hour < 0 || minutes < 0)
					throw new IllegalArgumentException("Value of parameter passed must be in the range of hours <= 24, minutes <= 60");

				String startDate = date + " 00:00:00";
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFormat.parse(startDate));


				cal.add(Calendar.MINUTE, minutes);
				//cal.add(Calendar.HOUR_OF_DAY,(hour-1));
				cal.add(Calendar.HOUR_OF_DAY,(hour));
				//cal.add(Calendar.MINUTE,59);
				cal.add(Calendar.SECOND,59);
				String endDate = dateFormat.format(cal.getTime());
				//cal.add(Calendar.HOUR_OF_DAY,(-1));
				//startDate = dateFormat.format(cal.getTime());
				//result = "(" + timeStampLiteral + tzCast + ") >'" + startDate + "' AND (" + timeStampLiteral + tzCast + ") <='" + endDate + "'";
				
				
				//take away casting whne time zoe is utc and vertica time zone is UTC
				if(getGlobalVariable("Timezone").equalsIgnoreCase("UTC") && getGlobalVariable("VerticaServerTimezone").equalsIgnoreCase("UTC"))
					result = "(" + timeStampLiteral + ") ='" + endDate + "'";
				else
					result = "(" + timeStampLiteral + tzCast + ") ='" + endDate + "'";
			}
			else
				if(timeFrameBasedProcessingType.equals("daily")){
					if(dailyTimeStampType.equalsIgnoreCase("automatic"))
						result = formDailTimeStampBasedOnQuery(date, "fullTime", timeStampLiteral);
					else
						result = formDailTimeStampBasedOnQuery(date, dailyTimeStampType,timeStampLiteral);
				}
		} catch (ParseException e) {
			System.out.println("The input date was in wrong format");
			e.printStackTrace();
		}
		return result;
	}


	
	
	/**
	 * @param dateParam
	 * @param hourParam
	 * @param minutesParam
	 * @return
	 */
	public String timestampBuilder(String dateParam, String hourParam, String minutesParam) {
		String timeZoneGlobal = getGlobalVariable("Timezone");
		String hmsGlobal = getGlobalVariable("hmsGlobal");
		String finalDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		try {
			if(timeFrameBasedProcessingType.equalsIgnoreCase("finegrain")){
				if(minutesParam == null)
					minutesParam = "00";

				int hour = Integer.parseInt(hourParam);
				int minutes = Integer.parseInt(minutesParam);

				String startDate = dateParam + " 00:00:00";
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFormat.parse(startDate));
				cal.add(Calendar.MINUTE, minutes);
				//cal.add(Calendar.HOUR_OF_DAY,(hour-1));
				cal.add(Calendar.HOUR_OF_DAY,(hour));
				//cal.add(Calendar.MINUTE,59);
				cal.add(Calendar.SECOND,59);
				String endDate = dateFormat.format(cal.getTime());

				finalDate= endDate +  " " + timeZoneGlobal;
			}
			else if(timeFrameBasedProcessingType.equalsIgnoreCase("daily"))
				finalDate = dateParam + " " + hmsGlobal + " " + timeZoneGlobal;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}	

		return finalDate;
	}


	/**
	 * @param resultSet
	 * @param positions
	 * @param label
	 * @return
	 * @throws SQLException
	 */
	public String generateResult(ResultSet resultSet,int[] positions, HashMap<Integer, String> label) throws SQLException{

		String res = null;
		while (resultSet.next())
		{
			if (res==null)
			{
				res= "{";
				for(int key: positions){
					res= res + "\"" + label.get(key) + "\":\"" + (resultSet.getString(key)).trim() + "\", ";

				}

				int ind = res.lastIndexOf(",");
				res = new StringBuilder(res).replace(ind, ind+1,"").toString();
				res = res + "}";
				//res= "{\"id\":\""+ (rs.getString(1)).trim() +"\", \"value\":\""+(rs.getString(2)).trim()+ "\", \"source\":\""+(rs.getString(3)).trim() +"\"}";
			}

			else
			{

				res= res+  ", "+ "{";
				for(int key: positions){
					res= res + "\"" + label.get(key) + "\":\"" + (resultSet.getString(key)).trim() + "\",";

				}

				int ind = res.lastIndexOf(",");
				res = new StringBuilder(res).replace(ind, ind+1,"").toString();
				res = res + "}";


				//res=res+", "+ "{\"id\":\""+ (rs.getString(1)).trim() +"\", \"value\":\""+(rs.getString(2)).trim()+ "\", \"source\":\""+(rs.getString(3)).trim() +"\"}";
			}
		}
		/*for(int key: positions){
            System.out.println(key  +" :: "+ positions.get(key));
            if(positions.get(key).equalsIgnoreCase("string")){
            	stringValue = resultSet.getString(key);
            }
            if(positions.get(key).equalsIgnoreCase("integer")){
            	integerValue = resultSet.getInt(key);
            }*/
		if (res !=null)
		{
			res= "["+res+"]";
		}
		else
		{
			res= "[]";
		}
		return res;
	}


}
