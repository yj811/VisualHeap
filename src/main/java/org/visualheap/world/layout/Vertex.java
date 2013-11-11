package org.visualheap.world.layout;

class Vertex extends AbstractVertex {
	
	private String name;
	
	public Vertex(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}