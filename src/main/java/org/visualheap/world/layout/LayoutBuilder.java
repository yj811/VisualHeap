package org.visualheap.world.layout;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.visualheap.app.Game;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * @author oliver
 *
 */
public class LayoutBuilder {
	

	/**
	 * So that JUNG displays cyclic graphs correctly, we want to maintain a 
	 * 1 - 1 mapping between ObjectReference's and ObjectReferenceVertex's
	 * (Otherwise the cycles in ObjectReferences referring to each other won't
	 * be reflected in the graph of ObjectReferenceVertex's).
	 * That's what this map does.
	 */
	private Map<ObjectReference, ObjectReferenceVertex> objRefMapping
		= new HashMap<ObjectReference, ObjectReferenceVertex>();
	
	private FRLayout<Vertex, Edge> layout;
    private Graph<Vertex, Edge> graph;
    
    // has the graph changed since we last completed the layout algo?
    private boolean layoutUpToDate = true;

	/**
	 * builds a graph of the heap to depth specified.
	 * @param debugger a debugger object (doesn't need to be the main one...)
	 * @param initialSet ObjectReferences on the stack frame
	 * @param depth depth to search to (unimplemented)
	 * @return returns a graph layout
	 */
	public static LayoutBuilder fromObjectReferences(Collection<ObjectReference> initialSet, int depth) {
		
	    return new LayoutBuilder(initialSet, depth);
	    
	}
	    
		

	/**
	 * either creates a new ObjectReferenceVertex for this object reference,
	 * or returns the old one if we have already created one.
	 * @param layout layout for this Vertex to follow
	 * @param ref ObjectReference to lookup
	 * @return the ObjectReferenceVertex for this ObjectReference
	 */
	private ObjectReferenceVertex getVertexFromObjRef(
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
	 * @param parent Vertex to add children of
	 * @param depth depth to search to
	 */
	void visitChildren(Graph<Vertex, Edge> graph, Vertex parent, int depth) {
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
					childVert = new UnfollowedReferenceVertex(childObjRef, layout, this);
				} else {
					
					// try to find an existing vertex for this reference
					ObjectReferenceVertex vert = objRefMapping.get(childObjRef);
					
					if(vert == null) {
						// no pre-existing vertex, make a new one
						vert = new ObjectReferenceVertex(childObjRef, layout);
						objRefMapping.put(childObjRef, vert);
						// explore successors of this vertex.
						visitChildren(graph, vert, depth - 1);
					}
					childVert = vert;
				}

			}
			
			if(childVert != null) {
			    layoutUpToDate = false;
				graph.addEdge(new Edge(layout, parent, childVert), parent, childVert);
			}
		}
	}
	
	private LayoutBuilder(Collection<ObjectReference> initialSet, int depth) {
        graph = new DirectedSparseGraph<Vertex, Edge>();
        layout = new FRLayout<Vertex, Edge>(graph, new Dimension(100, 100));
        
     // construct the graph
        Vertex dummy = new DummyVertex(layout);
        
        for(ObjectReference ref : initialSet) {
            
            ObjectReferenceVertex vert = getVertexFromObjRef(layout, ref);
            
            layoutUpToDate = false;
            graph.addEdge(new Edge(layout, dummy, vert), dummy, vert);
            visitChildren(graph, vert, depth - 1);
        }
        
        runLayoutAlgorithm();
        
    }

	/**
	 * Steps the layout algorithm to completion.
	 */
    public void runLayoutAlgorithm() {
        // run the layout algorithm
        
        layout.initialize();
        while(!layout.done()) {
            layout.step();
        }
        layoutUpToDate = true;
    }
    
    /**
     * Performs one step of the layout algorithm if it isn't up to date
     */
    public void stepLayoutAlgorithm() {
        
        if(!layoutUpToDate && layout.done()) {
            // layout thinks it is up to date, but it isn't
            // happens on the first step after a change.
            layout.initialize();
        }
        
        if(!layout.done()) {
            layout.step();
        }
        
        if(layout.done()) {
            layoutUpToDate = true;
        }
    }

    /**
     * Iterates through all objects in the graph, creating objects in the 3d
     * world corresponding to each vertex and edge
     */
    public void displayGraph(Game game) {
        // draw the vertices
        for (Vertex vertex : graph.getVertices()) {
            vertex.createInWorld(game);
        }
        
        for (Edge edge : graph.getEdges()) {
            edge.createInWorld(game);
        }
    }

}
