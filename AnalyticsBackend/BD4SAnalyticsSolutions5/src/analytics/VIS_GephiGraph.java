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
 

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterator;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

import com.hp.hpl.bd4s.wfm.template.Vertica;
import com.hpl.hp.utils.CommonMethods;


public class VIS_GephiGraph

{
	/*
	*	Class used to have custom equality criteria
	*/
	public class NodeWrapper {
		Node node;

		public NodeWrapper(Node node){
			this.node = node;
		}

		@Override
		public boolean equals(Object obj){
			if (obj == null) return false;
			if (!obj.getClass().equals(VIS_GephiGraph.NodeWrapper.class)) return false;
			return node.getNodeData().getId().equals( ((NodeWrapper) obj).node.getNodeData().getId());
		}

		@Override
		public int hashCode(){
			return node.getNodeData().getId().hashCode();
		}

	}

	public class EdgeWrapper {
		String source;
		String target;
		float weight;

		public EdgeWrapper(String source, String target, float weight){
			this.source = source;
			this.target = target;
			this.weight = weight;
		}

		@Override
		public boolean equals(Object obj){
			if (obj == null) return false;
			if (!obj.getClass().equals(VIS_GephiGraph.EdgeWrapper.class)) return false;
			EdgeWrapper e = (EdgeWrapper) obj;
			return source.equals(e.source) && target.equals(e.target) && weight == e.weight;
		}

		@Override
		public int hashCode(){
			return (source + target).hashCode();
		}
	}

	Vertica ver;
	Connection conn;
	CommonMethods commonMethods;
	Properties graphProps;
	private String edge_query;
	private ProjectController pc;
	private Workspace workspace;
	private GraphModel graphModel;


	public VIS_GephiGraph(Vertica ver, String db_query, CommonMethods commonMethods, Properties graphProps) {
		this.ver = ver;
		this.edge_query = db_query;
		this.commonMethods = commonMethods;
		this.graphProps = graphProps;

		//Init a project
		this.pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		this.workspace = pc.getCurrentWorkspace();
	}

