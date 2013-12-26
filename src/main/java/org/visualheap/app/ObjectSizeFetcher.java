package org.visualheap.app;

/**
 * Created with IntelliJ IDEA.
 * User: Anna
 * Date: 26/12/13
 * Time: 21:56
 * To change this template use File | Settings | File Templates.
 */

import java.lang.instrument.Instrumentation;
public class ObjectSizeFetcher {
    private static volatile Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static long getObjectSize(Object obj) {
        if (instrumentation == null)
            throw new IllegalStateException("Instrumentation agent not initiated");
        return instrumentation.getObjectSize(obj);
    }
}


