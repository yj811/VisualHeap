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
    public void onBreakpoint(StackFrame sf) {
        System.out.println("HeapListener breakpoint, got object references");
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
        if (close) {
        	writer.close();
        } else {
        	writer.flush();
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
