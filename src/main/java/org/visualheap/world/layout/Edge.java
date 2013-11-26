package org.visualheap.world.layout;

import java.awt.geom.Point2D;

import org.visualheap.app.Game;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * represents an edge (i.e. a reference) between two objects
 * uses given layout to figure out where start and end coordinates are.
 * @author oliver
 *
 */
public class Edge {
	
	protected Layout<Vertex, Edge> layout;
	protected Vertex start;
	protected Vertex end;

	public Edge(Layout<Vertex, Edge> layout, Vertex start, Vertex end) {
		this.layout = layout;
		this.start = start;
		this.end = end;
	}
	
	private Vector3f threedeeify(Point2D point) {
		// we want this on the x-z plane in JME
		return new Vector3f((float)point.getX(), 0, (float)point.getY());
	}
	
	public void createInWorld(Game game) {
		
		Point2D startPoint = layout.transform(start);
		Point2D endPoint = layout.transform(end);		
		
		Line line = new Line(threedeeify(startPoint), threedeeify(endPoint));
		line.setLineWidth(5);
		Geometry g = new Geometry("line", line);
		
        g.setMaterial(game.getMagentaGlowMaterial());
		g.setMesh(line);
		game.addNonCollidable(g); // non - collidable
	}

}
