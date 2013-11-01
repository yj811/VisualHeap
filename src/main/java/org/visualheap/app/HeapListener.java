package org.visualheap.app;

import com.sun.jdi.ObjectReference;
import org.visualheap.debugger.NullListener;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: eleanor
 * Date: 31/10/13
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class HeapListener extends NullListener {
    @Override
    public void onBreakpoint(List<ObjectReference> fromStackFrame) {
        System.out.println("breakpoint, got "
                + fromStackFrame.size() + " object references");

        for(ObjectReference object : fromStackFrame) {
            System.out.println(object.uniqueID());
        }
    }

	@Override
	public void onStep(List<ObjectReference> fromStackFrame) {
		// TODO Auto-generated method stub
		
	}
}
