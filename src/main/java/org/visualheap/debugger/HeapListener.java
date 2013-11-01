package org.visualheap.debugger;

import com.sun.jdi.ObjectReference;

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
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

	@Override
	public void onStep(List<ObjectReference> fromStackFrame) {
		// TODO Auto-generated method stub
		
	}
}
