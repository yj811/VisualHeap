package org.visualheap.debugger;

import java.util.List;

import com.sun.jdi.ObjectReference;

public interface DebugListener {
	
	public void onBreakpoint(List<ObjectReference> fromStackFrame);

}
