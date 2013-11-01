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

import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import java.util.*;
import java.io.PrintWriter;

/**
 * This class processes incoming JDI events and displays them
 *
 * @author Robert Field
 * @author Oliver Myerscough
 */
class DebuggerEventThread extends Thread {

    private final VirtualMachine vm;   // Running VM

    private boolean connected = true;  // Connected to VM
    private boolean vmDied = true;     // VMDeath occurred

	private DebugListener listener;
	
	private List<Breakpoint> breakpointsToAdd = new Vector<Breakpoint>();

	private ThreadReference lastBreakpointedThread;

	/**
	 * construct an event thread, with preset breakpoint.
	 * @param vm virtual machine to observe
	 * @param listener DebugListener instance to call back to
	 */
    DebuggerEventThread(VirtualMachine vm, DebugListener listener) {
        super("event-handler");
        this.vm = vm;
        this.listener = listener;
        
        setEventRequests();
    }

    /**
     * Run the event handling thread.
     * As long as we are connected, get event sets off
     * the queue and dispatch the events within them.
     */
    @Override
    public void run() {
    	
    	
    	
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                boolean shouldResume = true;
				while (it.hasNext()) {
                    shouldResume = shouldResume && handleEvent(it.nextEvent());
                }
                
                if(shouldResume) {
                	eventSet.resume();
                }
                
            } catch (InterruptedException exc) {
                // Ignore
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            }
        }
    }

	private void setBreakpoint(ReferenceType classType, Breakpoint bp) {
		Location bpLoc = null;
		try {
			for(Location loc : classType.allLineLocations()) {
				
    			System.out.println(classType.name() + " line number " + loc.lineNumber());
    			if(loc.lineNumber() == bp.getLine()) {
    				bpLoc = loc;
    			}
    		}
		} catch (AbsentInformationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(bpLoc != null) {
	    	BreakpointRequest bpReq = vm.eventRequestManager()
	    			.createBreakpointRequest(bpLoc);
	    	bpReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
	    	bpReq.enable();
		} else {
			System.err.println("couldn't set breakpoint");
			throw new RuntimeException("couldn't set breakpoint");
		}
	}


    /**
     * Create the desired event requests, and enable
     * them so that we will get events.
     */
    private void setEventRequests() {
    	
        EventRequestManager mgr = vm.eventRequestManager();

        // want all exceptions
        ExceptionRequest excReq = mgr.createExceptionRequest(null,
                                                             true, true);
        // suspend so we can step
        excReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        excReq.enable();


        ThreadDeathRequest tdr = mgr.createThreadDeathRequest();
        // Make sure we sync on thread death
        tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        tdr.enable();
        
        VMDeathRequest vmdr = mgr.createVMDeathRequest();
        vmdr.setSuspendPolicy(EventRequest.SUSPEND_ALL); // is this necessary?
        vmdr.enable();

        // stop on class prepare so we can check if the loaded class is one
        // we want to set a breakpoint in
        ClassPrepareRequest cpr = mgr.createClassPrepareRequest();
        cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        cpr.enable();

    }
   

    /**
     * Dispatch incoming events
     */
    private boolean handleEvent(Event event) {
    	boolean shouldResume = true;
        if (event instanceof ExceptionEvent) {
            exceptionEvent((ExceptionEvent)event);       
        } else if (event instanceof ClassPrepareEvent) {
            classPrepareEvent((ClassPrepareEvent)event);
        } else if (event instanceof VMStartEvent) {
            vmStartEvent((VMStartEvent)event);
        } else if (event instanceof VMDeathEvent) {
            vmDeathEvent((VMDeathEvent)event);
        } else if (event instanceof VMDisconnectEvent) {
        	System.out.println("vm disconnect");
            vmDisconnectEvent((VMDisconnectEvent)event);
        } else if (event instanceof BreakpointEvent) {
        	shouldResume = false;
            breakpointEvent((BreakpointEvent)event);
        } else if (event instanceof StepEvent) {
        	shouldResume = false;
        	stepEvent((StepEvent) event);
        } else if (event instanceof ThreadDeathEvent) {
        	threadDeathEvent((ThreadDeathEvent)event);
        } else {
            throw new Error("Unexpected event type " + event.toString());
        }
        return shouldResume;
    }

    private void threadDeathEvent(ThreadDeathEvent event) {
    	System.out.println("thread death");	
	}

	/***
     * A VMDisconnectedException has happened while dealing with
     * another event. We need to flush the event queue, dealing only
     * with exit events (VMDeath, VMDisconnect) so that we terminate
     * correctly.
     */
    private synchronized void handleDisconnectedException() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        vmDeathEvent((VMDeathEvent)event);
                    } else if (event instanceof VMDisconnectEvent) {
                        vmDisconnectEvent((VMDisconnectEvent)event);
                    }
                }
                eventSet.resume(); // Resume the VM
            } catch (InterruptedException exc) {
                // ignore
            }
        }
    }

    private void vmStartEvent(VMStartEvent event)  {
    	listener.vmStart();
    }

    private void breakpointEvent(BreakpointEvent event)  {
    	//vm.suspend(); // just to be safe
    	ThreadReference thread = event.thread();
    	lastBreakpointedThread = event.thread();
    	List<ObjectReference> objRefs = getObjectReferencesOnThreadStack(thread);
    	listener.onBreakpoint(objRefs);
    	        	
    }

	private List<ObjectReference> getObjectReferencesOnThreadStack(
			ThreadReference thread) {
		List<ObjectReference> objRefs = Collections.emptyList();
		try {
    		objRefs = new ArrayList<ObjectReference>();
			StackFrame sf = thread.frame(0);
			for(LocalVariable lv : sf.visibleVariables()) {
				Value val = sf.getValue(lv);
				if(val instanceof ObjectReference) {
					ObjectReference objRef = (ObjectReference) val;
					objRefs.add(objRef);
				}
			}
		} catch (IncompatibleThreadStateException e) {
			// means the thread was not suspended, but obviously it will be
			e.printStackTrace();
		} catch (AbsentInformationException e) {
			// if the invoked program was not compiled with full debug info,
			// this might happen
			e.printStackTrace();
		}
		return objRefs;
	}
    
    /**
     * handles a step event
     * @param event the step event
     */
    private void stepEvent(StepEvent event) {
    	
    	System.out.println("step event");
    	
    	List<ObjectReference> fromStackFrame 
    		= getObjectReferencesOnThreadStack(lastBreakpointedThread);
    	listener.onStep(fromStackFrame);
    }

    /**
     * A new class has been loaded.
     * Set watchpoints on each of its fields
     */
    private void classPrepareEvent(ClassPrepareEvent event)  {
        EventRequestManager mgr = vm.eventRequestManager();
        
        ReferenceType refType = event.referenceType();
        for(Breakpoint bp : breakpointsToAdd) {
	        if(refType.name().equals(bp.getClassName())) {
	        	System.out.println("found " + bp.getClassName());
	        	setBreakpoint(refType, bp);
	        }
        }
        
    
    }

    private void exceptionEvent(ExceptionEvent event) {
   
    }

    public void vmDeathEvent(VMDeathEvent event) {
        vmDied = true;
        listener.vmDeath();
    }

    public void vmDisconnectEvent(VMDisconnectEvent event) {
    	System.out.println("vm disconnect event");
        listener.vmDeath();
        System.out.println("called vmDeath");
        connected = false;
    }

	public void addBreakpoint(String className, int breakpointLine) {
		
		Breakpoint bp = new Breakpoint(className, breakpointLine);
		
		// TODO search already loaded classes
		breakpointsToAdd.add(bp);
		
		
	}

	private class Breakpoint {
		private int line;
	
		private String className;
		
		public Breakpoint(String className, int line) {
			this.line = line;
			this.className = className;
		}
		
		public int getLine() {
			return line;
		}

		public String getClassName() {
			return className;
		}

	}

	/**
	 * step the VM.
	 * as we create breakpoints with SUSPEND_ALL, only one breakpoint
	 * can be reached at a time. So the thread to step is the last
	 * breakpointed thread.
	 */
	public void step() {
		
		System.out.println("step eventthread");
		
		ThreadReference threadToStep = lastBreakpointedThread;
		
		// TODO step_over, step_out etc
		StepRequest stepRequest = vm.eventRequestManager()
				.createStepRequest(threadToStep, 
						StepRequest.STEP_LINE, StepRequest.STEP_OVER);
		stepRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		stepRequest.addCountFilter(1);
		stepRequest.enable();
		
		vm.resume();
	}
	
}
