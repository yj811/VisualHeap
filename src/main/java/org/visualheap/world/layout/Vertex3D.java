package org.visualheap.world.layout;

import javax.vecmath.Point3f;

public class Vertex3D<V> {
	
	private V innerVertex;
	private Point3f location;
	
	public Vertex3D(V innerVertex, Point3f location) {
		this.innerVertex = innerVertex;
		this.location = location;
	}

	public Point3f getLocation() {
		return location;
	}
	
	public V getInnerVertex() {
		return innerVertex;
	}

}
