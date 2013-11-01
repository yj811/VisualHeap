package org.visualheap.debugger;

import java.util.List;


import com.sun.jdi.ObjectReference;

public class NullListener implements DebugListener {

	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {
		System.out.println("NullListener: onBreakpoint");
	}

	@Override
	public void onStep(List<ObjectReference> fromStackFrame) {
		System.out.println("NullListener: onStep");
	}

	@Override
	public void vmStart() {
		System.out.println("NullListener: vmStart");
	}

	@Override
	public void vmDeath() {
		System.out.println("NullListener: vmDeath");
	}
	
}