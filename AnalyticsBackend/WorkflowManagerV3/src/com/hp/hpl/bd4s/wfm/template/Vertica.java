//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.

package com.hp.hpl.bd4s.wfm.template;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * @author  marco casassa mont
 *
 */

public class Vertica {
	
	private Properties connProp;

  
	public BoneCP connectionPool = null;

	public String verticaServer;
	public String verticaPort;
	public String verticaUser;
	public String verticaPassword;
	public String verticaDatabase;
	public String verticaSchema;
	public int boneCPminconn;
	public int boneCPmaxconn;
	public int boneCPpartitions;
	public int boneCPmaxConnectionAgeInSeconds;
	
	private PreparedStatement prepStmt;
	
	public Vertica(Map<String, String> config)
	{
		
		if (config.get("databaseServer") != null) {
			this.verticaServer = config.get("databaseServer");
		} else {
			System.err.println("No database server defined.");
			return;
		}
		
		if (config.get("databasePort") != null) {
			this.verticaPort = config.get("databasePort");
		} else {
			System.out.println("No database port defined. Using default 5433.");
			this.verticaPort = "5433";
		}
		
		if (config.get("databaseUser") != null ||
			config.get("databasePassword") != null) {
			this.verticaUser = config.get("databaseUser");
			this.verticaPassword = config.get("databasePassword");
		} else {
			System.out.println("No database login information defined. Using ''");
			this.verticaUser = "";
			this.verticaPassword = "";
		}
		
		if (config.get("databaseDatabase") != null) {
				this.verticaDatabase = config.get("databaseDatabase");
		} else {
			System.err.println("No database defined. Database connection failed.");
			return;
		}

		if (config.get("databaseSchema") != null) {
			this.verticaSchema = config.get("databaseSchema");
	    } else {
		   System.err.println("No database schema defined. Using public schema");
		   this.verticaSchema = "public"; 	
	   }
		
		// getting BoneCP parameters
		
		try
		{
			if (config.get("boneCPminconn") != null) 
			 {
				this.boneCPminconn = Integer.parseInt(config.get("boneCPminconn"));
				System.out.println("BoneCP Min Connection: "+ this.boneCPminconn);
		     } 
			else 
		     {
			   System.err.println("Failure in retrieving BoneCP min connections property");
			   return;	
		     }
			
			if (config.get("boneCPmaxconn") != null) 
			 {
				this.boneCPmaxconn = Integer.parseInt(config.get("boneCPmaxconn"));
				System.out.println("BoneCP Max Connection: "+ this.boneCPmaxconn);
		     } 
			else 
		     {
			   System.err.println("Failure in retrieving BoneCP max connections property");
			   return;	
		     }		
			
			if (config.get("boneCPpartitions") != null) 
			 {
				this.boneCPpartitions = Integer.parseInt(config.get("boneCPpartitions"));
				System.out.println("BoneCP Partitions: "+ this.boneCPpartitions);
		     } 
			else 
		     {
			   System.err.println("Failure in retrieving BoneCP partitions property");
			   return;	
		     }					
			
			if (config.get("boneCPmaxConnectionAgeInSeconds") != null) 
			 {
				this.boneCPmaxConnectionAgeInSeconds = Integer.parseInt(config.get("boneCPmaxConnectionAgeInSeconds"));
				System.out.println("BoneCP MaxConnectionAgeInSeconds: "+ this.boneCPmaxConnectionAgeInSeconds);
		     } 
			else 
		     {
			   System.err.println("Failure in retrieving BoneCP maxConnectionAgeInSeconds property");
			   return;	
		     }				
			
			
		}
		catch(Exception e)
		{
			System.out.println("Wrong BoneCP parameter configuration");
			return;
		}		
		
	
		
		
		
		
		
		this.connProp = new Properties(); 
		this.connProp.put("user", this.verticaUser); 
		this.connProp.put("password", this.verticaPassword); 
		
		// Load JDBC driver 
		try { Class.forName("com.vertica.jdbc.Driver"); } 
		catch (ClassNotFoundException e) 
		{ // Could not find the driver class. Likely an issue 
			// with finding the .jar file. 
			System.err.println("Could not find the JDBC driver class."); 
			e.printStackTrace(); 
			return; 
		} 
			
		// setup the connection pool
		try
		{
			System.out.println("Configuring BoneCP Connection Pool");
			BoneCPConfig Bconfig = new BoneCPConfig();
			Bconfig.setJdbcUrl("jdbc:vertica://"+this.verticaServer+":"+this.verticaPort+"/"+this.verticaDatabase); 
			Bconfig.setUsername(this.verticaUser); 
			Bconfig.setPassword(this.verticaPassword);
			Bconfig.setMinConnectionsPerPartition(this.boneCPminconn);
			Bconfig.setMaxConnectionsPerPartition(this.boneCPmaxconn);
			Bconfig.setPartitionCount(this.boneCPpartitions);
			Bconfig.setMaxConnectionAgeInSeconds(this.boneCPmaxConnectionAgeInSeconds);    // setting connection age
			
		    connectionPool = new BoneCP(Bconfig); // setup the connection pool

		}
		catch(Exception e)
		{
			System.err.println("Connection pool setting error");
			e.printStackTrace();
			
		}
		
		return;
	}
	

	
	/**
	 * Connects to the database - SUPPORT FOR MULTIPLE CONNECTIONS
	 * @return conn if successful, null if not
	 */
	
