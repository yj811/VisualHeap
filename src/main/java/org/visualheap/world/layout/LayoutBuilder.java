package org.visualheap.world.layout;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashSet;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class LayoutBuilder<V> {

	private Graph<V, Edge> graph;
	
	public LayoutBuilder() {
		graph = new DirectedSparseGraph<V, Edge>();
	}	
	
	public LayoutBuilder(Graph<V, Edge> graph) {
		this.graph = graph;
	}
	
	public void addObjectToGraph(V from, V to) {
		graph.addEdge(new Edge(), from, to);
	}
	
	public Collection<Vertex3D<V>> computeLayout() {
		FRLayout<V, Edge> layout = new FRLayout<V, Edge>(graph, new Dimension(300, 300));
		
		while(!layout.done()) {
			layout.step();
		}
		
		Collection<Vertex3D<V>> points = new HashSet<Vertex3D<V>>();
		for(V v : graph.getVertices()) {
			float x = (float) layout.getX(v);
			float y = 0;
			float z = (float) layout.getY(v);
			points.add(new Vertex3D<V>(v, new Point3f(x, y, z)));
		}
		return points;
	}
	
	

}
