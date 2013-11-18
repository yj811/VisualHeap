package org.visualheap.world.layout;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.visualheap.debugger.Debugger;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class LayoutBuilder<V> {

	private Graph<V, Edge> graph;
	
	/**
	 * builds a graph of the heap to depth specified.
	 * @param debugger a debugger object (doesn't need to be the main one...)
	 * @param initialSet ObjectReferences on the stack frame
	 * @param depth depth to search to (unimplemented)
	 * @return
	 */
	public static LayoutBuilder<ObjectReference> fromObjectReferences(Debugger debugger,
			Collection<ObjectReference> initialSet, int depth) {
		
		ObjectReference dummy = new DummyObjectReference();
		LayoutBuilder<ObjectReference> layoutBuilder = new LayoutBuilder<ObjectReference>();
		
		for(ObjectReference ref : initialSet) {
			layoutBuilder.addObjectToGraph(dummy, ref);
		}
		
		return layoutBuilder;
	}
	
	private LayoutBuilder() {
		graph = new DirectedSparseGraph<V, Edge>();
	}	
	
	private LayoutBuilder(Graph<V, Edge> graph) {
		this.graph = graph;
	}
	
	public void addObjectToGraph(V from, V to) {
		graph.addEdge(new Edge(), from, to);
	}
	
	/**
	 * compute a 2d layout for this graph.
	 * it should be possible to get JUNG to layout in 3d, but I can't make that
	 * work, yet.
	 * @return
	 */
	public Layout<V, Edge> computeLayout() {
		FRLayout<V, Edge> layout = new FRLayout<V, Edge>(graph, new Dimension(10, 10));
		
		while(!layout.done()) {
			layout.step();
		}
	
		return layout;
	}
	
	public Graph<V, Edge> getGraph() {
		return graph;
	}
	
	

}
