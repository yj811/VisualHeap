package org.visualheap.app;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import org.visualheap.debugger.NullListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Creation Details.
 * User: eleanor
 * Date: 31/10/13
 * Time: 15:24
 */
public class HeapListener extends NullListener {
    @Override
    public void onBreakpoint(List<ObjectReference> fromStackFrame) {
        System.out.println("breakpoint, got "
                + fromStackFrame.size() + " object references");

        // try-catch block around for-each-object loop for file writing
        try {
            File file = new File("../out/heapdata.txt");
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(ObjectReference object : fromStackFrame) {
                bw.write("Unique ID: " + object.uniqueID());
                bw.newLine();
                bw.write("Hashcode: " + object.hashCode());
                bw.newLine();
                bw.write("References: " + object.referringObjects(0)); // in-bound references

                /* object.referenceType() returns the type of the object
                 * {either ClassType, ArrayType or InterfaceType}
                 * Given that we've been passed an ObjectReference
                 * it is fair to assume they will always be ClassType in this method. (?)
                 *
                 * These extend ReferenceType which has the method allFields()
                 * which returns a list of Fields -> these can be used in the getValue/getValues methods
                 *   -> good example of this in debugger.java -> getObjectReferences method
                 */
                ReferenceType objectType = object.referenceType();


            }

            bw.close();
        } catch (IOException ex) {
            System.out.println("Error with file creation. Exiting.");
            ex.printStackTrace();
            return;
        }
    }

	@Override
	public void onStep(List<ObjectReference> fromStackFrame) {
		// TODO Auto-generated method stub
		
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
