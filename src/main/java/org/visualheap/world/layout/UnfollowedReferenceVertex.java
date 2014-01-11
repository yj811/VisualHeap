package org.visualheap.world.layout;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.visualheap.app.Game;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.ObjectReference;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;

public class UnfollowedReferenceVertex extends ObjectReferenceVertex {
    private final int MINI_BOX = 3;

    public UnfollowedReferenceVertex(ObjectReference ref, LayoutBuilder lb) {
		super(ref, lb);
	}

	@Override
	public void createInWorld(Game game) {
        geo.setMaterial(game.getYellowGlowMaterial());
        //geo.setLocalScale(MINI_BOX);
        geo.setUserData("vertex", this);
        
        updatePosition();
        // make obj visible on scene and collidable
        game.addCollidable(geo); 
	}

	@Override
	public void select(Game game) {
		System.out.println("click unfollowed reference");
		
		// replace this vertex in the graph with an ObjectReferenceVertex
		ObjectReferenceVertex newVert = new ObjectReferenceVertex(objRef, lb);
        newVert.select(game);

		lb.replace(this, newVert);
		/*
		*/
		
		Vector3f myPos = geo.getLocalTranslation();
		lb.setPosition(newVert, myPos.x, myPos.y);
		
		// add children to layout.
		lb.visitChildren(newVert, 0);
	}

    @Override
    protected Geometry createGeometry() {
        Box box = new Box(MINI_BOX,MINI_BOX,MINI_BOX);
        return new Geometry("Box", box );
    }
    
    @Override
    public int getDimension() {
        return MINI_BOX;
    }
}
