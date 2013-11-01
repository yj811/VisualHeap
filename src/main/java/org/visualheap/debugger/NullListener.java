package org.visualheap.debugger;

import java.util.List;


import com.sun.jdi.ObjectReference;

public class NullListener implements DebugListener {

	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {
		// TODO Auto-generated method stub
		
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