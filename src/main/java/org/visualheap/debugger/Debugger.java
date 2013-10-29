/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation and proper error handling, might not be present in
 * this sample code.
 */


package org.visualheap.debugger;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;
import com.sun.jdi.ReferenceType;

import java.util.LinkedList;
import java.util.Map;
import java.util.List;

import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Robert Field
 * @author oliver myerscough
 * @author aviv beeri
 */
public class Debugger {

    // Running remote VM
    private final VirtualMachine vm;
    
    private String innerClassPath = null;

	private Integer breakpointLine;

	private String className;

	private String mainArgs;

	private DebugListener listener;
	private static Debugger debugger;

    /**
     * main
     */
    public static void main(String[] args) {
        DebugListener debugListener = new TestDebugListener() {
          @Override
					public void onBreakpoint(List<ObjectReference> fromStackFrame) {
            super.onBreakpoint(fromStackFrame);
						debugger.resume();
					}
  
				};
        
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

	/**
     * Launch target VM.
     * Generate the trace.
     */
    public Debugger(String classPath, String className,
    		int breakpointLine, DebugListener listener) {
    	
    	this.listener = listener;
    	this.innerClassPath = classPath;
    	this.className = className;
    	this.breakpointLine = breakpointLine;
    	
        
        
        StringBuffer sb = new StringBuffer();
        
    	sb.append("-cp");
    	sb.append(" ");
    	sb.append(innerClassPath);
    	sb.append(" ");
    	sb.append(className);
    	
        
        mainArgs = sb.toString();
        className = mainArgs.substring(mainArgs.lastIndexOf(" ") + 1);
        System.out.println(mainArgs);
        System.out.println(className);
        
        vm = launchTarget();
        startEventThread();
    }


    /**
     * starts the event thread
     * resumes VM
     */
    private void startEventThread() {
		DebuggerEventThread eventThread 
        	= new DebuggerEventThread(vm, className, 
        			breakpointLine, listener);
        eventThread.start();
        vm.resume();

    }

    /**
     * Launch target VM.
     */
    private VirtualMachine launchTarget() {
        LaunchingConnector connector = findLaunchingConnector();
        Map<String, Connector.Argument> arguments =
           connectorArguments(connector, mainArgs);
        try {
            return connector.launch(arguments);
        } catch (IOException exc) {
            throw new Error("Unable to launch target VM: " + exc);
        } catch (IllegalConnectorArgumentsException exc) {
            throw new Error("Internal error: " + exc);
        } catch (VMStartException exc) {
            throw new Error("Target VM failed to initialize: " +
                            exc.getMessage());
        }
    }

    /**
     * Find a com.sun.jdi.CommandLineLaunch connector
     */
    private LaunchingConnector findLaunchingConnector() {
        List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
        for (Connector connector : connectors) {
            if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
                return (LaunchingConnector)connector;
            }
        }
        throw new Error("No launching connector");
    }

    /**
     * Return the launching connector's arguments.
     */
    private Map<String, Connector.Argument> connectorArguments(LaunchingConnector connector, String mainArgs) {
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        Connector.Argument mainArg =
                           (Connector.Argument)arguments.get("main");
        if (mainArg == null) {
            throw new Error("Bad launching connector");
        }
        mainArg.setValue(mainArgs);

        return arguments;
    }

    /**
     * returns a list of objects referenced by the given object
     * @param objRef object to get references for
     */
	public List<ObjectReference> getObjectReferences(ObjectReference objRef) {
		// Return a list of objects referenced by the given object
	    
	    List<ObjectReference> resultList = new LinkedList<ObjectReference>();
	    
	    ReferenceType type = objRef.referenceType();
	    List<Field> fields = type.fields();
	    
	    for (Field f : fields) {
	        Value v = objRef.getValue(f);
	        if (v instanceof ObjectReference) {
	            resultList.add((ObjectReference)v);
	        }
	    }
	    
		return resultList;
	}

	/**
	 * resume the VM
	 */
	public void resume() {
		vm.resume();
	}
}
