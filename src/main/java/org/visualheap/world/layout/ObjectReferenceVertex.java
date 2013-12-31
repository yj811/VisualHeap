package org.visualheap.world.layout;


import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import edu.uci.ics.jung.algorithms.layout.Layout;
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
	
	public ObjectReferenceVertex(ObjectReference ref, Layout<Vertex, Edge> layout) {
	    super(layout);
	    objRef = ref;
	}

	/**
	 * display a cube in the game world.
	 */
	@Override
	public void createInWorld(Game game) {
        ReferenceType type = objRef.referenceType();
        Material material;

        // Set colour
        if(game.getMaterialHashMap().containsKey(type)) {
            material = (Material)game.getMaterialHashMap().get(type);
        } else {
            //material = game.getGreenGlowMaterial();
            material = game.createNewMaterial(type);
        }

        // Set size
        int size = 1;
        if (type.fieldByName("size") != null) {
            Field f = type.fieldByName("size");
            size = Integer.parseInt(objRef.getValue(f).toString());
            size = (size == 0) ? 1 : size;
        }
        // Results in null pointer exception currently
        //result += "Size: " + ObjectSizeFetcher.getObjectSize(objRef);

        geo.setLocalScale(size);
        geo.setMaterial(material);
        geo.setUserData("vertex", this);
        
        updatePosition();
        // make obj visible on scene and collidable
        game.addCollidable(geo); 
        
	}
	
	public String createInformation() {
        ReferenceType refType = objRef.referenceType();

		String result = "Type: " + refType.name() + "\n";

        // Results in null pointer exception currently
        if(refType.name().equals("java.util.LinkedList")) {
            Field f = refType.fieldByName("size");
            result += "Found size: " + objRef.getValue(f);
        } else {
            System.out.println("Not linked list: "+refType.name());
        }
        //result += "Size: " + ObjectSizeFetcher.getObjectSize(objRef);

		for(Field f : refType.allFields()) {
			result += f.name() + "\t" + objRef.getValue(f) + "\n";
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
		game.setObjInfo(createInformation());
	}

    @Override
    protected Geometry createGeometry() {
        Box box = new Box(1,1,1);
        return new Geometry("box", box );
    }
	
}
