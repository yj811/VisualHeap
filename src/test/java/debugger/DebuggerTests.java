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
	
	private final String CLASSPATH = "build/classes/test";
	private final String ARRAYCLASS = "debugger.testprogs.Array";
	
	
	
	@Test(timeout = defaultTimeout)
	public void CanStartJVM() {
		new Debugger(CLASSPATH, ARRAYCLASS, 12, new NullListener());
	}
	
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpoint() throws InterruptedException {
		

		LatchingDebugListener listener = new LatchingDebugListener();
		new Debugger(CLASSPATH, ARRAYCLASS, 15, listener);
		
		listener.getResult();
	
		
	}
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpointAt12() throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		
		new Debugger(CLASSPATH, ARRAYCLASS, 12, listener);
		
		assertEquals(1, listener.getResult());
		
		
	}
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpointAt14() throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		
		new Debugger(CLASSPATH, ARRAYCLASS, 14, listener);
		
		assertEquals(2, listener.getResult());
		
	}
	
	@Test(timeout = defaultTimeout)
	public void ArrayReachesBreakpointAt17() throws InterruptedException {
		
		LatchingDebugListener listener = new LatchingDebugListener();
		
		new Debugger(CLASSPATH, ARRAYCLASS, 17, listener);
		
		assertEquals(3, listener.getResult());
		
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
