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

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;

import java.util.Map;
import java.util.List;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This program traces the execution of another program.
 * See "java Trace -help".
 * It is a simple example of the use of the Java Debug Interface.
 *
 * @author Robert Field
 */
public class Debugger {

    // Running remote VM
    private final VirtualMachine vm;
    
    private String innerClassPath = null;

	private Integer breakpointLine;

	private String className;

	private String mainArgs;

	private DebugListener listener;

    /**
     * main
     */
    public static void main(String[] args) {
        DebugListener debugListener = new TestDebugListener();
        
        String classPath = args[1];
        String className = args[2];
        int breakpointLine = Integer.parseInt(args[3]);
        
		new Debugger(classPath, className, breakpointLine, debugListener);
    }

    /**
     * Parse the command line arguments.
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
        generateTrace(new PrintWriter(System.out));
    }


    /**
     * Generate the trace.
     * Enable events, start thread to display events,
     * start threads to forward remote error and output streams,
     * resume the remote VM, wait for the final event, and shutdown.
     */
    void generateTrace(PrintWriter writer) {
    	System.out.println("tracing");
        String[] excludes = {};
		DebuggerEventThread eventThread 
        	= new DebuggerEventThread(vm, className, 
        			breakpointLine, listener);
        eventThread.start();
        vm.resume();

        /*
        // Shutdown begins when event thread terminates
        try {
            eventThread.join();
            errThread.join(); // Make sure output is forwarded
            outThread.join(); // before we exit
        } catch (InterruptedException exc) {
            // we don't interrupt
        }
        */
        writer.close();
    }

    /**
     * Launch target VM.
     * Forward target's output and error.
     */
    VirtualMachine launchTarget() {
    	System.out.println("launching " + mainArgs);
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
    LaunchingConnector findLaunchingConnector() {
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
    Map<String, Connector.Argument> connectorArguments(LaunchingConnector connector, String mainArgs) {
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        Connector.Argument mainArg =
                           (Connector.Argument)arguments.get("main");
        if (mainArg == null) {
            throw new Error("Bad launching connector");
        }
        mainArg.setValue(mainArgs);

        return arguments;
    }
}
