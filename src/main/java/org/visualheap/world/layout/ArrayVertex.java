package org.visualheap.world.layout;

import java.util.Collection;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.ReferenceType;
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

	@Override
	public String createInformation() {
		ReferenceType type = arrRef.referenceType();
		String result = "Type: " + type.name() + "\n";


		result += " [";

		for (Value v : arrRef.getValues()) {
			if (v != null) {
			  result += v.toString() + " , ";
			} else {
          	  result +=  " null, ";
          	}
		}
		result += "]";

		result += "\n";


		return result;		
	}
}
