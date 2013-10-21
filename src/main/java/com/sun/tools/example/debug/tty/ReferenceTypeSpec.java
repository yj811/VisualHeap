/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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
import com.sun.jdi.request.ClassPrepareRequest;

interface ReferenceTypeSpec {
    /**
     * Does the specified ReferenceType match this spec.
     */
    boolean matches(ReferenceType refType);
    ClassPrepareRequest createPrepareRequest();

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);
}
