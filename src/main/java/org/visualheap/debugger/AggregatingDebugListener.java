package org.visualheap.debugger;

import java.util.List;
import java.util.Vector;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

public class AggregatingDebugListener implements DebugListener {
	/**
	 * aggregates a set of @{link DebugListener}s together.
	 * Passes all events this object recieves onto it's members.
	 */
	
	
	private List<DebugListener> listeners = new Vector<DebugListener>();
	
	public AggregatingDebugListener(DebugListener... listeners) {
		for(DebugListener listener : listeners) {
			addListener(listener);
		}
	}
	
	/**
	 * Add this listener to this aggregation set.
	 * @param listener The listener to add.
	 */
	public void addListener(DebugListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove this listener to this aggregation set.
	 * @param listener The listener to remove.
	 */
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

	@Override
	public void newOnBreakpoint(StackFrame sf) {
		// TODO Auto-generated method stub
		
	}

}
