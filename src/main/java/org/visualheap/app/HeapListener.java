package org.visualheap.app;

import com.sun.jdi.*;
import org.visualheap.debugger.NullListener;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Creation Details.
 * User: eleanor
 * Date: 31/10/13
 * Time: 15:24
 */
public class HeapListener extends NullListener {
    @Override
    public void newOnBreakpoint(StackFrame sf) {
        System.out.println("HeapListener breakpoint, got object references");

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
            }
        }
        
        
        writer.println("-------- Heap Listener Output --------");
        writer.println("");

        try {
            for(LocalVariable lv : sf.visibleVariables()) {
                Value val = sf.getValue(lv);
                if(val instanceof ObjectReference) {
                    // Object reference
                    writer.println("ObjectReference");
                    writer.println("    ID:         " + val.hashCode());
                    writer.println("    TypeName:   " + lv.typeName());
                    writer.println("    Val:        " + val.toString());
                    //System.out.println("ObjectReference found: " + lv.typeName() + " " + val.toString());
                } else {
                    writer.println("LocalVariable");
                    writer.println("    ID:         " + val.hashCode());
                    writer.println("    TypeName:   " + lv.typeName());
                    writer.println("    Val:        " + val.toString());
                    //System.out.println("LocalVariable found: " + lv.typeName() + " "  + val.toString());
                }
            }
        } catch (AbsentInformationException e) {
            // if the invoked program was not compiled with full debug info,
            // this might happen
            e.printStackTrace();
        }

        writer.flush();
        System.out.println("HeapListener: newOnBreakpoint finished");
    }

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
