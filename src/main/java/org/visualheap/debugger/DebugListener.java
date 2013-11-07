package org.visualheap.debugger;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

import java.util.List;

public interface DebugListener {
	/**
	 * implement this interface to receive messages from the debugger
	 * @see Debugger
	 */
	
	/**
	 * called when the {@link Debugger} reaches a breakpoint.
	 */
	
    void onBreakpoint(StackFrame sf);
	
    void onStep(StackFrame sf);
	
    void vmStart();
	
    void vmDeath();
}
