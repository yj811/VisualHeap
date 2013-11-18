package org.visualheap.app;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Game extends SimpleApplication {

	Geometry obj;
	private Boolean running = true;
	
	// start a new game.
	public static void main(String[] args) {
		Game game = new Game();
		game.start();
    }
	
	// load objects before game starts.
	@Override
	public void simpleInitApp() {
		
        Box box = new Box(1,1,1);
        obj = new Geometry("Box", box );
        Material mat_brick = new Material( 
            assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap", 
            assetManager.loadTexture("Texture/images.jpeg"));
        obj.setMaterial(mat_brick);
        obj.setLocalTranslation(2.0f,-2.5f,0.0f);
        // make obj visible on scene.
        rootNode.attachChild(obj);
        keyMapping();
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
