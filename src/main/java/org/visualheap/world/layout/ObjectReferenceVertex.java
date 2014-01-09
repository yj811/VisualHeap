package org.visualheap.world.layout;


import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import org.visualheap.app.Game;

import java.util.Collection;
import java.util.Vector;

/**
 * Represents an object reference that we want to display in the world.
 * Uses given Layout to find out what coordinates it is allocated in the game world.
 * @author oliver
 *
 */
public class ObjectReferenceVertex extends Vertex {

	protected ObjectReference objRef;
    protected Material material;
	
	public ObjectReferenceVertex(ObjectReference ref, LayoutBuilder lb) {
	    super(lb, false);
	    objRef = ref;
	    lb.registerVertex(this);
	}

	/**
	 * display a cube in the game world.
	 */
	@Override
	public void createInWorld(Game game) {
        ReferenceType type = objRef.referenceType();

        // Set colour
        if (type.isStatic()) {
            material = game.getGreenGlowMaterial();
        } else if (game.getTypeColorHashMap().containsKey(type)) {
            material = game.getMaterial((ColorRGBA)game.getTypeColorHashMap().get(type));
        } else {
            material = game.createNewMaterial(type);
        }

        // Set size
        int size = type.allFields().size();
        System.out.println("Number of fields for " + type.name() + " : " + size);
        size = (size == 0) ? BASE_BOX : size;

        geo.setLocalScale(1, size * 2, 1);
        geo.setMaterial(material);
        geo.setUserData("vertex", this);
        
        updatePosition();
        // make obj visible on scene and collidable
        game.addCollidable(geo);
	}
	
	public String createInformation() {
        ReferenceType type = objRef.referenceType();
		String result = "Type: " + type.name() + "\n";

		for(Field f : type.allFields()) {
			result += f.name() + ":   " + objRef.getValue(f) + "\n";
		}
		
		return result;		
	}

	/**
	 * get children of object reference.
	 */
	@Override
	public Collection<Value> getChildren() {
		Collection<Value> values = new Vector<Value>();
		
		for(Field f : objRef.referenceType().fields()) {
			values.add(objRef.getValue(f));
		}
		
		return values;
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

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    protected Geometry createGeometry() {
        Box box = new Box(BASE_BOX, BASE_BOX, BASE_BOX);
        return new Geometry("box", box);
    }

    @Override
    public ReferenceType getType() {
        return objRef.referenceType();
    }
}
