/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.Driver;
import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.Stack;

public class ImportBufferManager {
    public static volatile long maxUsingBufferSize = Runtime.getRuntime().maxMemory();
    public static volatile long perUsingBufferSize = 0L;
    public static volatile long usingBufferSize = 0L;
    public static final int waitTime = 180000;
    public static final int perWaitTime = 10000;
    public static Object lock = new Object();
    public static Object perUselock = new Object();
    public static Hashtable<Integer, SoftReference<Stack<byte[]>>> cacheBuffer = new Hashtable();

    public static void setMaxUsingBufferSize(int bufferSize) {
        long buffer = bufferSize;
        maxUsingBufferSize = buffer * 1024L * 1024L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getBufferAllways(int bufferSize) {
        byte[] result;
        int retryTimes = 0;
        Error error = null;
        while (true) {
            if (retryTimes > 300) {
                throw new RuntimeException("getBufferAllways timeout", error);
            }
            ++retryTimes;
            Object object = lock;
            synchronized (object) {
                SoftReference<Stack<byte[]>> sr;
                Stack<byte[]> stack;
                if (cacheBuffer.containsKey(bufferSize) && (stack = (sr = cacheBuffer.get(bufferSize)).get()) != null && stack.size() > 0) {
                    return stack.pop();
                }
            }
            result = null;
            try {
                result = new byte[bufferSize];
            }
            catch (Error e) {
                error = e;
                Driver.writeLog("ImportBufferManager.getBufferAllways--", e);
                System.gc();
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                continue;
            }
            break;
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getBuffer(int bufferSize) throws InterruptedException {
        Throwable error = null;
        long currentTimeBef = System.currentTimeMillis();
        while (true) {
            Object object = lock;
            synchronized (object) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - currentTimeBef > 180000L) {
                    String message = error != null ? error.toString() : "get buffer timeout, current using :" + usingBufferSize + " maxUsingBufferSize:" + maxUsingBufferSize;
                    throw new RuntimeException(message);
                }
                if (usingBufferSize + (long)bufferSize <= maxUsingBufferSize) {
                    SoftReference<Stack<byte[]>> sr;
                    Stack<byte[]> stack;
                    if (cacheBuffer.containsKey(bufferSize) && (stack = (sr = cacheBuffer.get(bufferSize)).get()) != null && stack.size() > 0) {
                        usingBufferSize += (long)bufferSize;
                        return stack.pop();
                    }
                    byte[] result = null;
                    try {
                        result = new byte[bufferSize];
                    }
                    catch (Error e) {
                        error = e;
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        continue;
                    }
                    usingBufferSize += (long)bufferSize;
                    return result;
                }
                lock.wait(10000L);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releaseCatch(byte[] buffer) {
        if (buffer == null) {
            return;
        }
        Object object = lock;
        synchronized (object) {
            if (cacheBuffer.containsKey(buffer.length)) {
                SoftReference<Stack<byte[]>> sr = cacheBuffer.get(buffer.length);
                Stack<Object> stack = sr.get();
                if (stack == null) {
                    stack = new Stack();
                    stack.push(buffer);
                } else {
                    stack.push(buffer);
                }
            } else {
                Stack<byte[]> stack = new Stack<byte[]>();
                stack.push(buffer);
                SoftReference sr = new SoftReference(stack);
                cacheBuffer.put(buffer.length, sr);
            }
            lock.notify();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releaseBuffer(byte[] buffer) {
        if (buffer == null) {
            return;
        }
        Object object = lock;
        synchronized (object) {
            usingBufferSize -= (long)buffer.length;
            if (cacheBuffer.containsKey(buffer.length)) {
                SoftReference<Stack<byte[]>> sr = cacheBuffer.get(buffer.length);
                Stack<Object> stack = sr.get();
                if (stack == null) {
                    stack = new Stack();
                    stack.push(buffer);
                } else {
                    stack.push(buffer);
                }
            } else {
                Stack<byte[]> stack = new Stack<byte[]>();
                stack.push(buffer);
                SoftReference sr = new SoftReference(stack);
                cacheBuffer.put(buffer.length, sr);
            }
            lock.notifyAll();
        }
    }

    public static int perGetTotalBuffer(int bufferSize) {
        Object object = perUselock;
        synchronized (object) {
            while (true) {
                if (perUsingBufferSize + (long)bufferSize <= maxUsingBufferSize) {
                    perUsingBufferSize += (long)bufferSize;
                    return bufferSize;
                }
                try {
                    perUselock.wait(60000L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releasePerGetTotalBuffer(int bufferSize) {
        Object object = perUselock;
        synchronized (object) {
            perUsingBufferSize -= (long)bufferSize;
            perUselock.notifyAll();
        }
    }

    public static void releaseIncludeSelf() {
        cacheBuffer.clear();
    }
}

