package org.visualheap.world.layout;

import java.util.Collection;

import org.visualheap.app.Game;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;

/**
 * represents a vertex we will want do draw in the world 
 * i.e. an object on the heap.
 * @author oliver
 *
 */
public abstract class Vertex {

	/**
	 * add this object to the game world.
	 * drawing / adding to physics space should happen here.
	 * @param game
	 */
	public abstract void createInWorld(Game game);

	/**
	 * get children (of an object reference)
	 * @return
	 */
	public abstract Collection<ObjectReference> getChildren();

	/**
	 * called when the vertex is clicked on
	 * 
	 * i.e. display some object information for a ObjectReferenceVertex
	 * @param game the game object this vertex is displayed in
	 */
	public abstract void select(Game game);
	
}