	public void create_graph () {

		//Get graph model of current workspace
		this.graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

		System.out.println("	Importing data into a graph with a query: ");
		System.out.println("		" + edge_query);

		conn = ver.connection();
		Statement stmt;
		ResultSet rs;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(edge_query);

			HashSet<NodeWrapper> nodes = new HashSet<NodeWrapper>();
			ArrayList<EdgeWrapper> edges = new ArrayList<EdgeWrapper>();

			while(rs.next()){
				String source = rs.getString(1);
				String target = rs.getString(2);
				float weight = rs.getFloat(3);
				ResultSetMetaData rsmd = rs.getMetaData();
				int numColumns = rsmd.getColumnCount();

				Node n1 = graphModel.factory().newNode(source);
				n1.getNodeData().setLabel(source);
				n1.getAttributes().setValue("type", "Client");
				n1.getAttributes().setValue("cat", "Client");

				Node n2 = graphModel.factory().newNode(target);
				n2.getNodeData().setLabel(target);
				n2.getAttributes().setValue("type", "Domain");
				for (int i = 4; i <= numColumns; i++) {
					n2.getAttributes().setValue(rsmd.getColumnName(i), rs.getObject(i));
				}

				Edge e = graphModel.factory().newEdge(n1, n2, weight, true);

				nodes.add(new NodeWrapper(n1));
				nodes.add(new NodeWrapper(n2));
				edges.add(new EdgeWrapper(source, target, weight));
			}

			DirectedGraph graph = graphModel.getDirectedGraph();
			for(NodeWrapper n : nodes){
				graph.addNode(n.node);
			}
			for(EdgeWrapper e : edges){
				Node s = graph.getNode(e.source);
				Node t = graph.getNode(e.target);
				graph.addEdge(graphModel.factory().newEdge(s, t, e.weight, true));
			}
			commonMethods.printMessage("RESULT", "Nodes in graph: " + graph.getNodeCount() + "\nEdges in graph: " + graph.getEdgeCount());

			rs.close();
		    stmt.close();
	//		if (disconnect) {ver.disconnect(conn);};
		} catch (SQLException e) {
			System.err.println("Problem with database.");
			e.printStackTrace();
		}

	}
	
	
	public void yh_layout(){
		
		System.out.println("	Running YifanHu layout algorithm");
	
		//Run YifanHuLayout for 100 passes - The layout always takes the current visible view
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(this.graphModel);
		layout.resetPropertiesValues();
		layout.setOptimalDistance(100f);
		layout.initAlgo();
		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
		 layout.goAlgo();
		}
	}
	
	
	public void fr_layout (){
		
		System.out.println("	Running FruchtermanReingold layout algorithm");
	
		FruchtermanReingoldBuilder fruchtermanReingoldBuilder = new FruchtermanReingoldBuilder();
	    FruchtermanReingold layout = fruchtermanReingoldBuilder.buildLayout();
	    //FruchtermanReingold fr = new FruchtermanReingold(frb);
	    layout.setArea(1000f);
	    layout.setSpeed(1.0);
	    layout.setGravity(10.0);
	    layout.setGraphModel(this.graphModel);
	    for (int i = 0; i < 6000 && layout.canAlgo(); i++) {
			 layout.goAlgo();
		}
	    
	    layout.endAlgo();
    
	}
	
	public void fa2layout (){
		System.out.println("	Running ForceAtlas2 layout algorithm");
		ForceAtlas2Builder fa2Builder = new ForceAtlas2Builder();
		ForceAtlas2 layout = fa2Builder.buildLayout();
		layout.setGraphModel(this.graphModel);

		layout.initAlgo();

		layout.setScalingRatio(5.0);
		layout.setStrongGravityMode(true);
		layout.setAdjustSizes(true); //"Prevent Overlap" in GUI

		for (int i = 0; i < 6000 && layout.canAlgo(); i++) {
			 layout.goAlgo();
		}

	    layout.endAlgo();
	}

	public void modularity_partition (){
		
		System.out.println("	Perfoming partition and identifying clusters");
	
	
		DirectedGraph graph = this.graphModel.getDirectedGraph();
		
		//Get attribute model of current workspace
		
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		
		
		
		//Run modularity algorithm - community detection
		Modularity modularity = new Modularity();
			
		modularity.execute(this.graphModel, attributeModel);
		
		//Partition with ‘modularity_class’, just created by Modularity algorithm
		AttributeColumn modColumn = attributeModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
		
		PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
		
		Partition p = partitionController.buildPartition(modColumn, graph);
		System.out.println("	" + p.getPartsCount() + " partitions found");
		NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
		nodeColorTransformer.randomizeColors(p);
		partitionController.transform(p, nodeColorTransformer);
		
	
	}
	
	public void process_dga(){
		
		int minOutDegree = 20;
		try {
			minOutDegree = Integer.parseInt(graphProps.getProperty("minOutDegree"));
		} catch (Exception e) {
		}

		System.out.println("minOutDegree: " + minOutDegree);
		
		DirectedGraph graph = graphModel.getDirectedGraph();
		Node[] tempNodes = graph.getNodes().toArray();

		for (Node n : tempNodes){
			if (n.getAttributes().getValue("cat").equals("Client")){
				if (graph.getOutDegree(n) < minOutDegree){
					graph.clearEdges(n);
					graph.removeNode(n);
				}
			}
		}
		tempNodes = graph.getNodes().toArray();
		for (Node n : tempNodes){
			if (graph.getNeighbors(n).toArray().length == 0){
				graph.removeNode(n);	//remove nodes with no edges
			}
		}
		commonMethods.printMessage("RESULT", "AFTER CLEANUP: Nodes in graph: " + graph.getNodeCount() + "\nEdges in graph: " + graph.getEdgeCount());
		//Partition with 'cat' column, which is in the data
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
		Partition p = partitionController.buildPartition(attributeModel.getNodeTable().getColumn("cs1"), graph);
		NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
		nodeColorTransformer.randomizeColors(p);
		partitionController.transform(p, nodeColorTransformer);


		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.OUTDEGREE_RANKING);
		AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(20);
		sizeTransformer.setMaxSize(60);
		rankingController.transform(degreeRanking,sizeTransformer);
	}
	
	public void process_dgagen(){
		int minOutDegree = 20;
		try {
			minOutDegree = Integer.parseInt(graphProps.getProperty("minOutDegree"));
		} catch (Exception e) {
		}

		System.out.println("minOutDegree: " + minOutDegree);
		
		DirectedGraph graph = graphModel.getDirectedGraph();
		Node[] tempNodes = graph.getNodes().toArray();

		for (Node n : tempNodes){
			if (n.getAttributes().getValue("cat").equals("Client")){
				if (graph.getOutDegree(n) < minOutDegree){
					graph.clearEdges(n);
					graph.removeNode(n);
				} else {
					Node[] neighbors = graph.getNeighbors(n).toArray();
					int nx = 0;
					for (int i = 0; i < neighbors.length; i++) {
						if (neighbors[i].getAttributes().getValue("cat").equals("NXDOMAIN")) {
							nx++;
						}
					}
					if (nx < 20) {
						graph.clearEdges(n);
						graph.removeNode(n);
					}
				}
			}
		}
		tempNodes = graph.getNodes().toArray();
		for (Node n : tempNodes){
			if (graph.getNeighbors(n).toArray().length == 0){
				graph.removeNode(n);	//remove nodes with no edges
			}
		}
		commonMethods.printMessage("RESULT", "AFTER CLEANUP: Nodes in graph: " + graph.getNodeCount() + "\nEdges in graph: " + graph.getEdgeCount());
		//Partition with 'cat' column, which is in the data
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
		Partition p = partitionController.buildPartition(attributeModel.getNodeTable().getColumn("cs1"), graph);
		NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
		nodeColorTransformer.randomizeColors(p);
		partitionController.transform(p, nodeColorTransformer);


		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.OUTDEGREE_RANKING);
		AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(20);
		sizeTransformer.setMaxSize(60);
		rankingController.transform(degreeRanking,sizeTransformer);
	}

	public void create_graph_file (String filename){
		
		System.out.println("	Saving to file " + filename);
	
		//Export full graph
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
		 ec.exportFile(new File(filename));
		} catch (IOException ex) {
		 ex.printStackTrace();
		return;
		}
	
	}

}