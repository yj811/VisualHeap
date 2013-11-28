package org.visualheap.world.layout;

import java.awt.geom.Point2D;
import java.util.Collection;

import org.visualheap.app.Game;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.Value;

import edu.uci.ics.jung.algorithms.layout.Layout;

public class NullReferenceVertex extends Vertex {
	
	private Layout<Vertex, Edge> layout;

	public NullReferenceVertex(Layout<Vertex, Edge> layout) {
		this.layout = layout;
	}

	@Override
	public void createInWorld(Game game) {
		
		Box box = new Box(1,1,1);
		
		
		
        Geometry obj = new Geometry("box", box );
        obj.setMaterial(game.getRedGlowMaterial());
        obj.setUserData("vertex", this);
        
        Point2D location = layout.transform(this);
        
        obj.setLocalTranslation((float)location.getX(), 0, (float)location.getY());
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
		// TODO Auto-generated method stub

	}

}