	public Connection connection()
	{
		Connection conn = null;
			try {
				System.out.println("Getting connection from BoneCP Connection Pool");
				conn = connectionPool.getConnection(); 
				System.out.println("Setting schema to: "+ this.verticaSchema);
				conn.createStatement().executeUpdate("set search_path to " + this.verticaSchema);
				return conn;
			} catch(Exception e) {
				System.err.println("Issue with Connection Pool: getting connection");
				e.printStackTrace();
				return null;
			}

	}
	
	

	
	/**
	 * Disconnects from the database - SUPPORT FOR MULTIPLE CONNECTIONS
	 * @return true if successful, null if not
	 */
	
	public boolean disconnect(Connection conn)
	{
	  try
	  {
		  if (conn != null)
		  {
			  System.out.println("Releasing connection to BoneCP Connection Pool");
			  conn.close();
			  
			  //conn = null;
			  
		  }
		  
	  }
	 catch(Exception e)
	 {
		 System.err.println("Failure during disconnection from database");
		 e.printStackTrace();
		 return false;
	 }
	 return true;
	}	
	
	
	

	
	
	public boolean setParam(int id, Object value) {
		
		try {
			this.prepStmt.setObject(id, value);
		} catch (SQLException e) {
			System.err.println("SQL Parameter Error");
			e.printStackTrace();
			return false;
		}
		return true;
	}	
	
	public boolean execute() {
		
		try {
			this.prepStmt.execute();
			return true;
		} catch (SQLException e) {
			System.err.println("SQL Query Execution Error");
			e.printStackTrace();
			return false;
		}
	}
	
	public List<HashMap<String, String>> getResult() {
		try {
			ResultSet rs = this.prepStmt.getResultSet();
			ResultSetMetaData metaData = rs.getMetaData();

			int numColumn = metaData.getColumnCount();
			List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
			if (rs != null)
			{
				while ( rs.next() ) {
					HashMap<String, String> map = new HashMap<String, String>();
					for (int i = 1; i <= numColumn; i++)
					{
						map.put(metaData.getColumnName(i), rs.getString(i));
					}
					data.add(map);
				}
			}
			return data;
		} catch (SQLException e) {
			System.err.println("SQL Result Set Error");
			e.printStackTrace();
			return null;
		}
	}
	
	public int getRowCount(ResultSet resultSet) {
	    if (resultSet == null) {
	        return 0;
	    }
	    try {
	        resultSet.last();
	        return resultSet.getRow();
	    } catch (SQLException exp) {
	        exp.printStackTrace();
	    } finally {
	        try {
	            resultSet.beforeFirst();
	        } catch (SQLException exp) {
	            exp.printStackTrace();
	        }
	    }
	    return 0;
	}
	

}
