package org.visualheap.debugger;

public class NoLineForBreakpointException extends RuntimeException {


	private static final long serialVersionUID = 1L;
	// means you tried to set a breakpoint at a line that 
	// doesn't exist / has no executable code on it
}
