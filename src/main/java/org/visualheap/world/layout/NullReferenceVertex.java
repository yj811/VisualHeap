package org.visualheap.world.layout;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.sun.jdi.Value;
import edu.uci.ics.jung.algorithms.layout.Layout;
import org.visualheap.app.Game;

import java.util.Collection;

public class NullReferenceVertex extends Vertex {

    private Material material;

	public NullReferenceVertex(LayoutBuilder lb) {
		super(lb);
	}

	@Override
	public void createInWorld(Game game) {
        material = game.getRedGlowMaterial();
        geo.setMaterial(material);
        geo.setUserData("vertex", this);
        
        updatePosition();
        // make obj visible on scene and collidable
        game.addCollidable(geo);
	}

    public String createInformation() {
        String result = "Null reference";
        return result;
    }

    public String createMethInformation() {
        return "";
    }

	@Override
	public Collection<Value> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void select(Game game) {
        if (game.getSelectedVertex() == this) {
            game.removeSelectedVertex();
            game.setObjInfo("");
            game.setObjMethInfo("");
        } else {
            game.setSelectedVertex(this);
            game.setObjInfo(createInformation());
            game.setObjMethInfo(createMethInformation());
        }
	}

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    protected Geometry createGeometry() {
        Sphere sphere = new Sphere(32, 32, 3f);
        return new Geometry("sphere", sphere);
    }

}
