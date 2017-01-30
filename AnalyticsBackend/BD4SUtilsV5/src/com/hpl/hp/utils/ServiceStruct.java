//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.


package com.hpl.hp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.*;

/**
 * Class to store and transfer data about a service
 * @author marco casassa mont
 *
 */
public class ServiceStruct {
	
	private HashMap<String, Object> config;
	
	public ServiceStruct() {
		this.config = new HashMap<String, Object>();
	}
	
	/**
	 * Adds a key/value pair to the config
	 * @param key key of the config value
	 * @param value value of the config key
	 */
	public void add(String key, Object value) {
		this.config.put(key, value);
	}
	
	/**
	 * @param key Key of the requested value
	 * @return the value of the config key, null if is does not exist
	 */
	public String getString(String key) {
		if (this.config.containsKey(key))
			return this.config.get(key).toString();
		else
			return null;
	}
	
	/**
	 * @param key Key of the requested value
	 * @return an ArrayList of the JSON Array
	 */
	public ArrayList<String> getStringList(String key) {
		ArrayList<String> result = new ArrayList<String>();
		if (this.config.containsKey(key)) {
			try {
				JSONArray array = new JSONObject("{array: "+this.getString(key)+"}")
														.getJSONArray("array");
				
				for (int x = 0; x < array.length(); x++) {
					result.add(array.getString(x));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * Returns the current configuration
	 * @return the current configuration
	 */
	public HashMap<String, Object> getConfig() {
		return config;
	}
	
	/**
	 * @param key Key of the requested value
	 * @return the value of the config key, null if is does not exist
	 */
	public Integer getInt(String key) {
		if (this.config.containsKey(key))
			return Integer.parseInt(this.config.get(key).toString());
		else
			return null;
	}
	
	/**
	 * @param key Key of the requested value
	 * @return the value of the config key, null if is does not exist
	 */
	public Date getDate(String key) {
		if (this.config.containsKey(key))
			try {
				return new SimpleDateFormat("yyyyMMdd'T'HHmmss")
					.parse(this.config.get(key).toString());
			} catch (ParseException e) {
				System.err.println("Date could not be parsed from String");
				e.printStackTrace();
				return null;
			}
		else
			return null;
	}
	
	/**
	 * Transforms the current configuration into JSON format
	 * @return A JSON String representing the current configuration
	 */
	public String toJSONString()
	{
		return new JSONObject(this.config).toString();
	}
	
	/**
	 * Transforms the current coniguration into a JSON Object
	 * @return a JSON Object representing the current configuration
	 */
	public JSONObject toJSONObject()
	{
		return new JSONObject(this.config);
	}
}
