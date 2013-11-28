package org.visualheap.world.layout;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.visualheap.app.Game;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.ObjectReference;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;

public class UnfollowedReferenceVertex extends ObjectReferenceVertex {

	public UnfollowedReferenceVertex(ObjectReference ref, 
			Layout<Vertex, Edge> layout) {
		super(ref, layout);
	}

	@Override
	public void createInWorld(Game game) {
		Box box = new Box(1,1,1);
        Geometry obj = new Geometry("Box", box );
        obj.setMaterial(game.getYellowGlowMaterial());
        obj.setUserData("vertex", this);
        
        Point2D location = layout.transform(this);
        
        obj.setLocalTranslation((float)location.getX(), 0, (float)location.getY());
        // make obj visible on scene and collidable
        game.addCollidable(obj); 
	}

	@Override
	public void select(Game game) {
		
		System.out.println("click unfollowed reference");
		
		Graph<Vertex, Edge> graph = layout.getGraph();
		
		// replace this vertex in the graph with an ObjectRefernceVertex
		ObjectReferenceVertex newVert = new ObjectReferenceVertex(objRef, layout);
		for(Edge e : graph.getInEdges(this)) {
			Vertex start = e.start;
			graph.addEdge(new Edge(layout, start, newVert), start, newVert);
		}
		graph.removeVertex(this);
		
		// add children to layout.
		LayoutBuilder.visitChildren(graph, layout, newVert, 0);
		
		game.rebuildWorld();
		
	}

}
