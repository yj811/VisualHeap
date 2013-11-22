package org.visualheap.app;


import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;
import org.visualheap.world.layout.Edge;
import org.visualheap.world.layout.LayoutBuilder;
import org.visualheap.world.layout.Vertex;

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
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
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
	private static final String TREEREFERENCE = "debugger.testprogs.TreeReference";
	
	private static final float WALK_SPEED = 0.5f;
	
	private Layout<Vertex, Edge> layout;
	private Material matBrick;
	private BulletAppState bulletAppState;
	private CharacterControl player;
	private Node collidables;

	// are left/right/up/down keys pressed?
	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;
	private boolean rise;
	private boolean sink;
	
	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();
	private Vector3f camUp = new Vector3f();
	private Vector3f walkDirection = new Vector3f();
	private Debugger d;

	// start a new game.
	public static void main(String[] args) {
		final Game game = new Game();

		DebugListener listener = new NullListener() {
			
			
			@Override
			public void onBreakpoint(final StackFrame sf) {
				
				final ExecutorService es =  Executors.newCachedThreadPool();
				/*
				 * If the game is started in this thread it blocks the event-thread
				 * Perhaps we should start all event handlers in their own threads?
				 */
				es.execute(new Runnable() {

					@Override
					public void run() {
						
						Collection<ObjectReference> initialSet = getObjectReferencesFromStackFrame(sf);
			
						Layout<Vertex, Edge> layout 
							= LayoutBuilder.fromObjectReferences(initialSet, 4);
						
						game.useLayout(layout);
						game.setShowSettings(false);
						game.start();
						
					
					}
					
				});
				
				es.shutdown();
				try {
					es.awaitTermination(1, TimeUnit.MICROSECONDS);
				} catch (InterruptedException e) {
				
					e.printStackTrace();
				}
			}
			
		};
		
		Debugger debugger = new Debugger(CLASSPATH, TREEREFERENCE, 19, listener);
		game.setDebugger(debugger);
	
    }
	
	private void setDebugger(Debugger debugger) {
		this.d = debugger;
		
	}
	
	
	private void useLayout(Layout<Vertex, Edge> layout) {
		this.layout = layout;
	}
	


	/**
	 * Executed by JME when the game starts up.
	 * Adds a box at each of the vertices specified by the layout.
	 */
	@Override
	public void simpleInitApp() {
		
		matBrick = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	  	matBrick.setTexture("ColorMap", assetManager.loadTexture("Textures/images.jpeg"));

		
		// will hold all collidable objects.
		collidables = new Node();
		rootNode.attachChild(collidables);
	
        // some initial physics setup
	    bulletAppState = new BulletAppState();
	    stateManager.attach(bulletAppState);
		
		constructWorld();
		
		setupPlayer();
		createFloor();
		
		setupLight();
        setupKeys();
        
        // create a collision shape for all collidable objects
        CollisionShape world = CollisionShapeFactory.createDynamicMeshShape(collidables);
        bulletAppState.getPhysicsSpace().add(new RigidBodyControl(world, 0));
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

	private void setupPlayer() {
		// setup the player's collision boundary + some parameters
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f,
				10f, 1);
		player = new CharacterControl(capsuleShape, 0.05f);
		player.setJumpSpeed(0);
		player.setFallSpeed(0);
		cam.lookAt(new Vector3f(10f,0f,10f), camUp);
	
		player.setPhysicsLocation(new Vector3f(0, 4, 0));

		// adding an object to the physics space makes it collidable
		bulletAppState.getPhysicsSpace().add(player);
	}

	private void createFloor() {
		// create a floor, else we fall immediately
		// plane with normal (0, 1, 0) i.e. up
		// 5 units below the origin.
		Plane floor = new Plane(new Vector3f(0, 1, 0), -5);
		PlaneCollisionShape floorShape = new PlaneCollisionShape(floor);
		PhysicsControl floorControl = new RigidBodyControl(floorShape);

		bulletAppState.getPhysicsSpace().add(floorControl);
	}

	/**
	 * Iterates through all objects in the graph, creating objects in the 3d
	 * world corresponding to each vertex and edge
	 */
	private void constructWorld() {

		Graph<Vertex, Edge> graph = layout.getGraph();

		System.out.println(graph.getVertexCount() + " objects");

		// draw the vertices
		for (Vertex vertex : graph.getVertices()) {
			vertex.createInWorld(this);
		}

		for (Edge edge : graph.getEdges()) {
			edge.createInWorld(this);
		}
		//rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
		addGridSquare();
		
	}

	/**
	 * setup key mappings. Each key press leads to an Action event, which we
	 * listen to
	 */
	private void setupKeys() {
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Rise", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("Sink", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_ESCAPE));
		inputManager.addListener(this, "Left");
		inputManager.addListener(this, "Right");
		inputManager.addListener(this, "Up");
		inputManager.addListener(this, "Down");
		inputManager.addListener(this, "Rise");
		inputManager.addListener(this, "Sink");
		inputManager.addListener(this, "Quit");
	}

	/**
	 * Called when an Action occours.
	 */
	public void onAction(String binding, boolean isPressed, float tpf) {
		if (binding.equals("Left")) {
			left = isPressed;
		} else if (binding.equals("Right")) {
			right = isPressed;
		} else if (binding.equals("Up")) {
			up = isPressed;
		} else if (binding.equals("Down")) {
			down = isPressed;
		} else if (binding.equals("Rise")) {
		    rise = isPressed;
		} else if (binding.equals("Sink")) {
		    sink = isPressed;
        } else if (binding.equals("Quit")) {
        	//TODO: Change this to our expected behaviour when the user closes the game window
        	d.resume();
        	this.stop();
        }
	}

	/**
	 * From JME tutorial.
	 * 
	 * This is the main event loop--walking happens here. We check in which
	 * direction the player is walking by interpreting the camera direction
	 * forward (camDir) and to the side (camLeft) and above (camUp) . The setWalkDirection()
	 * command is what lets a physics-controlled player walk. We also make sure
	 * here that the camera moves with player.
	 */
	@Override
	public void simpleUpdate(float tpf) {
		camDir.set(cam.getDirection()).multLocal(WALK_SPEED);
		camLeft.set(cam.getLeft()).multLocal(WALK_SPEED);
		camUp.set(cam.getUp()).multLocal(WALK_SPEED);
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
		if (rise) {
            walkDirection.addLocal(camUp);
        }
		if (sink) {
            walkDirection.addLocal(camUp.negate());
        }
		player.setWalkDirection(walkDirection);
		cam.setLocation(player.getPhysicsLocation());
		System.out.println(cam.getLocation().toString());
	}

	public Material getStandardMaterial() {
		return matBrick;
	}

	public void addCollidable(Geometry child) {
		collidables.attachChild(child);
	}

	public void addNonCollidable(Geometry child) {
		rootNode.attachChild(child);
	}
	
	public void addGridSquare() {
	    Quad q = new Quad(2000,2000);
        Geometry blueq = new Geometry("Quad", q);
        blueq.setLocalTranslation(new Vector3f(-1000, -1.2f,-1000));
        blueq.rotate( 270*FastMath.DEG_TO_RAD , 270*FastMath.DEG_TO_RAD  , 0f);
        Material mat1 = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
        
        mat1.setColor("Color", ColorRGBA.White);
        Texture quadTexture = assetManager.loadTexture(
                "Textures/grid.jpg");
        quadTexture.setWrap(Texture.WrapMode.Repeat);
        q.scaleTextureCoordinates(new Vector2f(500,500));
        mat1.setTexture("ColorMap", quadTexture);
       
        blueq.setMaterial(mat1);   
        rootNode.attachChild(blueq);
	
	}

}
