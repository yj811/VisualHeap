package org.visualheap.world.layout;

import java.util.Collection;
import java.util.HashSet;

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
    private final int DUMMY = 4;
	private Collection<Value> children;

	public DummyVertex(LayoutBuilder lb, Collection<? extends Value> children) {
		super(lb);
		this.children = new HashSet<Value>();
		for(Value v : children) {
			this.children.add(v);
		}
	}

	@Override
	public void createInWorld(Game game) {
        material = game.getBlueGlowMaterial();
        geo.setMaterial(material);
        geo.setUserData("vertex", this);
        game.addCollidable(geo); 
	}

	@Override
	public Collection<Value> getChildren() {
		return children;
	}

    @Override
    public void select(Game game) {
        if (game.getSelectedVertex() == this) {
            game.removeSelectedVertex();
            game.setObjInfo("");
        } else {
            game.setSelectedVertex(this);
            game.setObjInfo(createInformation());
        }
    }

    public String createInformation() {
        return "Stack Frame\n";
    }

    @Override
    protected Geometry createGeometry() {
        Box box = new Box(DUMMY,10*DUMMY,DUMMY);
        return new Geometry("Box", box );
    }

    @Override
    public Material getMaterial() {
        return material;
    }

}
