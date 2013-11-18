package org.visualheap.app;


import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple3f;

import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;
import org.visualheap.world.layout.Edge;
import org.visualheap.world.layout.LayoutBuilder;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;

/**
 * 
 * See http://hub.jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_collision
 * 
 * @author oliver, kinda.
 *
 */

public class Game extends SimpleApplication implements ActionListener {

	private static final String CLASSPATH = "build/classes/test";
	private static final String ARRAYCLASS = "debugger.testprogs.Array";
	private static final String CYCLICREFERENCE = "debugger.testprogs.CyclicReference";
	private static final String SIMPLEREFERENCE = "debugger.testprogs.SimpleReference";
	private static final String TREEREFERENCE = "debugger.testprogs.TreeReference";
	
	private Boolean running = true;
	private LayoutBuilder<ObjectReference> layoutBuilder;
	private Material matBrick;
	private BulletAppState bulletAppState;
	private CharacterControl player;

	// start a new game.
	public static void main(String[] args) {
		final Game game = new Game();

		DebugListener listener = new NullListener() {
			
			
			@Override
			public void onBreakpoint(final StackFrame sf) {
				Executor exec =  Executors.newCachedThreadPool();
				/*
				 * If the game is started in this thread it blocks the event-thread
				 * Perhaps we should start all event handlers in their own threads?
				 */
				exec.execute(new Runnable() {

					@Override
					public void run() {
						
						Collection<ObjectReference> initialSet = getObjectReferencesFromStackFrame(sf);
						
						LayoutBuilder<ObjectReference> layoutBuilder 
							= LayoutBuilder.fromObjectReferences(new Debugger(new NullListener()), initialSet, 4);
					
						game.useLayoutBuilder(layoutBuilder);
						game.start();
					}
					
				});
			}
			
		};
		
		Debugger debugger = new Debugger(CLASSPATH, TREEREFERENCE, 19, listener);
		
	
    }
	
	
	private void useLayoutBuilder(
			LayoutBuilder<ObjectReference> layoutBuilder) {
		this.layoutBuilder = layoutBuilder;
	}

	/**
	 * Executed by JME when the game starts up.
	 * Adds a box at each of the vertices specified by the layout.
	 */
	@Override
	public void simpleInitApp() {
	
		// load materials
		matBrick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matBrick.setTexture("ColorMap", assetManager.loadTexture("Texture/images.jpeg"));
	
		
		setupGraphics();
		setupPhysics();
		setupLight();
        setupKeys();
	}

	  private void setupLight() {
		    // We add light so we see the scene
		    AmbientLight al = new AmbientLight();
		    al.setColor(ColorRGBA.White.mult(1.3f));
		    rootNode.addLight(al);
		 
		    DirectionalLight dl = new DirectionalLight();
		    dl.setColor(ColorRGBA.White);
		    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		    rootNode.addLight(dl);
	  }

	private void setupPhysics() {
		
	    bulletAppState = new BulletAppState();
	    stateManager.attach(bulletAppState);
	   // bulletAppState.getPhysicsSpace().enableDebug(assetManager);
	 
	    // setup the player's collision boundary + some parameters
	    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
	    player = new CharacterControl(capsuleShape, 0.05f);
	    player.setJumpSpeed(20);
	    player.setFallSpeed(30);
	    player.setGravity(30);
	    player.setPhysicsLocation(new Vector3f(0, 0, 0));
	
	    bulletAppState.getPhysicsSpace().add(player);
	    
	    
	    // create a floor, else we fall immediately
	    // plane with normal (0, 1, 0) i.e. up
	    // 5 units below the origin.
	    Plane floor = new Plane(new Vector3f(0, 1, 0), -5);
	    PlaneCollisionShape floorShape = new PlaneCollisionShape(floor);
	    PhysicsControl floorControl = new RigidBodyControl(floorShape);
	    	    
	    bulletAppState.getPhysicsSpace().add(floorControl);
	}


