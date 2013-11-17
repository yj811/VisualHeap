package debugger;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;
import org.visualheap.debugger.StepDepth;

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;


public class DebuggerTests {
	
	
	private static final long defaultTimeout = 2000;


	private static String[] testCmd(String className, Integer breakpointLine) {
		String[] cmd = {"-cp",  "build/classes/test", 
				"-bp", breakpointLine.toString(), 
				"debugger.testprogs." + className};
		return cmd;
	}
	
	private static final String CLASSPATH = "build/classes/test";
	private static final String ARRAYCLASS = "debugger.testprogs.Array";
	private static final String SIMPLEREFERENCE = "debugger.testprogs.SimpleReference";
	private static final String NOREFERENCES = "debugger.testprogs.NoReferences";
	private static final String CYCLICREFERENCE = "debugger.testprogs.CyclicReference";
	private static final String CYCLICREFFILE = "debugger/testprogs/CyclicReference.java";

	
	
	@Test(timeout = defaultTimeout)
	public void CanStartJVM() {
		new Debugger(CLASSPATH, ARRAYCLASS, 12, new NullListener());
	}
	
	@Test(timeout = defaultTimeout)
	public void JVMTerminates() {
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, new NullListener());
		debugger.resume();
		debugger.await();
	}
	
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpoint() throws InterruptedException {
		

		CountingDebugListener listener = new CountingDebugListener();
		new Debugger(CLASSPATH, ARRAYCLASS, 15, listener);
		
		listener.getResult();
	
		
	}
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpointAt12() throws InterruptedException {
		
		CountingDebugListener listener = new CountingDebugListener();
		
		new Debugger(CLASSPATH, ARRAYCLASS, 12, listener);
		
		assertEquals(1, listener.getResult());
		
		
	}
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpointAt14() throws InterruptedException {
		
		CountingDebugListener listener = new CountingDebugListener();
		
		new Debugger(CLASSPATH, ARRAYCLASS, 14, listener);
		
		assertEquals(2, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpointAt17() throws InterruptedException {
		
		CountingDebugListener listener = new CountingDebugListener();
		
		new Debugger(CLASSPATH, ARRAYCLASS, 17, listener);
		
		assertEquals(3, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void CanAddSecondBreakpoint() throws InterruptedException {
		
		CountingDebugListener listener = new CountingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, 17, listener);
		debugger.addBreakpoint(ARRAYCLASS, 14);
		
		assertEquals(2, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void CanReachTwoBreakpoints() throws InterruptedException {
		CountingDebugListener listener = new CountingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, 17, listener);
		debugger.addBreakpoint(ARRAYCLASS, 14);
		
		// first breakpoint
		assertEquals(2, listener.getResult());
		listener.reset(); // reset the latch
		
		debugger.resume();
		
		// second breakpoint
		assertEquals(3, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void CanStep() throws InterruptedException {
		CountingDebugListener listener = new CountingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, listener);
		debugger.addBreakpoint(ARRAYCLASS, 17);
		debugger.addBreakpoint(ARRAYCLASS, 14);
	  debugger.resume();	
		// first breakpoint
		assertEquals(2, listener.getResult());
		listener.reset();
		
		debugger.step();
		
		assertEquals(3, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void CanStepIntoFunctions() throws InterruptedException {
		CountingDebugListener listener = new CountingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, listener);
		debugger.addBreakpoint(ARRAYCLASS, 17);
		debugger.resume();	
		// first breakpoint
		assertEquals(3, listener.getResult());
		listener.reset();
		
		debugger.step(StepDepth.STEP_INTO);
		
		assertEquals(0, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void CanStepOutOfFunctions() throws InterruptedException {
		CountingDebugListener listener = new CountingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, listener);
		debugger.addBreakpoint(ARRAYCLASS, 31);
		debugger.addBreakpoint(ARRAYCLASS, 19);
		debugger.resume();	
		assertEquals(2, listener.getResult()); // line 31
		listener.reset();
		
		debugger.step(StepDepth.STEP_OUT);
		
		assertEquals(3, listener.getResult()); // line 19
		
	}
	
	@Test(timeout = defaultTimeout)
	public void CanStepTwice() throws InterruptedException {
		CountingDebugListener listener = new CountingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, listener);
		debugger.addBreakpoint(ARRAYCLASS, 14);
		debugger.resume();	
		// first breakpoint
		assertEquals(2, listener.getResult());
		listener.reset();
		
		debugger.step();
		
		assertEquals(3, listener.getResult());
		
		debugger.step();
		
		assertEquals(3, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void CanStepWithNoChangeInNumberOfObjects() 
			throws InterruptedException {
		CountingDebugListener listener = new CountingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, ARRAYCLASS, 15, listener);
		
		// first breakpoint
		assertEquals(3, listener.getResult());
		listener.reset();
		
		debugger.step();
		
		assertEquals(3, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void recieveVMStartEvent() throws InterruptedException {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		new Debugger(CLASSPATH, ARRAYCLASS, 15, 
				new NullListener() {
			
			@Override 
			public void vmStart() {
				latch.countDown();
			}
			
		});
		
		latch.await();
		
	}
	
	@Test(timeout = defaultTimeout)
	public void recieveVMDeathEvent() throws InterruptedException {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		DebugListener listener = new NullListener() {
			
			@Override 
			public void vmDeath() {
				latch.countDown();
			}
			
		};
		
		Debugger debugger = new Debugger(CLASSPATH, SIMPLEREFERENCE, listener);
		debugger.resume();
		
		latch.await();
		
	}
	
  @Test(timeout = defaultTimeout)
	public void recieveExitedMainEvent() throws InterruptedException {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		// As far as I can see this is mental layout is the only way to make this work
		final Debugger debugger 
			= new Debugger(CLASSPATH, SIMPLEREFERENCE, new NullListener());
		
		DebugListener listener = new NullListener() {
			
			@Override
			public void onBreakpoint(StackFrame sf) {
				// when we hit the breakpoint just resume.
				debugger.resume();
			}
			
			@Override 
			public void exitedMain() {
				latch.countDown();
			}
			
		};
		
		debugger.addListener(listener);		
		// set a breakpoint so the MainExit event is activated.
		debugger.addBreakpoint(SIMPLEREFERENCE, 15);
		debugger.resume();

	  	System.out.println("recieveExitedMainEvent running");
		
		latch.await();
		
	}
	
	@Test(timeout = 2*defaultTimeout)
	public void SimpleReferenceTestCanRun()
			throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		
		new Debugger(CLASSPATH, SIMPLEREFERENCE, 15, listener);
		
		listener.getResult();
		
	}

	@Test(timeout = 2*defaultTimeout)
	public void SimpleReferenceTestHasCorrectStructure() 
			throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, SIMPLEREFERENCE, 15, listener);
		
		List<ObjectReference> fromStackFrame = listener.getResult();
		
		assertEquals("two references should be visible", 2, fromStackFrame.size());
		
		ObjectReference simpleRef = fromStackFrame.get(1);
		
		for(int i = 0; i < 10; i++) {			
			List<ObjectReference> references 
				= debugger.getObjectReferences(simpleRef);
			assertNotNull("getObjectReferences returned null", references);
			assertEquals("SimpleRef should contain one reference", 1, references.size());
			simpleRef = references.get(0);
		}
		
	}
	
	@Test(timeout = defaultTimeout)
	public void NoReferencesTestCanRun() 
			throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		new Debugger(CLASSPATH, NOREFERENCES, 9, listener);
		listener.getResult();
	}
	
	@Test(timeout = defaultTimeout)
	public void ObjectWithNoReferencesHasNoReferences() 
			throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, NOREFERENCES, 9, listener);
		
		List<ObjectReference> fromStackFrame = listener.getResult();
		
		assertEquals("two references should be visible", 2, fromStackFrame.size());
		
		ObjectReference noReferences = fromStackFrame.get(1);
		List<ObjectReference> emptyList 
			= debugger.getObjectReferences(noReferences);
		
		
		assertEquals(
				"NoReferences object should have no references, " + emptyList.size() + " reported",
				0, emptyList.size());
	}
	
	@Test(timeout = defaultTimeout)
	public void CyclicReferencesTestCanRun() 
			throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		new Debugger(CLASSPATH, CYCLICREFERENCE, 18, listener);
		listener.getResult();
	}
	
	@Test(timeout = defaultTimeout)
	public void CyclicReferencesLoop() 
			throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		
		Debugger debugger = new Debugger(CLASSPATH, CYCLICREFERENCE, 18, listener);
		
		List<ObjectReference> fromStackFrame = listener.getResult();
		
		assertEquals("two references should be visible", 2, fromStackFrame.size());
		
		ObjectReference firstObject = fromStackFrame.get(1);
		List<ObjectReference> objectReferences 
			= debugger.getObjectReferences(firstObject);	
		
		assertEquals(
				"CyclicReference object should have 1 reference, " 
						+ objectReferences.size() + " reported",
				1, objectReferences.size());
		
		ObjectReference other = objectReferences.get(0);
		objectReferences = debugger.getObjectReferences(other);
		
		assertEquals(
				"CyclicReference object should have 1 reference, " 
						+ objectReferences.size() + " reported",
				1, objectReferences.size());
		
		ObjectReference returnToFirst = objectReferences.get(0);
		
		assertEquals("Cyclic refererence should get back to original object",
				firstObject.uniqueID(), returnToFirst.uniqueID());
	}
	
	@Test
	public void getSourceFilenameContainsMainProgramFile() throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		Debugger debugger = new Debugger(CLASSPATH, CYCLICREFERENCE, 18, listener);
		
		listener.getResult();
		
		for(String s : debugger.getSourceCodeFilenames()) {
			System.out.println("sourcefile " + s);
		}
		
		assertTrue("getSourceCodeFilenames should return the filename of the program being debugged",
				debugger.getSourceCodeFilenames().contains(CYCLICREFFILE));
	
	}
	
	@Test
	public void getSourceFilenameFilterWorks() throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		Debugger debugger = new Debugger(CLASSPATH, CYCLICREFERENCE, 18, listener);

		listener.getResult();
		
		List<String> filters = Arrays.asList(".*java.*", ".*sun.*");
		
		List<String> justCyclic = debugger.getSourceCodeFilenames(filters);
		assertEquals("should have filtered out everything else", justCyclic.size(), 1);
		assertEquals("should give cyclic reference source file", justCyclic.get(0), CYCLICREFFILE);
		
	}
	
	@Test
	public void getBreakpointableLocationsInClass() throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		Debugger debugger = new Debugger(CLASSPATH, CYCLICREFERENCE, 18, listener);

		listener.getResult();
		List<Integer> lines = Arrays.asList(7, 8, 9, 11, 12, 13, 16, 18, 19);
		
		List<Location> locations = debugger.getBreakpointableLocationsInClass(CYCLICREFERENCE);
		
		assert(locations.size() == lines.size());
		for(Location location : locations) {
			assert(lines.contains(location.lineNumber()));
		}
		
	}
	
}
