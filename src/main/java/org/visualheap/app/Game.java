package org.visualheap.app;


import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
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
	
	private static final float WALK_SPEED = 0.3f;
	
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
	
	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();
	private Vector3f walkDirection = new Vector3f();

	
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
						
						Layout<Vertex, Edge> layout 
							= LayoutBuilder.fromObjectReferences(initialSet, 4);
					
						game.useLayout(layout);
						game.start();
					}
					
				});
			}
			
		};
		
		Debugger debugger = new Debugger(CLASSPATH, TREEREFERENCE, 19, listener);
		
	
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
	  	matBrick.setTexture("ColorMap", assetManager.loadTexture("Texture/images.jpeg"));

		
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
				6f, 1);
		player = new CharacterControl(capsuleShape, 0.05f);
		player.setJumpSpeed(20);
		player.setFallSpeed(30);
		player.setGravity(30);
		player.setPhysicsLocation(new Vector3f(0, 10, 0));

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
			right = isPressed;
		} else if (binding.equals("Up")) {
			up = isPressed;
		} else if (binding.equals("Down")) {
			down = isPressed;
		} else if (binding.equals("Jump")) {
			if (isPressed) {
				player.jump();
			}
		}
	}

	/**
	 * From JME tutorial.
	 * 
	 * This is the main event loop--walking happens here. We check in which
	 * direction the player is walking by interpreting the camera direction
	 * forward (camDir) and to the side (camLeft). The setWalkDirection()
	 * command is what lets a physics-controlled player walk. We also make sure
	 * here that the camera moves with player.
	 */
	@Override
	public void simpleUpdate(float tpf) {
		camDir.set(cam.getDirection()).multLocal(WALK_SPEED);
		camLeft.set(cam.getLeft()).multLocal(WALK_SPEED);
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

	public Material getStandardMaterial() {
		return matBrick;
	}

	public void addCollidable(Geometry child) {
		collidables.attachChild(child);
	}

	public void addNonCollidable(Geometry child) {
		rootNode.attachChild(child);
	}
    
}
