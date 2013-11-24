package org.visualheap.app;

import org.visualheap.world.layout.CollisionHandler;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;

/**
 * passes collision events on to the CollisionHandler of the objects involved (if set)
 * @author oliver
 *
 */
final class CollisionAdapter implements
		PhysicsCollisionListener {
	@Override
	public void collision(PhysicsCollisionEvent event) {
		callCollisionHandler(event.getObjectA().getUserObject());
		callCollisionHandler(event.getObjectB().getUserObject());
	}

	private void callCollisionHandler(Object userObject) {
		if(userObject instanceof CollisionHandler) {
			CollisionHandler handler = (CollisionHandler)userObject;
			handler.callback();
		}
	}
}