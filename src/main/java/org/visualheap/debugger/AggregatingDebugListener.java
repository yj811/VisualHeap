package org.visualheap.debugger;

import java.util.List;
import java.util.Vector;

import com.sun.jdi.ObjectReference;

public class AggregatingDebugListener implements DebugListener {
	
	private List<DebugListener> listeners = new Vector<DebugListener>();
	
	public void addListener(DebugListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(DebugListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {
		for(DebugListener listener : listeners) {
			listener.onBreakpoint(fromStackFrame);
		}
	}

	@Override
	public void onStep(List<ObjectReference> fromStackFrame) {
		for(DebugListener listener : listeners) {
			listener.onStep(fromStackFrame);
		}
	}

	@Override
	public void vmStart() {
		for(DebugListener listener : listeners) {
			listener.vmStart();
		}
	}

	@Override
	public void vmDeath() {
		for(DebugListener listener : listeners) {
			listener.vmDeath();
		}
	}

}
