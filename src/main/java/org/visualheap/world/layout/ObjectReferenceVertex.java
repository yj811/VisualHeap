package org.visualheap.world.layout;


import java.awt.geom.Point2D;
import java.util.Collection;

import org.visualheap.app.Game;
import org.visualheap.debugger.Debugger;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.ObjectReference;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * Represents an object reference that we want to display in the world.
 * Uses given Layout to find out what coordinates it is allocated in the game world.
 * @author oliver
 *
 */
public class ObjectReferenceVertex extends Vertex {

	private ObjectReference objRef;
	private Layout<Vertex, Edge> layout;

	public ObjectReferenceVertex(ObjectReference ref, Layout<Vertex, Edge> layout) {
		objRef = ref;
		this.layout = layout;
	}

	/**
	 * display a cube in the game world.
	 */
	@Override
	public void createInWorld(Game game) {
		Box box = new Box(1,1,1);
        Geometry obj = new Geometry("Box", box );
        obj.setMaterial(game.getGreenGlowMaterial());
        
        Point2D location = layout.transform(this);
        
        obj.setLocalTranslation((float)location.getX(), 0, (float)location.getY());
        // make obj visible on scene and collidable
        game.addCollidable(obj, new CollisionHandler() {

			@Override
			public void callback() {
				System.out.println("hit an object reference");
			}
        	
        });  
	}

	/**
	 * get children of object reference.
	 */
	@Override
	public Collection<ObjectReference> getChildren() {
		return Debugger.getObjectReferences(objRef);
	}

}
