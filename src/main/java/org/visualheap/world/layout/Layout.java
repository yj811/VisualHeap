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

public class Layout<V extends AbstractVertex, E extends AbstractEdge> {

	private Graph<V, E> graph;
	
	public static void main(String args[]) {
		
		Graph<Vertex, Edge> g = new DirectedSparseGraph<Vertex, Edge>();
		
		Vertex a = new Vertex("a");
		Vertex b = new Vertex("b");
		Vertex c = new Vertex("c");
		
		g.addVertex(a);
		g.addVertex(b);
		g.addVertex(c);
		
		g.addEdge(new Edge(), a, b);
		g.addEdge(new Edge(), a, c);
		
		Layout<Vertex, Edge> layout = new Layout<Vertex, Edge>(g);
		Collection<Vertex3D> vs = layout.layout();
		
		for(Vertex3D v : vs) {
			System.out.println(v.toString() + " " + v.getLocation());
		}
		
	}
	
	public Layout(Graph<V, E> graph) {
		this.graph = graph;
	}
	
	public Collection<Vertex3D> layout() {
		//FRLayout<V, E> layout = new FRLayout<V, E>(graph, 
		//		new BoundingSphere(new Point3d(0, 0, 0), 300));
		FRLayout<V, E> layout = new FRLayout<V, E>(graph, new Dimension(300, 300));
		
		while(!layout.done()) {
			layout.step();
		}
		
		
		Collection<Vertex3D> points = new HashSet<Vertex3D>();
		for(V v : graph.getVertices()) {
			float x = (float) layout.getX(v);
			float y = 0;
			float z = (float) layout.getY(v);
			points.add(new Vertex3D(v, new Point3f(x, y, z)));
		}
		return points;
	}
	
	

}
