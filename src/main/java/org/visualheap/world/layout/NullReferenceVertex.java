package org.visualheap.world.layout;

import java.awt.geom.Point2D;
import java.util.Collection;

import org.visualheap.app.Game;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.Value;

import edu.uci.ics.jung.algorithms.layout.Layout;

public class NullReferenceVertex extends Vertex {

	public NullReferenceVertex(Layout<Vertex, Edge> layout) {
		super(layout);
	}

	@Override
	public void createInWorld(Game game) {
		
        geo.setMaterial(game.getRedGlowMaterial());
        geo.setUserData("vertex", this);
        
        updatePosition();
        // make obj visible on scene and collidable
        game.addCollidable(geo); 
        
	}

	@Override
	public Collection<Value> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void select(Game game) {
		// TODO Auto-generated method stub

	}

    @Override
    protected Geometry createGeometry() {
        Box box = new Box(1,1,1);
        return new Geometry("box", box );
    }

}
