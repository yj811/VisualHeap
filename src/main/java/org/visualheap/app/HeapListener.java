package org.visualheap.app;

import com.sun.jdi.*;
import org.visualheap.debugger.NullListener;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

/**
 * Creation Details.
 * User: eleanor
 * Date: 31/10/13
 * Time: 15:24
 */
public class HeapListener extends NullListener {

    @Override
    public void onBreakpoint(StackFrame sf) {
        System.out.println("HeapListener breakpoint, got stack frame");
        //if the PrintWriter points to System.out, which can happen if the writer fails,
        //make sure we don't close it.
        boolean close = true;
        PrintWriter writer = null;

        try {
            writer = new PrintWriter("out/HeapListenerOutput.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
        	if (writer == null) {
            	writer = new PrintWriter(System.out);
            	close = false;
            }
        }

        writer.println("-------- Heap Listener Output --------");
        writer.println("");

        try {
            for(LocalVariable lv : sf.visibleVariables()) {
                Value val = sf.getValue(lv);
                if(val instanceof ObjectReference) {
                    getReferences(((ObjectReference) val), writer, new HashMap<Integer, ObjectReference>());
                } else {
                    writer.println("LocalVariable");
                    writer.println("    HashCode:   " + val.hashCode());
                    //writer.println("    UniqueId:   " + ((ObjectReference) val).uniqueID());
                    writer.println("    TypeName:   " + lv.typeName());
                    writer.println("    Val:        " + val.toString());
                    writer.println("    IsArgument: " + lv.isArgument());
                    writer.println("    IsVisible:  " + lv.isVisible(sf));
                }
            }
        } catch (AbsentInformationException e) {
            // if the invoked program was not compiled with full debug info,
            // this might happen
            e.printStackTrace();
        }
        if (close) {
        	writer.close();
        } else {
        	writer.flush();
        }
    }

    public void getReferences(ObjectReference val, PrintWriter writer, HashMap<Integer, ObjectReference> prevRefs) {
        if (prevRefs.containsKey(val.hashCode())) {
            writer.println("Cyclic reference found");
            return;
        }

        ReferenceType objectType = val.referenceType();

        writer.println("ObjectReference");
        writer.println("    HashCode:   " + val.hashCode());
        //writer.println("    UniqueId:   " + ((ObjectReference) val).uniqueID());
        writer.println("    TypeName:   " + objectType.name());
        writer.println("    Val:        " + val.toString());
        //writer.println("    IsArgument: " + lv.isArgument());
        //writer.println("    IsVisible:  " + lv.isVisible(sf));

        writer.println("");
        // referringObjects(0) - Returns all objects that directly reference this object.
        if (val.referringObjects(0).size() > 0) {
        	writer.println("    References: " + val.referringObjects(0));
        	System.err.println(val.referringObjects(0));
        }
        List<Field> fields = objectType.allFields();

        prevRefs.put(val.hashCode(), val);

        for (Field f : fields) {
            writer.println("    Values: ");
            Value v = val.getValue(f);
            if (v instanceof ObjectReference) {
                writer.println("        " + v.toString());
                
                getReferences(((ObjectReference) v), writer, prevRefs);
            }
        }
    }

	@Override
	public void onStep(StackFrame sf) {
		System.out.println("HeapListener: onStep");
	}

    @Override
    public void vmStart() {
        System.out.println("HeapListener: vmStart");
    }

    @Override
    public void vmDeath() {
        System.out.println("HeapListener: vmDeath");
    }
}
