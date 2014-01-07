/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;
import com.sun.jdi.ReferenceType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Vector;

import java.io.InputStream;
import java.io.IOException;

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
	private VirtualMachine vm;
	private String innerClassPath = null;
	private String mainClass;
	private String mainArgs;
	private String cmdArgs;
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


		bootVM();
		addBreakpoint(className, breakpointLine);
		resume();
	}


	public Debugger(String classPath, String mainName, DebugListener listener) {

		this.listener = listener;
		this.innerClassPath = classPath;
		this.mainClass = mainName;
		bootVM();	
	}

	public Debugger(DebugListener listener) {
		this.listener = listener;
	}


	/**
	 * returns a list of objects referenced by the given object.
	 * @param objRef object to get references for
	 * @return list of objects referenced by given object reference
	 * @see ObjectReference
	 */
	public static final List<ObjectReference> getObjectReferences(ObjectReference objRef) {
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
	 * resume the VM from a suspended state.
	 */
	public final void resume() {
		vm.resume();
	}
	
	public final void kill() {
		try {
			if (vm != null && eventThread != null && eventThread.isConnected()) {
				vm.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final InputStream getOutput() {
		return vm.process().getInputStream();
	}
	
	public final InputStream getErrOutput() {
		return vm.process().getErrorStream();
	}

	/**
	 * Allows the debugger's target classpath can be configured, before execution.
	 *
	 */
	public void setClassPath(String classPath) {
		this.innerClassPath = classPath;
	}

	/**
	 * Allows the debugger's target classname and package can be configured, before execution.
	 *
	 */

	public void setClassName(String className) {
		this.mainClass = className;
	}
	
	/**
	 * Allows the debugger's target classname and package can be configured, before execution.
	 *
	 */

	public void setCmdArgs(String cmdArgs) {
		this.cmdArgs = cmdArgs;
	}

	/**
	 * starts the debugger's target program in a VM, and should result in the VM suspending at the VMStart event, ready for breakpoint adding.
	 */

	public void bootVM() {
		vm = launchTarget();
		startEventThread();

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
	 * @param depth StepDepth enum.
	 */
	public void step() {
		eventThread.step();
	}
	
	public void step(StepDepth depth) {
		eventThread.step(depth);
	}
	
	/**
	 * Add a listener to the events generated by this Debugger
	 * @param listener the listener to add
	 */
	public void addListener(DebugListener listener) {
		eventThread.addListener(listener);
	}

	public void await() {
		try {
			vm.process().waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Invokes getSourceCodeFilenames without any filters
	 * @return list of source code file names.
	 */
	public List<String> getSourceCodeFilenames() {
		return getSourceCodeFilenames(Collections.<String> emptyList());
	}
	
	/**
	 * @param filters regular expressions matching class names to exclude
	 * @return list of source code file names.
	 */
	public List<String> getSourceCodeFilenames(List<String> filters) {
		List<String> fileNames = new Vector<String>();		
		for(ReferenceType loadedClass : vm.allClasses()) {
			try {
				if(!matchesFilter(loadedClass.name(), filters)) {
					fileNames.addAll(loadedClass.sourcePaths(null));
				}
			} catch (AbsentInformationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fileNames;
	}
	
	public List<Location> getBreakpointableLocationsInClass(String className) {
		List<ReferenceType> classes = vm.classesByName(className);
		
		if(classes.isEmpty()) {
			return Collections.emptyList();
		} else {
			try {
				return classes.get(0).allLineLocations();
			} catch (AbsentInformationException e) {
				return Collections.emptyList();
			}
		}
		
	}

	//PRIVATE METHODS

	/**
	 * Used by getSourceCodeFilenames to perform filtering of classes.
	 * @param className candidate path
	 * @param filters regular expression filters
	 * @return whether this sourcePath matches any of the filters
	 */
	private boolean matchesFilter(String className, List<String> filters) {
		for(String filter : filters) {
			if(className.matches(filter)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * starts the event thread
	 */
	private void startEventThread() {
		eventThread = new DebuggerEventThread(vm, listener);
		eventThread.start();
		//vm.resume();
	}


	private String constructMainArguments(String className) {
		StringBuffer sb = new StringBuffer();

		sb.append("-cp");
		sb.append(" ");
		sb.append(innerClassPath);
		sb.append(" ");
		sb.append(mainClass);
		return sb.toString();
	}


	/**
	 * Launch target VM.
	 */
	private VirtualMachine launchTarget() {
		mainArgs = constructMainArguments(mainClass);
		LaunchingConnector connector = findLaunchingConnector();
		Map<String, Connector.Argument> arguments =
				connectorArguments(connector, mainArgs);
		try {
			return connector.launch(arguments);
		} catch (IOException exc) {
			throw new Error("Unable to launch target VM: " + exc);
		} catch (IllegalConnectorArgumentsException exc) {
			throw new Error("Internal error: " + exc.argumentNames());
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
		
		Connector.Argument optionArg =
				(Connector.Argument)arguments.get("options");
		Connector.Argument mainArg =
				(Connector.Argument)arguments.get("main");
		if (mainArg == null) {
			throw new Error("Bad launching connector");
		}
		if (optionArg == null) {
			throw new Error("Bad launching connector");
		}
		mainArg.setValue(mainClass + " " + cmdArgs);
		optionArg.setValue("-cp " + innerClassPath);
		return arguments;
	}


	
}
