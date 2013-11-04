package org.visualheap.app;

import com.sun.jdi.ObjectReference;
import org.visualheap.debugger.NullListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                bw.write("References: " + object.referringObjects(0));
            }

            bw.close();
        } catch (IOException ex) {
            System.out.println("Error with file creation. Exiting.");
            ex.printStackTrace();
            return;
        }

        /*
         * Iterate with for-each loop
         * extract as much as possible that we can use
         * output to file
         *
         * find some good graph algorithms
         */
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
