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


package com.sun.tools.example.debug.bdi;

import com.sun.jdi.*;

public class AccessWatchpointSpec extends WatchpointSpec {

    AccessWatchpointSpec(EventRequestSpecList specs,
                         ReferenceTypeSpec refSpec, String fieldId) {
        super(specs, refSpec,  fieldId);
    }

    /**
     * The 'refType' is known to match.
     */
   @Override
    void resolve(ReferenceType refType) throws InvalidTypeException,
                                             NoSuchFieldException {
        if (!(refType instanceof ClassType)) {
            throw new InvalidTypeException();
        }
        Field field = refType.fieldByName(fieldId);
        if (field == null) {
            throw new NoSuchFieldException(fieldId);
        }
        setRequest(refType.virtualMachine().eventRequestManager()
                   .createAccessWatchpointRequest(field));
    }

   @Override
    public boolean equals(Object obj) {
        return (obj instanceof AccessWatchpointSpec) && super.equals(obj);
    }
}
