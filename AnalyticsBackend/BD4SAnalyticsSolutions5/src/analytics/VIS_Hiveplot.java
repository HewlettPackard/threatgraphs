//(c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License, Version 2.0 (the "License"); 
//you may not use this file except in compliance with the License. You may obtain a copy of the 
//License at "http://www.apache.org/licenses/LICENSE-2.0" Unless required by applicable law or agreed to in writing, 
//software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
//either express or implied.


package analytics;

/**
/**
 * @author marco casassa mont, yolanta beresna
 *
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import com.hp.hpl.bd4s.wfm.template.Vertica;
import com.hpl.hp.utils.CommonMethods;

public class VIS_Hiveplot

{
	/*
	 * Class used to have custom equality criteria
	 */

	public class DomainWrapper {
		String name;
		HashMap<String, Object> attributes = new HashMap<String, Object>();

		public DomainWrapper(String name) {
			this.name = name;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!obj.getClass().equals(VIS_Hiveplot.DomainWrapper.class))
				return false;
			DomainWrapper e = (DomainWrapper) obj;
			return e.name.equals(name);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

	public class EdgeWrapper {
		String source;
		String target;
		float weight;

		public EdgeWrapper(String source, String target, float weight) {
			this.source = source;
			this.target = target;
			this.weight = weight;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!obj.getClass().equals(VIS_Hiveplot.EdgeWrapper.class))
				return false;
			EdgeWrapper e = (EdgeWrapper) obj;
			return source.equals(e.source) && target.equals(e.target)
					&& weight == e.weight;
		}

		@Override
		public int hashCode() {
			return (source + target).hashCode();
		}
	}

	Vertica ver;
	Connection conn;
	CommonMethods commonMethods;
	private String edge_query;
	Properties filterProps;

	// Data
	HashSet<String> clients = new HashSet<String>();
	HashSet<DomainWrapper> domains = new HashSet<DomainWrapper>();
	ArrayList<EdgeWrapper> edges = new ArrayList<EdgeWrapper>();

	// Computed Statistics
	HashMap<String, Integer> clientOutDegree = new HashMap<String, Integer>();
	HashMap<String, Integer> domainInDegree = new HashMap<String, Integer>();
	HashMap<String, Integer> responsiveCount = new HashMap<String, Integer>();

	public VIS_Hiveplot(Vertica ver, String db_query, CommonMethods commonMethods, Properties filterProps) {
		this.ver = ver;
		this.edge_query = db_query;
		this.commonMethods = commonMethods;
		this.filterProps = filterProps;
	}

	public void getData() {
		System.out.println("Importing data with query:");
		System.out.println("  " + edge_query);
		System.out.println("Start of getData: " + System.currentTimeMillis());
		conn = ver.connection();
		Statement stmt;
		ResultSet rs;
		// Collect the data
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(edge_query);

			while (rs.next()) {
				String source = rs.getString(1);
				String target = rs.getString(2);
				float weight = rs.getFloat(3);
				ResultSetMetaData rsmd = rs.getMetaData();
				int numColumns = rsmd.getColumnCount();

				clients.add(source);

				DomainWrapper d = new DomainWrapper(target);
				for (int i = 4; i <= numColumns; i++) {
					d.attributes.put(rsmd.getColumnName(i), rs.getObject(i));
				}
				domains.add(d);

				edges.add(new EdgeWrapper(source, target, weight));

			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Problem with database.");
			e.printStackTrace();
		}
		System.out.println("End of getData: " + System.currentTimeMillis());

	}

	/**
	 * Compute some general statistics like degree
	 */
	public void calculateStatistics() {
		System.out.println("Start of calculateStatistics: "
				+ System.currentTimeMillis());
		clientOutDegree.clear();
		domainInDegree.clear();
		responsiveCount.clear();
		// count degrees and responsiveCount
		for (EdgeWrapper e : edges) {
			clientOutDegree.put(e.source,
					clientOutDegree.getOrDefault(e.source, 0) + 1);

			domainInDegree.put(e.target,
					domainInDegree.getOrDefault(e.target, 0) + 1);

			if (lookupDomainCat(e.target).equals("A")) {
				responsiveCount.put(e.source, responsiveCount.getOrDefault(e.source, 0) + 1);
			}
		}
		System.out.println("End of calculateStatistics: "
				+ System.currentTimeMillis());

	}

	/**
	 * Processing for DGA. This removes clients with out degree less than 20
	 */
	public void processDGA() {
		System.out
				.println("Start of processDGA: " + System.currentTimeMillis());
		calculateStatistics();

		Object[] clientsArray = clients.toArray();
		Object[] edgesArray = edges.toArray();
		
		int minOutDegree = 20;
		try {
			minOutDegree = Integer.parseInt(filterProps.getProperty("minOutDegree"));
		} catch (Exception e) {
		}
		
		System.out.println("minOutDegree: " + minOutDegree);

		// determine clients to remove and do so
		for (int i = 0; i < clientsArray.length; i++) {
			if (clientOutDegree.getOrDefault((String) clientsArray[i], 0) < minOutDegree) {
				clients.remove((String) clientsArray[i]);
			}
		}

		// remove edges from these clients
		for (int i = 0; i < edgesArray.length; i++) {
			EdgeWrapper e = (EdgeWrapper) edgesArray[i];
			if (!clients.contains(e.source)) {
				edges.remove(e);
			}
		}
		// we need to recalculate as the indegree of the domains changed when
		// removing clients
		calculateStatistics();
		// remove domains which are not queried anymore (degree = 0)
		Object[] domainsArray = domains.toArray();
		for (int i = 0; i < domainsArray.length; i++) {
			DomainWrapper d = (DomainWrapper) domainsArray[i];
			if (domainInDegree.getOrDefault(d.name, 0) == 0) {
				domains.remove(d);
			}
		}

		System.out.println("End of processDGA: " + System.currentTimeMillis());

	}
	
	/**
	 * Processing for BL.
	 */
	public void processBL() {
		System.out.println("Start of processBL: " + System.currentTimeMillis());
		calculateStatistics();

		Object[] clientsArray = clients.toArray();
		Object[] edgesArray = edges.toArray();
		
		int minOutDegree = 0;
		try {
			minOutDegree = Integer.parseInt(filterProps.getProperty("minOutDegree"));
		} catch (Exception e) {
		}
		
		System.out.println("minOutDegree: " + minOutDegree);

		// determine clients to remove and do so
		for (int i = 0; i < clientsArray.length; i++) {
			if (clientOutDegree.getOrDefault((String) clientsArray[i], 0) < minOutDegree) {
				clients.remove((String) clientsArray[i]);
			}
		}

		// remove edges from these clients
		for (int i = 0; i < edgesArray.length; i++) {
			EdgeWrapper e = (EdgeWrapper) edgesArray[i];
			if (!clients.contains(e.source)) {
				edges.remove(e);
			}
		}
		// we need to recalculate as the indegree of the domains changed when
		// removing clients
		calculateStatistics();
		// remove domains which are not queried anymore (degree = 0)
		Object[] domainsArray = domains.toArray();
		for (int i = 0; i < domainsArray.length; i++) {
			DomainWrapper d = (DomainWrapper) domainsArray[i];
			if (domainInDegree.getOrDefault(d.name, 0) == 0) {
				domains.remove(d);
			}
		}
		System.out.println("End of processBL: " + System.currentTimeMillis());

	}

	/**
	 * Lookup the category of a domain
	 */
	public String lookupDomainCat(String domain) {
		for (DomainWrapper d : domains) {
			if (d.name.equals(domain)) {
				return (String) d.attributes.get("cat");
			}
		}
		return "";
	}

	/**
	 * Builds JSON from the hiveplot data
	 * @return the hiveplot data as JSON
	 */
	public String toJSON() {
		// Build the JSON to return
		System.out.println("Start of toJSON: " + System.currentTimeMillis());
		//String json_result = "";
		StringBuilder json_result = new StringBuilder(1000);
		json_result.append("{\"clients\": [");
		String[] clientsArray = clients.toArray(new String[clients.size()]);
		for (int i = 0; i < clientsArray.length; i++) {
			json_result.append("{ \"id\": \"")
						.append(clientsArray[i])
						.append("\",\"apercentage\": ")
						.append(Math.round(((float) responsiveCount.getOrDefault(clientsArray[i], 0) / (float) clientOutDegree
								.getOrDefault(clientsArray[i], 1))*100))
						.append("}");

			if (i < clientsArray.length - 1) {
				json_result.append(",");
			}
		}
		json_result.append("],\"domains\": [");
		int i = 0;
		for (DomainWrapper d : domains) {
			json_result.append("{\"id\": \"")
						.append(d.name)
						.append("\"");
			Object[] keys = d.attributes.keySet().toArray();
			if (keys.length > 0) {
				json_result.append(",");
			}
			for (int j = 0; j < keys.length; j++) {
				json_result.append("\"" + (String) keys[j] + "\": " + "\""
						+ d.attributes.get((String) keys[j]) + "\"");
				if (j < keys.length - 1) {
					json_result.append(",");
				}
			}
			json_result.append("}");
			if (i < domains.size() - 1) {
				json_result.append(",");
			}
			i++;
		}
		json_result.append("],\"edges\": [");
		EdgeWrapper[] edgesArray = edges.toArray(new EdgeWrapper[edges.size()]);
		for (int j = 0; j < edgesArray.length; j++) {
			json_result.append("{\"source\": \"")
						.append(edgesArray[j].source)
						.append("\", \"target\": \"")
						.append(edgesArray[j].target)
						.append("\", \"weight\": ")
						.append((int)edgesArray[j].weight)
						.append("}");
			if (j < edgesArray.length - 1) {
				json_result.append(",");
			}
		}
		json_result.append("]}");
		System.out.println("End of toJSON: " + System.currentTimeMillis());
		return json_result.toString();
	}

}