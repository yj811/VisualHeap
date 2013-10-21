package org.visualheap.debugger;

import java.util.List;

import com.sun.jdi.ObjectReference;

public class TestDebugListener implements DebugListener {

	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {
		System.out.println("breakpoint, got " 
				+ fromStackFrame.size() + " object references");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
