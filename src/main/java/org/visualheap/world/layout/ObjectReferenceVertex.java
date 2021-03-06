package org.visualheap.world.layout;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.*;

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
        if (game.getTypeColorHashMap().containsKey(type)) {
            material = game.getMaterial((ColorRGBA)game.getTypeColorHashMap().get(type));
        } else {
            material = game.createNewMaterial(type);
        }

        // Set size
        int size = type.allFields().size();
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
			try {
			    
				if (f.type() instanceof PrimitiveType) {
					
					result += f.name() + ": (" + f.type().toString() + ")   " + objRef.getValue(f);
				
				} else {
					result += f.name() + ": " + objRef.getValue(f);
				}
				if (f.type() instanceof ArrayType) {
                    result += " [";
                    
                    ArrayReference array = (ArrayReference) objRef.getValue(f);
                    for (Value v : array.getValues()) {
                    	if (v != null) {
                          result += v.toString() + " , ";
                    	} else {
                    	  result +=  " null, ";
                    	}
                    }
                    result += "]";
                }
				result += "\n";
			} catch (ClassNotLoadedException e) {
				result += f.name() + ": (Unknown)  " + objRef.getValue(f) + "\n";
			}
		}
		
		return result;		
	}

    public String createMethInformation() {
        ReferenceType type = objRef.referenceType();
        String result = "Type: " + type.name() + "\n";

        for(Method m : type.allMethods()) {
            try {
				result += m.name() + "(";
				for (Type t : m.argumentTypes()) {
					int i = t.name().lastIndexOf('.');
					result += t.name().substring(i + 1) + ",";
				}
				result += ")\n";
			} catch (ClassNotLoadedException e) {
				result += m.name() + ": Arguments Unavailable\n";
			}
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
        Box box = new Box(BASE_BOX, BASE_BOX, BASE_BOX);
        return new Geometry("box", box);
    }

    @Override
    public ReferenceType getType() {
        return objRef.referenceType();
    }
}
