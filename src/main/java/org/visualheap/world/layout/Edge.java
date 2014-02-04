package org.visualheap.world.layout;

import java.awt.geom.Point2D;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jme3.math.Quaternion;
import com.jme3.scene.shape.Cylinder;
import org.visualheap.app.Game;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Dome;
import com.jme3.math.FastMath;

/**
 * represents an edge (i.e. a reference) between two objects
 * uses given layout to figure out where start and end coordinates are.
 * @author oliver
 *
 */
public class Edge implements ChangeListener {
    protected Vertex start;
	protected Vertex end;
    private LayoutBuilder lb;
    private Geometry lineGeo;
    private Geometry coneGeo;
    private Geometry diskGeo;
    private Line line;
    private Dome cone;
    private Cylinder disk;

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

		float radius = 2.00f; //end.getDimension() / 2;
        cone = new Dome(Vector3f.ZERO, 2, 32, radius, false); // Cone
        coneGeo = new Geometry("cone", cone);
        coneGeo.setMaterial(game.getMagentaGlowMaterial());
        game.addNonCollidable(coneGeo);

        disk = new Cylinder(32, 32, radius, 0.10f, true); // Base of cone
        diskGeo = new Geometry("disk", disk);
        diskGeo.setMaterial(game.getMagentaGlowMaterial());
        game.addNonCollidable(diskGeo);
        
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
        
        Vector3f conePos = startVec.add(startToEnd.divide(2.00f)); //startVec.add(startToEnd.normalize().mult(endDimension));
        coneGeo.setLocalTranslation(conePos);

        // This quaternion stores the cones rotation to align with reference
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(FastMath.PI / 2, startToEnd.cross(new Vector3f(0, 1, 0)));
        coneGeo.setLocalRotation(rotation);
        coneGeo.setLocalScale(1.00f, 3.00f, 1.00f);

        // TODO: Needs changing to rotate disk into correct position
        rotation.fromAngleAxis(FastMath.PI / 2, startToEnd.cross(new Vector3f(0, 1, 0)));
        diskGeo.setLocalTranslation(conePos);
        diskGeo.setLocalRotation(rotation);
    }

    void removeFromWorld(Game game) {
        game.removeNonCollidable(lineGeo);
        game.removeNonCollidable(coneGeo);
        game.removeNonCollidable(diskGeo);
    }

}
