package org.visualheap.world.layout;

import java.util.Collection;

import org.visualheap.app.Game;

import com.sun.jdi.ObjectReference;

/**
 * A vertex with no inner ObjectReference.
 * used to add stack frame to graph as a vertex.
 * @author oliver
 *
 */
public class DummyVertex extends Vertex {

	@Override
	public void createInWorld(Game game) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<ObjectReference> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

}
