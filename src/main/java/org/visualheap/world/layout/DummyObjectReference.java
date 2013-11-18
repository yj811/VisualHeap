package org.visualheap.world.layout;

import java.util.List;
import java.util.Map;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

final class DummyObjectReference implements ObjectReference {
	@Override
	public Type type() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine virtualMachine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disableCollection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableCollection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int entryCount() throws IncompatibleThreadStateException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Value getValue(Field arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Field, Value> getValues(List<? extends Field> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value invokeMethod(ThreadReference arg0, Method arg1,
			List<? extends Value> arg2, int arg3)
			throws InvalidTypeException, ClassNotLoadedException,
			IncompatibleThreadStateException, InvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCollected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ThreadReference owningThread()
			throws IncompatibleThreadStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReferenceType referenceType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ObjectReference> referringObjects(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(Field arg0, Value arg1)
			throws InvalidTypeException, ClassNotLoadedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long uniqueID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ThreadReference> waitingThreads()
			throws IncompatibleThreadStateException {
		// TODO Auto-generated method stub
		return null;
	}
}