/*
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
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


package com.sun.tools.example.debug.tty;

import com.sun.jdi.*;
import com.sun.jdi.request.*;

class AccessWatchpointSpec extends WatchpointSpec {

    AccessWatchpointSpec(ReferenceTypeSpec refSpec, String fieldId)
                                  throws MalformedMemberNameException {
        super(refSpec, fieldId);
    }

    /**
     * The 'refType' is known to match, return the EventRequest.
     */
    @Override
    EventRequest resolveEventRequest(ReferenceType refType)
                                      throws NoSuchFieldException {
        Field field = refType.fieldByName(fieldId);
        EventRequestManager em = refType.virtualMachine().eventRequestManager();
        EventRequest wp = em.createAccessWatchpointRequest(field);
        wp.setSuspendPolicy(suspendPolicy);
        wp.enable();
        return wp;
    }

    @Override
    public String toString() {
        return MessageOutput.format("watch accesses of",
                                    new Object [] {refSpec.toString(),
                                                   fieldId});
    }
}
