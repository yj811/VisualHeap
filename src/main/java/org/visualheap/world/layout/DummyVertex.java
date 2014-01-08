package org.visualheap.world.layout;

import java.util.Collection;

import com.jme3.material.Material;
import org.visualheap.app.Game;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import com.sun.jdi.Value;

/**
 * A vertex with no inner ObjectReference.
 * used to add stack frame to graph as a vertex.
 * @author oliver
 *
 */
public class DummyVertex extends Vertex {

    private Material material;
    private final int DUMMY = 2;

	public DummyVertex(LayoutBuilder lb) {
		super(lb);
	}

	@Override
	public void createInWorld(Game game) {
		Box box = new Box(DUMMY,DUMMY,DUMMY);
        geo = new Geometry("Box", box );
        material = game.getGreenGlowMaterial();
        geo.setMaterial(material);
        // make obj visible on scene and collidable
        game.addCollidable(geo); 
	}

	@Override
	public Collection<Value> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void select(Game game) {
		// TODO display some stack frame related information?
	}

    @Override
    protected Geometry createGeometry() {
        Box box = new Box(DUMMY,DUMMY,DUMMY);
        return new Geometry("Box", box );
    }

    @Override
    public Material getMaterial() {
        return material;
    }

}
