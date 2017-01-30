//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.



package com.hp.hpl.bd4s.wfm.template;

/**
 * @author  marco casassa mont
 *
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BasicRequestHandler {
	
	@Context ServletContext context;
	
	protected Map<String, String> wfmConfig;
	
	protected static String initialise="true";
	protected static Vertica db = null;

	public  void init(ServletContext ctx) {
		System.out.println("Incoming RestAPI request");
		this.wfmConfig = loadConfigFile(System.getenv("BD4S_WFM_CONFIG"), ctx);
   
		synchronized(initialise)
		{
	    	//if (db == null)
	    	 if (initialise.equals("true"))	
		     {
	    	   db = new Vertica(this.wfmConfig);
	    	   initialise="false";	              
		     }
		}
		
	}
	
	protected String generateRandomString (final int length) {
	    Random r = new Random(); // perhaps make it a class variable so you don't make a new one every time
	    StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < length; i++) {
	        char c = (char)(r.nextInt((int)(Character.MAX_VALUE)));
	        sb.append(c);
	    }
	    return sb.toString();
	}

	protected Map<String, String> loadConfigFile(String path, ServletContext ctx) {
		
		Map<String, String> result = new HashMap<String, String>();
		Type mapType = new TypeToken<Map<String, String>>(){}.getType();
		Gson gson = new Gson();
		//String path = ctx.getRealPath(file);
		
		// read file content
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			br.close();
			String jsonConfig = sb.toString();
			
			result = gson.fromJson(jsonConfig, mapType);
		} catch (Exception e) {
			System.err.println("Error loading configuration file '"+path+"'");
			e.printStackTrace();
		}		
		
		return result;
	}

}
