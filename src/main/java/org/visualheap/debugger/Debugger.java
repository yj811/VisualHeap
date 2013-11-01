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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.ReferenceType;

import java.util.LinkedList;
import java.util.Map;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Robert Field
 * @author oliver myerscough
 * @author aviv beeri
 */
public class Debugger {
	/**
	 * run a class in the jvm with a breakpoint set
	 * when the breakpoint is reached the onBreakpoint method of the
	 * given DebugListener will be called {see DebuggerEventThread class}
	 * @see DebugListener
	 */

    // Running remote VM
    private final VirtualMachine vm;
    private String innerClassPath = null;
	private Integer breakpointLine;
	private String mainClass;
	private String mainArgs;
	private DebugListener listener;
	private DebuggerEventThread eventThread;

	/**
     * Launch target VM with specified breakpoint
     * @param classPath classpath to class to invoke
     * @param className the class to invoke, qualified by package name etc
     * @param breakpointLine source line number to set a breakpoint at
     * @param listener DebugListener to call back to when the breakpoint 
     * is reached
     * @see DebugListener
     */
    public Debugger(String classPath, String className,
    		int breakpointLine, DebugListener listener) {
    	
    	this.listener = listener;
    	this.innerClassPath = classPath;
    	this.mainClass = className;
    	this.breakpointLine = breakpointLine;
        
        mainArgs = constructMainArguments(className);
        
        vm = launchTarget();
        startEventThread();
        
        eventThread.addBreakpoint(className, breakpointLine);
    }

	private String constructMainArguments(String className) {
		StringBuffer sb = new StringBuffer();
        
    	sb.append("-cp");
    	sb.append(" ");
    	sb.append(innerClassPath);
    	sb.append(" ");
    	sb.append(className);
		return sb.toString();
	}


    public Debugger(String classPath, String mainName, DebugListener listener) {
    	
    	this.listener = listener;
      	this.innerClassPath = classPath;
    	this.mainClass = mainName;
    	 
        mainArgs = constructMainArguments(mainClass);
        
        vm = launchTarget();
        startEventThread();
	
	}

	/**
>>>>>>> debugger
     * starts the event thread
     * resumes VM
     */
    private void startEventThread() {
		eventThread = new DebuggerEventThread(vm, listener);
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
     * returns a list of objects referenced by the given object.
     * @param objRef object to get references for
     * @return list of objects referenced by given object reference
     * @see ObjectReference
     */
	public final List<ObjectReference> getObjectReferences(ObjectReference objRef) {
	    List<ObjectReference> resultList = new LinkedList<ObjectReference>();
	    ReferenceType type = objRef.referenceType();
	    List<Field> fields = type.fields();
	    
	    for (Field f : fields) {
	        Value v = objRef.getValue(f);
	        if (v instanceof ObjectReference) {
	            resultList.add((ObjectReference) v);
	        }
	    }

		return resultList;
	}

	/**
	 * resume the VM.
	 */
	public final void resume() {
		vm.resume();
	}
	
	public final InputStream getOutput() {
		return vm.process().getInputStream();
	}
	
	/**
	 * add a breakpoint at the specified line
	 * @param className name of class to break in (qualified by package)
	 * @param breakpointLine line to add a breakpoint to
	 */
	public void addBreakpoint(String className, int breakpointLine) {
		eventThread.addBreakpoint(className, breakpointLine);
	}

	/**
	 * step the connected vm.
	 * vm must be suspended.
	 */
	public void step() {
		
		System.out.println("debugger step");
		
		eventThread.step();
	}

	public void await() {
		try {
			vm.process().waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