	private void setupGraphics() {
		
		Graph<ObjectReference, Edge> graph = layoutBuilder.getGraph();
		Layout<ObjectReference, Edge> layout = layoutBuilder.computeLayout();
		
		System.out.println(graph.getVertexCount() + " objects");
		
		// draw the vertices
		for(ObjectReference vertex : graph.getVertices()) {
			
			// transform a vertex into it's location according to the layout
			Point2D location = layout.transform(vertex);
			// later we could draw a box in different colours etc using 
			// the ObjectReferece from vertex.getInnerVertex()
			drawBox((float)location.getX(), 0, (float)location.getY());
		}
		
		for(Edge edge : graph.getEdges()) {
			ObjectReference from = graph.getSource(edge);
			ObjectReference to = graph.getDest(edge);
			
			Point2D fromPoint = layout.transform(from);
			Point2D toPoint = layout.transform(to);
			
			drawArrow(fromPoint, toPoint);
			
		}
	}
	
	private static Vector3f threedeeify(Point2D point) {
		// we want this on the x-z plane in JME
		return new Vector3f((float)point.getX(), 0, (float)point.getY());
	}


	private void drawArrow(Point2D fromPoint, Point2D toPoint) {
		Line line = new Line(threedeeify(fromPoint), threedeeify(toPoint));
		Geometry g = new Geometry("line", line);
		
        g.setMaterial(matBrick);
		g.setMesh(line);
		rootNode.attachChild(g);
	}

	/**
	 * add a box a (x, y, z)
	 * @param x
	 * @param y
	 * @param z
	 */
	private void drawBox(float x, float y, float z) {
		Box box = new Box(1,1,1);
        Geometry obj = new Geometry("Box", box );
        obj.setMaterial(matBrick);
        obj.setLocalTranslation(x, y, z);
        // make obj visible on scene.
        rootNode.attachChild(obj);
	}
    
    // initiate key triggers.
    private void keyMapping() {
    	inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));
    	
    	inputManager.addListener(actionListener, "Pause");
    }
    
    private ActionListener actionListener = new ActionListener() {

		@Override
		public void onAction(String action, boolean pressed, float f) {
			if(action.equals("Pause") && !pressed)
				running = !running;				
		}
    };
    
	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;
	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();
	private Vector3f walkDirection = new Vector3f();
    
    
	/**
	 * setup key mappings. Each key press leads to an Action event, which we listen to
	 */
    private void setupKeys() {
      inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
      inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
      inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
      inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
      inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
      inputManager.addListener(this, "Left");
      inputManager.addListener(this, "Right");
      inputManager.addListener(this, "Up");
      inputManager.addListener(this, "Down");
      inputManager.addListener(this, "Jump");
    }

    /** 
     * Called when an Action occours. 
     */
    public void onAction(String binding, boolean isPressed, float tpf) {
      if (binding.equals("Left")) {
        left = isPressed;
      } else if (binding.equals("Right")) {
        right= isPressed;
      } else if (binding.equals("Up")) {
        up = isPressed;
      } else if (binding.equals("Down")) {
        down = isPressed;
      } else if (binding.equals("Jump")) {
        if (isPressed) { player.jump(); }
      }
    }
   
    /**
     * From JME tutorial.
     * 
     * This is the main event loop--walking happens here.
     * We check in which direction the player is walking by interpreting
     * the camera direction forward (camDir) and to the side (camLeft).
     * The setWalkDirection() command is what lets a physics-controlled player walk.
     * We also make sure here that the camera moves with player.
     */
    @Override
      public void simpleUpdate(float tpf) {
          camDir.set(cam.getDirection()).multLocal(0.6f);
          camLeft.set(cam.getLeft()).multLocal(0.4f);
          walkDirection.set(0, 0, 0);
          if (left) {
        	  System.out.println("left");
              walkDirection.addLocal(camLeft);
          }
          if (right) {
              walkDirection.addLocal(camLeft.negate());
          }
          if (up) {
              walkDirection.addLocal(camDir);
          }
          if (down) {
              walkDirection.addLocal(camDir.negate());
          }
          player.setWalkDirection(walkDirection);
          cam.setLocation(player.getPhysicsLocation());
          System.out.println(cam.getLocation().toString());
      }
    
    
}
