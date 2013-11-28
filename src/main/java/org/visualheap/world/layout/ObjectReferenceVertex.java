package org.visualheap.world.layout;


import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import org.visualheap.app.Game;
import org.visualheap.debugger.Debugger;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * Represents an object reference that we want to display in the world.
 * Uses given Layout to find out what coordinates it is allocated in the game world.
 * @author oliver
 *
 */
public class ObjectReferenceVertex extends Vertex implements Savable {

	protected ObjectReference objRef;
	protected Layout<Vertex, Edge> layout;

	public ObjectReferenceVertex(ObjectReference ref, Layout<Vertex, Edge> layout) {
		objRef = ref;
		this.layout = layout;
	}

	/**
	 * display a cube in the game world.
	 */
	@Override
	public void createInWorld(Game game) {
		
		Box box = new Box(1,1,1);
		
		
		
        Geometry obj = new Geometry("box", box );
        obj.setMaterial(game.getGreenGlowMaterial());
        obj.setUserData("vertex", this);
        
        Point2D location = layout.transform(this);
        
        obj.setLocalTranslation((float)location.getX(), 0, (float)location.getY());
        // make obj visible on scene and collidable
        game.addCollidable(obj); 
        
	}
	
	public String createInformation() {
		String result = "Type: " + objRef.type().name() + "\n";
		
		for(Field f : objRef.referenceType().allFields()) {	
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
	public void read(JmeImporter arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(JmeExporter arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void select(Game game) {
		game.setObjInfo(createInformation());
	}
	
}
