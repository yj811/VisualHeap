package org.visualheap.world.layout;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

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
	 * So that JUNG displays cyclic graphs correctly, we want to maintain a 
	 * 1 - 1 mapping between ObjectReference's and ObjectReferenceVertex'es
	 * (Otherwise the cycles in ObjectReferences refering to eachother won't
	 * be reflected in the graph of ObjectReferenceVertex'es).
	 * That's what this map does.
	 */
	private static Map<ObjectReference, ObjectReferenceVertex> objRefMapping
		= new HashMap<ObjectReference, ObjectReferenceVertex>();
	

	/**
	 * builds a graph of the heap to depth specified.
	 * @param debugger a debugger object (doesn't need to be the main one...)
	 * @param initialSet ObjectReferences on the stack frame
	 * @param depth depth to search to (unimplemented)
	 * @return returns a graph layout
	 */
	public static Layout<Vertex, Edge> fromObjectReferences(Collection<ObjectReference> initialSet, int depth) {
		
		// construct the graph
		Graph<Vertex, Edge> graph = new DirectedSparseGraph<Vertex, Edge>();
		FRLayout<Vertex, Edge> layout = new FRLayout<Vertex, Edge>(graph, new Dimension(100, 100));
		Vertex dummy = new DummyVertex(layout);
		
		for(ObjectReference ref : initialSet) {
			
			ObjectReferenceVertex vert = getVertexFromObjRef(layout, ref);
			
			graph.addEdge(new Edge(layout, dummy, vert), dummy, vert);
		//	System.out.println(graph.addVertex(new ObjectReferenceVertex(ref, layout)));
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
	 * either creates a new ObjectReferenceVertex for this object reference,
	 * or returns the old one if we have already created one.
	 * @param layout layout for this Vertex to follow
	 * @param ref ObjectReference to lookup
	 * @return the ObjectReferenceVertex for this ObjectReference
	 */
	private static ObjectReferenceVertex getVertexFromObjRef(
			Layout<Vertex, Edge> layout, ObjectReference ref) {
		
		ObjectReferenceVertex vert = objRefMapping.get(ref);
		
		if(vert == null) {
			vert = new ObjectReferenceVertex(ref, layout);
			objRefMapping.put(ref, vert);
		}
			
		return vert;
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
		for(Value child : parent.getChildren()) {
			Vertex childVert = null;
			
			if(child == null) {
				// field of object was a null reference
				
				childVert = new NullReferenceVertex(layout);
				
			} else if(child instanceof ObjectReference) {
				// field was an ObjectReference
				ObjectReference childObjRef = (ObjectReference)child;
				if(depth == 0) {
					// stopped searching, mark reference as unfollowed.
					childVert = new UnfollowedReferenceVertex(childObjRef, layout);
				} else {
					
					// try to find an existing vertex for this reference
					ObjectReferenceVertex vert = objRefMapping.get(childObjRef);
					
					if(vert == null) {
						// no pre-existing vertex, make a new one
						vert = new ObjectReferenceVertex(childObjRef, layout);
						objRefMapping.put(childObjRef, vert);
						// explore successors of this vertex.
						visitChildren(graph, layout, vert, depth - 1);
					}
					childVert = vert;
				}

			}
			
			if(childVert != null) {
				graph.addEdge(new Edge(layout, parent, childVert), parent, childVert);
			}
		}
	}
	
	private LayoutBuilder() {
		
	}
	

}
