package org.visualheap.debugger;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;


import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class NullListener implements DebugListener {


  @Override
  public void onBreakpoint(StackFrame sf) {
      System.out.println("NullListener: newOnBreakpoint");
  }
	
  @Override
	public void onStep(StackFrame sf) {
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
	public void exitedMain() {
		System.out.println("NullListener: Exited main");
	}

    protected List<ObjectReference> getObjectReferencesFromStackFrame(
			StackFrame sf) {
		List<ObjectReference> objRefs = Collections.emptyList();
		try {
    		objRefs = new ArrayList<ObjectReference>();
			
			System.out.println("Current location - " + sf.location().sourceName() + ": " + sf.location().lineNumber());
			
			for(LocalVariable lv : sf.visibleVariables()) {
				Value val = sf.getValue(lv);
				if(val instanceof ObjectReference) {
					ObjectReference objRef = (ObjectReference) val;
					objRefs.add(objRef);
				}
			}
		} catch (AbsentInformationException e) {
			// if the invoked program was not compiled with full debug info,
			// this might happen
			e.printStackTrace();
		}
		return objRefs;
	}

}
