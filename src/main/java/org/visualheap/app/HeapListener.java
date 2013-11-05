package org.visualheap.app;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import org.visualheap.debugger.NullListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        /*try {
            File file = new File("/out/heaptext.txt");

            if(!file.exists()) {
                boolean success = file.mkdirs();
                if(success) {
                    System.out.println("in file does not exist");
                    file.createNewFile();
                } else {
                    throw new IOException();
                }
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(ObjectReference object : fromStackFrame) {
                bw.write("Unique ID: " + object.uniqueID());
                bw.newLine();
                bw.write("Hash Code: " + object.hashCode());
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
                 */ /*
                ReferenceType objectType = object.referenceType();
                List<Field> fields = objectType.allFields();

                for (Field f : fields) {
                    Value v = object.getValue(f);
                    if (v instanceof ObjectReference) {
                        bw.write(v.toString());
                    }
                }

            }

            bw.close();
        } catch (IOException ex) {
            System.out.println("Error with file creation. Exiting.");
            ex.printStackTrace();
            return;
        }*/

        for(ObjectReference object : fromStackFrame) {
            System.out.println("Unique ID: " + object.uniqueID());
            System.out.println("Hash Code: " + object.hashCode());
            System.out.println("References: " + object.referringObjects(0)); //inbound references

            ReferenceType objectType = object.referenceType();
            List<Field> fields = objectType.allFields();

            for (Field f : fields) {
                System.out.println("Values: ");
                Value v = object.getValue(f);
                if (v instanceof ObjectReference) {
                    System.out.println(v.toString());
                }
            }
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
