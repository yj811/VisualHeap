package org.visualheap.debugger;

import java.util.List;

import com.sun.jdi.ObjectReference;

public interface DebugListener {
	
	// implement this to receive events from the debugger
	
	public void onBreakpoint(List<ObjectReference> fromStackFrame);

}
