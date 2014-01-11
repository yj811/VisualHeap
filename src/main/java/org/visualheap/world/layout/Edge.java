package org.visualheap.world.layout;

import java.awt.geom.Point2D;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jme3.material.Material;
import org.visualheap.app.Game;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;

/**
 * represents an edge (i.e. a reference) between two objects
 * uses given layout to figure out where start and end coordinates are.
 * @author oliver
 *
 */
public class Edge implements ChangeListener {
	
	private static final int SPHERE_DISTANCE_FROM_ENDPOINT = 5;
    private static final int SPHERE_RADIUS = 3;
    protected Vertex start;
	protected Vertex end;
    private Line line;
    private LayoutBuilder lb;
    private Geometry lineGeo;
    private Geometry ballGeo;
    private Sphere ball;

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
		
		Vector3f startVec = threedeeify(endPoint);
        Vector3f endVec = threedeeify(startPoint);
		
		line = new Line(endVec, startVec);
		line.setLineWidth(5);
		lineGeo = new Geometry("line", line);
		
        lineGeo.setMaterial(game.getMagentaGlowMaterial());
		lineGeo.setMesh(line);
		game.addNonCollidable(lineGeo); // non - collidable

		float radius = end.getDimension() / 2;
        ball = new Sphere(32, 32, radius);
        ballGeo = new Geometry("ball", ball);
        ballGeo.setMaterial(game.getMagentaGlowMaterial());
        
        game.addNonCollidable(ballGeo);
        
        computePositions();

	}

    @Override
    public void stateChanged(ChangeEvent e) {
		computePositions();
    }

    private void computePositions() {
        Point2D startPoint = lb.getPosition(start);
		Point2D endPoint = lb.getPosition(end); 
		Vector3f startVec = threedeeify(endPoint);
        Vector3f endVec = threedeeify(startPoint);

		line.updatePoints(startVec, endVec);

        Vector3f startToEnd = endVec.subtract(startVec); 
        int endDimension = end.getDimension();
        // not updating ball size, building a new sphere damages performance.
        
        Vector3f ballPos = startVec.add(startToEnd.normalize().mult(endDimension));
        ballGeo.setLocalTranslation(ballPos);
    }

    void removeFromWorld(Game game) {
        game.removeNonCollidable(lineGeo);
        game.removeNonCollidable(ballGeo);
    }

}
