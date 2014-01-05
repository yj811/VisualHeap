package org.visualheap.world.layout;

import java.awt.geom.Point2D;
import java.util.Collection;

import org.visualheap.app.Game;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import edu.uci.ics.jung.algorithms.layout.Layout;

import com.sun.jdi.Value;

/**
 * A vertex with no inner ObjectReference.
 * used to add stack frame to graph as a vertex.
 * @author oliver
 *
 */
public class DummyVertex extends Vertex {

	public DummyVertex(LayoutBuilder lb) {
		super(lb);
	}

	@Override
	public void createInWorld(Game game) {
		Box box = new Box(1,1,1);
        Geometry obj = new Geometry("Box", box );
        obj.setMaterial(game.getMagentaGlowMaterial());
        // make obj visible on scene and collidable
        game.addCollidable(obj); 
	}

	@Override
	public Collection<Value> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void select(Game game) {
		// TODO display some stack frame related information?
	}

    @Override
    protected Geometry createGeometry() {
        Box box = new Box(1,1,1);
        return new Geometry("Box", box );
    }

}
