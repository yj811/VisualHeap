package org.visualheap.debugger;

import java.util.List;

import com.sun.jdi.ObjectReference;

public class TestDebugListener extends NullListener {

	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {
		System.out.println("breakpoint, got " 
				+ fromStackFrame.size() + " object references");
	}

	@Override
	public void onStep(List<ObjectReference> fromStackFrame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void vmStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void vmDeath() {
		// TODO Auto-generated method stub
		
	}

}
