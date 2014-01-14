package org.visualheap.world.layout;

import java.util.Collection;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Value;

public class ArrayVertex extends ObjectReferenceVertex {
	
	protected ArrayReference arrRef;

	public ArrayVertex(ArrayReference ref, LayoutBuilder lb) {
		super(ref, lb);
		arrRef = ref;
	}

	@Override
	public Collection<Value> getChildren() {
		return arrRef.getValues();
	}
}
