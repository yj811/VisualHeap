package org.visualheap.world.layout;


import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.visualheap.app.Game;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Geometry;
import com.sun.jdi.Value;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * represents a vertex we will want do draw in the world 
 * i.e. an object on the heap.
 * @author oliver
 *
 */
public abstract class Vertex implements Savable, ChangeListener {
    
    protected LayoutBuilder lb;
    protected Geometry geo;
    
    Vertex(LayoutBuilder lb) {
        this.lb = lb;
        // construction of the geometry delegated to subclasses, so we can have
        // different sizes / shapes etc.
        geo = createGeometry();
        lb.registerVertex(this);
    }
    
    Vertex(LayoutBuilder lb, boolean register) {
        this.lb = lb;
        geo = createGeometry();
        if(register) {
            lb.registerVertex(this);
        }
    }

    /**
     * Construct the geometry for this Vertex.
     * @return desired geometry
     */
	protected abstract Geometry createGeometry();

    /**
	 * add this object to the game world.
	 * drawing / adding to physics space should happen here.
	 * @param game
	 */
	public abstract void createInWorld(Game game);

	/**
	 * get children (of an object reference)
	 * @return
	 */
	public abstract Collection<Value> getChildren();

	/**
	 * called when the vertex is clicked on
	 * 
	 * i.e. display some object information for a ObjectReferenceVertex
	 * @param game the game object this vertex is displayed in
	 */
	public abstract void select(Game game);
	
	/**
	 * tell the Vertex to update it's position by asking the layout
	 */
	public void updatePosition() {
        Point2D location = lb.getPosition(this); 
        geo.setLocalTranslation((float)location.getX(), 0, (float)location.getY());
    }
	
	// make this savable
	@Override
	public void read(JmeImporter arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(JmeExporter arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	// implement ChangeListener
	@Override
	public void stateChanged(ChangeEvent e) {
	    updatePosition();
	}

    void removeFromWorld(Game game) {
        game.removeCollidable(geo);
    }
	
}
