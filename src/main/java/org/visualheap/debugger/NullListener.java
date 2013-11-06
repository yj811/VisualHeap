package org.visualheap.debugger;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

import java.util.List;

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

    @Override
    public void newOnBreakpoint(StackFrame sf) {
        System.out.println("NullListener: newOnBreakpoint");
    }

}