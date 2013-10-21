/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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


package com.sun.tools.example.debug.event;

import com.sun.jdi.*;
import com.sun.jdi.event.*;

public class VMStartEventSet extends AbstractEventSet {

    private static final long serialVersionUID = -3384957227835478191L;

    VMStartEventSet(EventSet jdiEventSet) {
        super(jdiEventSet);
    }

    /**
     * Returns the initial thread of the VM which has started.
     *
     * @return a {@link ThreadReference} which mirrors the event's
     * thread in the target VM.
     */
    public ThreadReference getThread() {
        return ((VMStartEvent)oneEvent).thread();
    }

   @Override
    public void notify(JDIListener listener) {
        listener.vmStart(this);
    }
}
