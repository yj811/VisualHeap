package org.visualheap.world.layout;

import javax.vecmath.Point3f;

public class Vertex3D extends AbstractVertex {
	
	private AbstractVertex innerVertex;
	private Point3f location;
	
	public Vertex3D(AbstractVertex innerVertex, Point3f location) {
		this.innerVertex = innerVertex;
		this.location = location;
	}

	public Point3f getLocation() {
		return location;
	}

}
