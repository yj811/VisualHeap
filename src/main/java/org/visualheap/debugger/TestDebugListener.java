package org.visualheap.debugger;

import java.util.List;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

public class TestDebugListener extends NullListener {

	@Override
	public void onBreakpoint(StackFrame sf) {

    List<ObjectReference> frameObjects = getObjectReferencesFromStackFrameForTest(sf); 
		System.out.println("breakpoint, got " 
				+ frameObjects.size() + " object references");
	}

	@Override
	public void onStep(StackFrame sf) {
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
