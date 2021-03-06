package org.visualheap.app;


import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import edu.uci.ics.jung.graph.Graph;
import org.visualheap.debugger.Debugger;
import org.visualheap.world.layout.Edge;
import org.visualheap.world.layout.LayoutBuilder;
import org.visualheap.world.layout.Vertex;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.jme3.light.DirectionalLight;
import com.jme3.light.AmbientLight;

/**
 *
 * See http://hub.jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_collision
 * 
 * @author oliver, kinda.
 *
 */

public class Game extends SimpleApplication implements ActionListener {
    
    public enum LAYOUT {
        FR, ISOM, KK
    }

	private static final String CLASSPATH = "build/classes/test";
    private static final String ARRAY = "debugger.testprogs.Array";
	private static final String TREEREFERENCE = "debugger.testprogs.TreeReference";
	private static final String CYCLICREFERENCE = "debugger.testprogs.CyclicReference";
	private static final String NULLREFERENCE = "debugger.testprogs.NullReference";
	private static final String TRIPLECYCLE = "debugger.testprogs.TripleCycle";
    private static final String MULTITYPES = "debugger.testprogs.MultipleTypes";

    private static final int LINEHEIGHT = 15;
    private static final int NOKEYS = 3;
	private static final float WALK_SPEED = 1f;

    private final int FONT_SIZE = 13;
	
	private LayoutBuilder layoutBuilder;
	private BulletAppState bulletAppState;
	private CharacterControl player;
	private Node collidables = new Node();
	private Node nonCollidables = new Node();
    private BitmapText objInfo;
    private BitmapText objMethInfo;
    private BitmapText keyInfo;
	private Geometry target;
	private Graph<Vertex, Edge> graph;

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
    private Material greenGlowMat;
	private Material magentaGlowMat;
	private Material whiteGlowMat;
	private Material yellowGlowMat;
	private Material redGlowMat;
    private Material blueGlowMat;
    private volatile boolean running;

    private HashMap<ReferenceType, ColorRGBA> typeColorHashMap;
    private Collection<ObjectReference> referencesOnStack;

    private Vertex selectedVertex;
    private Material oldMaterial;
    private Material selectedMaterial;
    
    //DEMO configuration variables
    public LAYOUT layout;
    public boolean animate;
    public Integer defaultDepth;

	// start a new game.
	/*
    public static void main(String[] args) {
		final Game game = new Game();

		DebugListener listener = new NullListener() {
			@Override
			public void onBreakpoint(final StackFrame sf) {
                game.beginGame(getObjectReferencesFromStackFrameForTest(sf), game.d);
            }
        };

        Debugger debugger = new Debugger(CLASSPATH, TREEREFERENCE, 21, listener);
        game.setDebugger(debugger);
    } */
    
    public Game() {
        super();
        running = false;
        System.out.println("G");
        layout = LAYOUT.ISOM;
        animate = false;
        defaultDepth = 3;
    }
				
    public Game(Integer d, LAYOUT l) {
        super();
        running = false;
        animate = false;
        layout = l;
        defaultDepth = d;
    }
    
    public Game(Integer d, LAYOUT l, boolean a) {
        super();
        running = false;
        animate = a;
        layout = l;
        defaultDepth = d;
    }

    public void beginGame(final Collection<ObjectReference> initialSet, Debugger debugger) {
        setDebugger(debugger);
        running = true;
		final ExecutorService es =  Executors.newCachedThreadPool();
		/*
		 * If the game is started in this thread it blocks the event-thread
		 * Perhaps we should start all event handlers in their own threads?
		 */
		es.execute(new Runnable() {
			@Override
			public void run() {
			    setReferencesOnStack(initialSet);
                setShowSettings(false);
                AppSettings settings = new AppSettings(true);
                settings.setResolution(800,600);                 
                setSettings(settings);
                start();
			}
		});
		
		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.MICROSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void setDebugger(Debugger debugger) {
		this.d = debugger;
	}
	
	private void setReferencesOnStack(Collection<ObjectReference> referencesOnStack) {
	    this.referencesOnStack = referencesOnStack;
	}

	/**
	 * Executed by JME when the game starts up.
	 * Adds a box at each of the vertices specified by the layout.
	 */
	@Override
	public void simpleInitApp() {
		// hide FPS/object statistics
		setDisplayFps(false);
		setDisplayStatView(false);

        greenGlowMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        greenGlowMat.setBoolean("UseMaterialColors", true);
        greenGlowMat.setColor("Diffuse",ColorRGBA.Green);
        greenGlowMat.setColor("Ambient", ColorRGBA.Green);

	    magentaGlowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	    magentaGlowMat.setColor("Color", ColorRGBA.Magenta);
	    
	    whiteGlowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	    whiteGlowMat.setColor("Color", ColorRGBA.White);

        yellowGlowMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        yellowGlowMat.setBoolean("UseMaterialColors", true);
        yellowGlowMat.setColor("Diffuse",ColorRGBA.Yellow);
        yellowGlowMat.setColor("Ambient", ColorRGBA.Yellow);

        redGlowMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        redGlowMat.setBoolean("UseMaterialColors", true);
        redGlowMat.setColor("Diffuse",ColorRGBA.Red);
        redGlowMat.setColor("Ambient", ColorRGBA.Red);

        blueGlowMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        blueGlowMat.setBoolean("UseMaterialColors", true);
        blueGlowMat.setColor("Diffuse",ColorRGBA.Blue);
        blueGlowMat.setColor("Ambient", ColorRGBA.Blue);

        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.Gray);
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(1.8f, -2.8f, 1.0f).normalizeLocal());
        rootNode.addLight(dl);

