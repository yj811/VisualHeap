package org.visualheap.debugger;

import java.util.List;

import com.sun.jdi.ObjectReference;

public interface DebugListener {
	
	/**
	 * implement this interface to recieve messages from the debugger
	 * @see Debugger
	 */
	
	/**
	 * called when the {@link Debugger} reaches a breakpoint.
	 * @param fromStackFrame list visible objects on the stack at this breakpoint
	 */
	
	void onBreakpoint(List<ObjectReference> fromStackFrame);

}
