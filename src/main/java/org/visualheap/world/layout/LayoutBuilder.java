package org.visualheap.world.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.visualheap.app.Game;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;

/**
 * @author oliver
 *
 */
public class LayoutBuilder {
    private final int BASE_SIZE = 100;

	/**
	 * So that JUNG displays cyclic graphs correctly, we want to maintain a 
	 * 1 - 1 mapping between ObjectReference's and ObjectReferenceVertex's
	 * (Otherwise the cycles in ObjectReferences referring to each other won't
	 * be reflected in the graph of ObjectReferenceVertex's).
	 * That's what this map does.
	 */
	private Map<ObjectReference, ObjectReferenceVertex> objRefMapping
		= new HashMap<ObjectReference, ObjectReferenceVertex>();
	
	private ObservableCachingLayout<Vertex, Edge> layout;
    private ISOMLayout<Vertex, Edge> isomLayout;
    private Graph<Vertex, Edge> graph;
    
    // has the graph changed since we last completed the layout algorithm?
    private boolean layoutUpToDate = true;
    private Game game;

	/**
	 * builds a graph of the heap to depth specified.
	 * @param initialSet ObjectReferences on the stack frame
     * @param game current game object
	 * @param depth depth to search to (unimplemented)
	 * @return returns a graph layout
	 */
	public static LayoutBuilder fromObjectReferences(Collection<ObjectReference> initialSet,
                                                     Game game, int depth) {
	    return new LayoutBuilder(initialSet, game, depth);
	}

	/**
	 * either creates a new ObjectReferenceVertex for this object reference,
	 * or returns the old one if we have already created one.
	 * @param layout layout for this Vertex to follow
	 * @param ref ObjectReference to lookup
	 * @return the ObjectReferenceVertex for this ObjectReference
	 */
	private ObjectReferenceVertex getVertexFromObjRef(Layout<Vertex, Edge> layout,
                                                      ObjectReference ref) {
		ObjectReferenceVertex vert = objRefMapping.get(ref);
		
		if(vert == null) {
			vert = new ObjectReferenceVertex(ref, this);
			objRefMapping.put(ref, vert);
		}
		
		return vert;
	}
	
	/**
	 * adds children of parent to graph so long as depth > 0
	 * @param parent Vertex to add children of
	 * @param depth depth to search to
	 */
	void visitChildren(Vertex parent, int depth) {
		for(Value child : parent.getChildren()) {
			Vertex childVert = null;
			
			if(child == null) {
				// field of object was a null reference
				childVert = new NullReferenceVertex(this);
			} else if(child instanceof ObjectReference) {
				// field was an ObjectReference
				ObjectReference childObjRef = (ObjectReference)child;
				if(depth == 0) {
					// stopped searching, mark reference as unfollowed.
					childVert = new UnfollowedReferenceVertex(childObjRef, this);
				} else {
					// try to find an existing vertex for this reference
					ObjectReferenceVertex vert = objRefMapping.get(childObjRef);
					
					if(vert == null) {
						// no pre-existing vertex, make a new one
						vert = new ObjectReferenceVertex(childObjRef, this);
						objRefMapping.put(childObjRef, vert);
						// explore successors of this vertex.
						visitChildren(vert, depth - 1);
					}
					childVert = vert;
				}
			}
			
			if(childVert != null) {
			    layoutUpToDate = false;
				graph.addEdge(new Edge(this, parent, childVert), parent, childVert);
                runLayoutAlgorithm();
			}
		}
	}
	
	private LayoutBuilder(Collection<ObjectReference> initialSet, Game game, int depth) {
        graph = new DirectedSparseGraph<Vertex, Edge>();
        isomLayout = new ISOMLayout<Vertex, Edge>(graph);
        isomLayout.setSize(new Dimension(BASE_SIZE, BASE_SIZE));
        layout = new ObservableCachingLayout<Vertex, Edge>(isomLayout);
        this.game = game;
        
        // construct the graph
        Vertex dummy = new DummyVertex(this);
        
        for(ObjectReference ref : initialSet) {
            ObjectReferenceVertex vert = getVertexFromObjRef(layout, ref);
            layoutUpToDate = false;
            graph.addEdge(new Edge(this, dummy, vert), dummy, vert);
            visitChildren(vert, depth - 1);
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
        layout.clear(); // clear layout cache
        layout.fireStateChanged();
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
            layout.clear(); // clear cache
            layout.fireStateChanged();
        }
        
        if(layout.done()) {
            layoutUpToDate = true;
        }
    }

    void setPosition(Vertex v, double x, double y) {
        layout.setLocation(v, new Point2D.Double(x, y));
    }
    
    Point2D getPosition(Vertex v) {
        return layout.transform(v);
    }
    
    void registerVertex(Vertex v) {
        layout.addChangeListener(v);
        checkLayoutDimension();
        v.createInWorld(game);
    }

    void registerEdge(Edge edge) {
        layout.addChangeListener(edge);
        edge.createInWorld(game);
    }

    void replace(Vertex oldVert, Vertex newVert) {
		for(Edge e : graph.getInEdges(oldVert)) {
			Vertex start = e.start;
			graph.addEdge(new Edge(this, start, newVert), start, newVert);
			e.removeFromWorld(game);
		}
		graph.removeVertex(oldVert);
		layout.removeChangeListener(oldVert);
		oldVert.removeFromWorld(game);
    }

    void checkLayoutDimension() {
        int vertexCount = layout.getGraph().getVertexCount();
        double scale = Math.sqrt(vertexCount);
        if (!(scale < 1)) {
            int newDim = (int) Math.round(BASE_SIZE * scale);
            isomLayout.setSize(new Dimension(newDim, newDim));
        }
    }
}
