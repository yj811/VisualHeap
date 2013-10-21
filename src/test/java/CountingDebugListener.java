import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import org.visualheap.debugger.DebugListener;

import com.sun.jdi.ObjectReference;


public class CountingDebugListener implements DebugListener {

	private int count;

	public CountingDebugListener(int count) {
		this.count = count;
	}
	
	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {
		System.out.println("count " + count + " size " + fromStackFrame.size());
		assertTrue(false);
		assertEquals("The stack frame should contain " + count + " object references.",
				fromStackFrame.size(), count);
	}

}
