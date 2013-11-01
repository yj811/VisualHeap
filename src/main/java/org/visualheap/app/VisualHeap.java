package org.visualheap.app;

import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;

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

        if(args.length != 3) {
            usage();
        } else {

            String classPath = args[0];
            String className = args[1];
            int breakpointLine;
            try {
                breakpointLine = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                usage();
                return;
            }

            debugger = new Debugger(classPath, className, breakpointLine, debugListener);
        }
    }

    private static void usage() {
        System.out.println("usage: java -jar debugger.jar"
                + " <classPath> <className> <breakpointLine>");
    }
}
