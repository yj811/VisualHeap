import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;

import com.sun.jdi.ObjectReference;


public class DebuggerTests {
	
	private static String[] arrayCmd 
		= {"-cp",  "build/classes/main", "-bp", "15", "org.group.jdiTest.Array"};
	
	private static String[] testCmd(String className, Integer breakpointLine) {
		String[] cmd = {"-cp",  "build/classes/main", 
				"-bp", breakpointLine.toString(), 
				"org.group.debugger." + className};
		return cmd;
	}
	
	@Test(timeout = 20 * 1000)
	public void ArrayReachesBreakpoint() {
		
		DebugListener listener = new DebugListener() {

			@Override
			public void onBreakpoint(List<ObjectReference> fromStackFrame) {
				assertEquals("breakpoint reached", true, true);
			}
			
		};
		
		new Debugger(DebuggerTests.testCmd("Array", 15), listener);
		
		
	}
	
	@Test(timeout = 20 * 1000)
	public void ArrayReachesBreakpointAt12() {
		
		DebugListener listener = new CountingDebugListener(1);
		
		new Debugger(DebuggerTests.testCmd("Array", 12), listener);
		
		
	}
	
	@Test(timeout = 20 * 1000)
	public void ArrayReachesBreakpointAt15() {
		
		DebugListener listener = new CountingDebugListener(2);
		
		new Debugger(DebuggerTests.testCmd("Array", 15), listener);
		
		
	}
	
	@Test(timeout = 20 * 1000)
	public void ArrayReachesBreakpointAt17() {
		
		DebugListener listener = new CountingDebugListener(3);
		
		new Debugger(DebuggerTests.testCmd("Array", 18), listener);
		
		
	}
	

}
