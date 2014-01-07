package org.visualheap.world.layout;

import java.awt.geom.Point2D;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jme3.material.Material;
import org.visualheap.app.Game;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Line;

/**
 * represents an edge (i.e. a reference) between two objects
 * uses given layout to figure out where start and end coordinates are.
 * @author oliver
 *
 */
public class Edge implements ChangeListener {
	
	protected Vertex start;
	protected Vertex end;
    private Line line;
    private LayoutBuilder lb;
    private Geometry geo;

	public Edge(LayoutBuilder lb, Vertex start, Vertex end) {
	    this.lb = lb;
		this.start = start;
		this.end = end;
		
		lb.registerEdge(this);
	}
	
	private Vector3f threedeeify(Point2D point) {
		// we want this on the x-z plane in JME
		return new Vector3f((float)point.getX(), 0, (float)point.getY());
	}
	
	public void createInWorld(Game game) {
		Point2D startPoint = lb.getPosition(start);
		Point2D endPoint = lb.getPosition(end);
		
		line = new Line(threedeeify(startPoint), threedeeify(endPoint));
		line.setLineWidth(5);
		geo = new Geometry("line", line);
		
        geo.setMaterial(game.getMagentaGlowMaterial());
		geo.setMesh(line);
		game.addNonCollidable(geo); // non - collidable
	}

    @Override
    public void stateChanged(ChangeEvent e) {
		Point2D startPoint = lb.getPosition(start);
		Point2D endPoint = lb.getPosition(end); 
		line.updatePoints(threedeeify(startPoint), threedeeify(endPoint));
    }

    void removeFromWorld(Game game) {
        game.removeNonCollidable(geo);
    }

}
