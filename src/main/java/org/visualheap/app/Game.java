package org.visualheap.app;


import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;
import org.visualheap.world.layout.Edge;
import org.visualheap.world.layout.LayoutBuilder;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;


public class Game extends SimpleApplication {

	private static final String CLASSPATH = "build/classes/test";
	private static final String ARRAYCLASS = "debugger.testprogs.Array";
	private static final String CYCLICREFERENCE = "debugger.testprogs.CyclicReference";
	Geometry obj;
	private Boolean running = true;
	private LayoutBuilder<ObjectReference> layoutBuilder;
	private Material matBrick;

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
							= LayoutBuilder.fromObjectReferences(new Debugger(new NullListener()), initialSet, 10);
					
						game.useLayoutBuilder(layoutBuilder);
						game.start();
					}
					
				});
			}
			
		};
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, 15, listener);
		
	
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
	
		
		Graph<ObjectReference, Edge> graph = layoutBuilder.getGraph();
		Layout<ObjectReference, Edge> layout = layoutBuilder.computeLayout();
		
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
		
        keyMapping();
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
        rootNode.attachChild(obj);
	}

	/**
	 * add a box a (x, y, z)
	 * @param x
	 * @param y
	 * @param z
	 */
	private void drawBox(float x, float y, float z) {
		Box box = new Box(1,1,1);
        obj = new Geometry("Box", box );
        obj.setMaterial(matBrick);
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
