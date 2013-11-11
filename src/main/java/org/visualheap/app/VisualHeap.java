package org.visualheap.app;

import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;
import org.visualheap.app.MainGUI;

/**
 * Created with IntelliJ IDEA.
 * User: eleanor
 * Date: 31/10/13
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public class VisualHeap {
    private static Debugger debugger;

    /**
     * main
     */
    public static void main(String[] args) {
        DebugListener debugListener = new HeapListener();
        debugger = null;
		if(args.length < 2 && args.length > 3) {
            debugger = new Debugger(debugListener);
            TestGUI gui = new TestGUI(debugger);
            gui.show();
        } else {
            String classPath = args[0];
            String className = args[1];
            debugger = new Debugger(debugListener);
            TestGUI gui = new TestGUI(debugger);
            if (args.length == 3) {
                Integer breakpointLine = 0;
                try {
                    breakpointLine = Integer.parseInt(args[2]);
                } catch(NumberFormatException e) {
                    usage();
                    return;
                }
                gui.addBreakpoint(breakpointLine, className);
            }
            gui.show(classPath, className);
            
        }
		
  
	}

    private static void usage() {
        System.out.println("usage: java -jar debugger.jar"
                + " <classPath> <className> <breakpointLine>");
    }
}
