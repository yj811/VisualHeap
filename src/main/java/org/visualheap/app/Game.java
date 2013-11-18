package org.visualheap.app;


import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.vecmath.Point3f;

import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;
import org.visualheap.world.layout.LayoutBuilder;
import org.visualheap.world.layout.Vertex3D;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;


public class Game extends SimpleApplication {

	private static final String CLASSPATH = "build/classes/test";
	private static final String ARRAYCLASS = "debugger.testprogs.Array";
	private static final String CYCLICREFERENCE = "debugger.testprogs.CyclicReference";
	Geometry obj;
	private Boolean running = true;
	private Collection<Vertex3D<ObjectReference>> layout;

	// start a new game.
	public static void main(String[] args) {
		final Game game = new Game();

		DebugListener listener = new NullListener() {
			
			
			@Override
			public void onBreakpoint(final StackFrame sf) {
				Executor exec =  Executors.newCachedThreadPool();
				exec.execute(new Runnable() {

					@Override
					public void run() {
						
						Collection<ObjectReference> initialSet = getObjectReferencesFromStackFrame(sf);
						
						LayoutBuilder<ObjectReference> layoutBuilder 
							= LayoutBuilder.fromObjectReferences(new Debugger(new NullListener()), initialSet, 10);
						Collection<Vertex3D<ObjectReference>> layout = layoutBuilder.computeLayout();
						
						game.setLayout(layout);
						game.start();
					}
					
				});
			}
			
		};
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, 15, listener);
		
	
    }
	
	protected void setLayout(Collection<Vertex3D<ObjectReference>> layout) {
		this.layout = layout;
		System.out.println("layout has " + layout.size() + " objects");
	}

	// load objects before game starts.
	@Override
	public void simpleInitApp() {
		
		for(Vertex3D<ObjectReference> vertex : layout) {
			Point3f location = vertex.getLocation();
			drawBox(location.x, location.y, location.z);
		}
		
        keyMapping();
	}

	private void drawBox(float x, float y, float z) {
		Box box = new Box(1,1,1);
        obj = new Geometry("Box", box );
        Material mat_brick = new Material( 
            assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap", 
            assetManager.loadTexture("Texture/images.jpeg"));
        obj.setMaterial(mat_brick);
        obj.setLocalTranslation(x, y, z);
        // make obj visible on scene.
        rootNode.attachChild(obj);
	}
	
	// initiate actions and update game state.
    @Override
    public void simpleUpdate(float tpf) {

    }
    
    // initiate key triggers.
    private void keyMapping() {
    	inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));
    	inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_A));
    	
    	inputManager.addListener(actionListener, "Pause");
    	inputManager.addListener(analogListener, "Rotate");
    }
    
    private ActionListener actionListener = new ActionListener() {

		@Override
		public void onAction(String action, boolean pressed, float f) {
			if(action.equals("Pause") && !pressed)
				running = !running;				
		}
    };
    	
	private AnalogListener analogListener = new AnalogListener() {

		@Override
		public void onAnalog(String action, float f1, float f2) {
			if(running && action.equals("Rotate"))
				obj.rotate(2, speed*f1, 3);				
		}
    };
    
    
}
