package org.visualheap.world.layout;

import java.awt.geom.Point2D;
import java.util.Collection;

import org.visualheap.app.Game;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.ObjectReference;

import edu.uci.ics.jung.algorithms.layout.Layout;

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
        
        Point2D location = layout.transform(this);
        
        obj.setLocalTranslation((float)location.getX(), 0, (float)location.getY());
        // make obj visible on scene and collidable
        game.addCollidable(obj); 
	}

	@Override
	public void select(Game game) {
		// TODO Auto-generated method stub

	}

}
