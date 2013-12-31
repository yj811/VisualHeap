package org.visualheap.world.layout;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.Value;
import edu.uci.ics.jung.algorithms.layout.Layout;
import org.visualheap.app.Game;

import java.util.Collection;

public class NullReferenceVertex extends Vertex {

	public NullReferenceVertex(Layout<Vertex, Edge> layout) {
		super(layout);
	}

	@Override
	public void createInWorld(Game game) {
		
        geo.setMaterial(game.getRedGlowMaterial());
        geo.setUserData("vertex", this);
        
        updatePosition();
        // make obj visible on scene and collidable
        game.addCollidable(geo);
	}

    public String createInformation() {
        String result = "Null reference";

        return result;
    }

	@Override
	public Collection<Value> getChildren() {
		// TODO Auto-generated method stub
		return null;
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
