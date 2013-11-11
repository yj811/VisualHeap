package org.visualheap.world.layout;

import java.util.Collection;
import java.util.HashSet;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;

import edu.uci.ics.jung.algorithms.layout3d.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout3d.FRLayout;
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
		FRLayout<V, E> layout = new FRLayout<V, E>(graph, 
				new BoundingSphere(new Point3d(0, 0, 0), 300));
		
		while(!layout.done()) {
			System.out.println("step");
			layout.step();
		}
		
		
		Collection<Vertex3D> points = new HashSet<Vertex3D>();
		for(V v : graph.getVertices()) {
			points.add(new Vertex3D(v, layout.transform(v)));
		}
		return points;
	}
	
	

}