        typeColorHashMap = new HashMap<ReferenceType, ColorRGBA>();

        // Turn off culling, so lines don't disappear at random.
        rootNode.setCullHint(CullHint.Never);

	    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
	    BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);        
	    fpp.addFilter(bloom);
	    viewPort.addProcessor(fpp);

		// will hold all collidable objects.
		rootNode.attachChild(collidables);
		
		// will hold everything else
		rootNode.attachChild(nonCollidables);
	
        // some initial physics setup
	    bulletAppState = new BulletAppState();
	    stateManager.attach(bulletAppState);

		constructWorld();

		layoutBuilder = LayoutBuilder.fromObjectReferences(referencesOnStack, this, defaultDepth);
		
		setupCrossHairs();
		setupPlayer();
        setupKeys();

        setKeyInfo("Key:", ColorRGBA.White, 0);
        setKeyInfo("Stack Frame", ColorRGBA.Blue, LINEHEIGHT * 1);
        setKeyInfo("Null Reference", ColorRGBA.Red, LINEHEIGHT * 2);
        //setKeyInfo("Static Reference", ColorRGBA.Green, LINEHEIGHT * 3);
        setKeyInfo("Unfollowed Reference", ColorRGBA.Yellow, LINEHEIGHT * 3);

        // create a collision shape for all collidable objects
        CollisionShape world = CollisionShapeFactory.createDynamicMeshShape(collidables);
        bulletAppState.getPhysicsSpace().add(new RigidBodyControl(world, 0));
	}

	// from JME tutorial, create a cross to help user determine target.
	private void setupCrossHairs() {
		 setDisplayStatView(false);
		 guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		 BitmapText ch = new BitmapText(guiFont, false);
		 ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		 ch.setText("+"); 
		 ch.setLocalTranslation(
                 settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
		 guiNode.attachChild(ch);		
	}

	private void setupPlayer() {
		// setup the player's collision boundary + some parameters
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 10f, 1);
		player = new CharacterControl(capsuleShape, 0.05f);
		player.setJumpSpeed(0);
		player.setFallSpeed(0);
		cam.lookAt(new Vector3f(10f, 0f, 10f), camUp);
		player.setPhysicsLocation(new Vector3f(0, 4, 0));

		// adding an object to the physics space makes it collidable
		bulletAppState.getPhysicsSpace().add(player);
	}

	private void constructWorld() {
	    // put the vertices in the world.
	    // layoutBuilder.displayGraph(this);
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
		inputManager.addMapping("Select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, "Left");
		inputManager.addListener(this, "Right");
		inputManager.addListener(this, "Up");
		inputManager.addListener(this, "Down");
		inputManager.addListener(this, "Rise");
		inputManager.addListener(this, "Sink");
		inputManager.addListener(this, "Quit");
		inputManager.addListener(this, "Select");
	}
	
	/**
	 * Called when an Action occurs.
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
        	
            //This should remain the same
            running = false;
        	this.stop();
        } else if (binding.equals("Select") && !isPressed) {
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            collidables.collideWith(ray, results);
            if (objInfo != null) guiNode.detachChild(objInfo);
            if (objMethInfo != null) guiNode.detachChild(objMethInfo);
            if (results.size() > 0) {
            	// pick the closest object as the target
                target = results.getClosestCollision().getGeometry();
                Vertex vertex = target.getUserData("vertex");
                	
                if(vertex != null) {
                	// tell the associated vertex it was clicked.
	                vertex.select(this);
                }
            }
        }
	}

	public void setObjInfo(String info) {
		objInfo = new BitmapText(guiFont, false);
        objInfo.setBox(new Rectangle(0, 0, 400, 400));
		objInfo.setSize(FONT_SIZE);
		objInfo.setColor(ColorRGBA.Yellow);
		objInfo.setText(info);
        //objInfo.setLocalTranslation(250, 580, 0);
        objInfo.setLocalTranslation(10, 580, 0);
		guiNode.attachChild(objInfo);
	}

    public void setObjMethInfo(String info) {
        /*objMethInfo = new BitmapText(guiFont, false);
        objMethInfo.setBox(new Rectangle(0, 0, 230, 400));
        objMethInfo.setSize(FONT_SIZE);
        objMethInfo.setColor(ColorRGBA.Yellow);
        objMethInfo.setText(info);
        objMethInfo.setLocalTranslation(10, 580, 0);
        guiNode.attachChild(objMethInfo);
        */
    }

    public void setKeyInfo(String info, ColorRGBA color, int offset) {
        keyInfo = new BitmapText(guiFont, false);
        keyInfo.setBox(new Rectangle(0, 0, 230, 400));
        keyInfo.setSize(FONT_SIZE);
        keyInfo.setColor(color);
        keyInfo.setText(info);
        keyInfo.setAlignment(BitmapFont.Align.Right);
        keyInfo.setLocalTranslation(550, 580 - offset, 0);
        guiNode.attachChild(keyInfo);
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
	    layoutBuilder.stepLayoutAlgorithm();
	    
		camDir.set(cam.getDirection()).multLocal(WALK_SPEED);
		camLeft.set(cam.getLeft()).multLocal(WALK_SPEED);
		camUp.set(cam.getUp()).multLocal(WALK_SPEED);
		walkDirection.set(0, 0, 0);

		if (left)
			walkDirection.addLocal(camLeft);
		if (right)
			walkDirection.addLocal(camLeft.negate());
		if (up)
			walkDirection.addLocal(camDir);
		if (down)
			walkDirection.addLocal(camDir.negate());
		if (rise)
            walkDirection.addLocal(camUp);
		if (sink)
            walkDirection.addLocal(camUp.negate());

		player.setWalkDirection(walkDirection);
		cam.setLocation(player.getPhysicsLocation());
		
		layoutBuilder.stepLayoutAlgorithm();
	}
	
	public Material getMagentaGlowMaterial() {
		return magentaGlowMat;
	}
	
	public Material getWhiteGlowMaterial() {
		return whiteGlowMat;
	}

	public Material getYellowGlowMaterial() {
		return yellowGlowMat;
	}

	public Material getRedGlowMaterial() {
		return redGlowMat;
	}

    public Material getBlueGlowMaterial() {
        return blueGlowMat;
    }

    public HashMap getTypeColorHashMap() {
        return typeColorHashMap;
    }

    public Material createNewMaterial(ReferenceType type) {
        ColorRGBA color = ColorRGBA.randomColor();
        Random rand = new Random();

        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        float a = 1;

        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors",true);

        // Could be improved to check for colours similar to used colours too
        while (isUsedColor(color)) {
            color = ColorRGBA.randomColor();
        }

        material.setColor("Ambient", color);
        material.setColor("Diffuse", color);

        //material.setColor("Color", color);
        setKeyInfo(type.name(), color, (1 + typeColorHashMap.size() + NOKEYS) * LINEHEIGHT);

        typeColorHashMap.put(type, color);
        return material;
    }

    public boolean isUsedColor(ColorRGBA color) {
        return typeColorHashMap.containsValue(color) || color.equals(ColorRGBA.Blue) || color.equals(ColorRGBA.Red)
                || color.equals(ColorRGBA.White) || color.equals(ColorRGBA.Magenta)
                || color.equals(ColorRGBA.Yellow);
    }

    public Material getMaterial(ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors",true);
        material.setColor("Ambient", color);
        material.setColor("Diffuse", color);

        return material;
    }

    public Vertex getSelectedVertex() {
        return selectedVertex;
    }

    public void removeSelectedVertex() {
        selectedVertex.setMaterial(oldMaterial);
        selectedVertex = null;
    }

    public void setSelectedVertex(Vertex v) {
        if (selectedVertex != null) {
            selectedVertex.setMaterial(oldMaterial);
        }

        ColorRGBA color = ColorRGBA.White;

        oldMaterial = v.getMaterial();
        selectedMaterial = oldMaterial.clone();
        selectedMaterial.setColor("GlowColor", color);

        selectedVertex = v;
        selectedVertex.setMaterial(selectedMaterial);
    }
	
	public void addCollidable(Geometry child) {
		collidables.attachChild(child);
	}

	public void addNonCollidable(Geometry child) {
		nonCollidables.attachChild(child);
	}
	
	public void addGridSquare() {
	    Quad q = new Quad(2000,2000);
        Geometry blueq = new Geometry("Quad", q);
        blueq.setLocalTranslation(new Vector3f(-1000, -5f,-1000));
        blueq.rotate( 270*FastMath.DEG_TO_RAD , 270*FastMath.DEG_TO_RAD  , 0f);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.White);
        
        Texture quadTexture = assetManager.loadTexture("textures/floor.png");
        quadTexture.setWrap(Texture.WrapMode.Repeat);
        q.scaleTextureCoordinates(new Vector2f(500,500));
        mat1.setTexture("ColorMap", quadTexture);
        
        blueq.setMaterial(mat1);   
        collidables.attachChild(blueq);
	}

    /**
     * Returns whether this game object is currently running
     * @return returns true if the game is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Called when the debugger state has changed, so that the window can be updated,
     * without creating a new one.
     * Currently left unimplemented.
     * @param object The object which should represent the changes to the heap
     */
    public void sync(Object object) {
        //TODO: given an object representing the updates to the heap, update the user view.
    }

    public void removeCollidable(Geometry geo) {
        collidables.detachChild(geo);
    }
    
    public void removeNonCollidable(Geometry geo) {
        nonCollidables.detachChild(geo);
    }
}
