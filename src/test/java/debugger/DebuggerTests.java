package debugger;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;

import com.sun.jdi.ObjectReference;


public class DebuggerTests {
	
	
	private static final long defaultTimeout = 1000;


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

	
	
	@Test(timeout = defaultTimeout)
	public void CanStartJVM() {
		new Debugger(CLASSPATH, ARRAYCLASS, 12, new NullListener());
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
	
	
	class NullListener implements DebugListener {

		@Override
		public void onBreakpoint(List<ObjectReference> fromStackFrame) {
			// do nothing
		}
		
	}
	
	class WaitingListener implements DebugListener {
		
		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void onBreakpoint(List<ObjectReference> fromStackFrame) {
			latch.countDown();
		}
		
		public void complete() throws InterruptedException {
			latch.await();
		}
		
		
	}
	


}
