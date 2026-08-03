/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.BundleActivator
 *  org.osgi.framework.BundleContext
 */
package org.xerial.snappy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xerial.snappy.SnappyLoader;
import org.xerial.snappy.SnappyNative;

public class SnappyBundleActivator
implements BundleActivator {
    public static final String LIBRARY_NAME = "snappyjava";

    public void start(BundleContext context) throws Exception {
        System.loadLibrary(System.mapLibraryName(LIBRARY_NAME));
        SnappyLoader.setApi(new SnappyNative());
    }

    public void stop(BundleContext context) throws Exception {
        SnappyLoader.setApi(null);
    }
}

