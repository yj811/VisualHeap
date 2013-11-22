package org.visualheap.world.layout;

import java.util.Collection;

import org.visualheap.app.Game;

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

}