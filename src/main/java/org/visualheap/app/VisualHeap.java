package org.visualheap.app;

import java.util.List;

import org.visualheap.debugger.Breakpoint;
import org.visualheap.debugger.DebugListener;
import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;

import com.sun.jdi.StackFrame;

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
		if(args.length < 1 || args.length > 3) {
            debugger = new Debugger();
            RealGUI gui = new RealGUI(debugger);
            gui.show();
        } else {
        	String className = "";
        	String classPath = "";
        	if(args.length >= 1) {
        		classPath = args[0];
        	}
        	if(args.length >= 2) {
        		className = args[1];
        	}
            
            debugger = new Debugger();
            RealGUI gui = new RealGUI(debugger);
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
