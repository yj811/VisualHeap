package org.visualheap.world.layout;

import java.awt.Dimension;
import java.util.Collection;

import com.sun.jdi.ObjectReference;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * not a real class, just wraps the two static functions you see here.
 * @author oliver
 *
 */
public class LayoutBuilder {

	/**
	 * builds a graph of the heap to depth specified.
	 * @param debugger a debugger object (doesn't need to be the main one...)
	 * @param initialSet ObjectReferences on the stack frame
	 * @param depth depth to search to (unimplemented)
	 * @return
	 */
	public static Layout<Vertex, Edge> fromObjectReferences(Collection<ObjectReference> initialSet, int depth) {
		
		// construct the graph
		Vertex dummy = new DummyVertex();
		Graph<Vertex, Edge> graph = new DirectedSparseGraph<Vertex, Edge>();
		FRLayout<Vertex, Edge> layout = new FRLayout<Vertex, Edge>(graph, new Dimension(100, 100));
		
		
		for(ObjectReference ref : initialSet) {
			Vertex vert = new ObjectReferenceVertex(ref, layout);
			graph.addEdge(new Edge(layout, dummy, vert), dummy, vert);
			visitChildren(graph, layout, vert, 4);
		}
		
		// run the layout algorithm
		
		layout.initialize();
		while(!layout.done()) {
			layout.step();
		}
		
		
		
		return layout;
	}
	
	/**
	 * adds children of parent to graph so long as depth > 0
	 * @param graph graph to add children to
	 * @param layout layout (needed to construct Vertex / Edge classes)
	 * @param parent Vertex to add children of
	 * @param depth depth to search to
	 */
	private static void visitChildren(Graph<Vertex, Edge> graph, Layout<Vertex, Edge> layout, 
			Vertex parent, int depth) {
		if(depth != 0) {
			for(ObjectReference child : parent.getChildren()) {
				Vertex childVert = new ObjectReferenceVertex(child, layout);

				graph.addEdge(new Edge(layout, parent, childVert), parent, childVert);
				visitChildren(graph, layout, childVert, depth - 1);
			}
		}
	}
	
	private LayoutBuilder() {
		
	}
	

}
