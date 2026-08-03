/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

public class Bucket {
    private static final int count = 1000;
    private int hashValue;
    private int totalCount;
    private int totalSize;
    private int currentCount;
    private int currentSize;

    public Bucket() {
    }

    public Bucket(int hashValue, int totalSize) {
        this.hashValue = hashValue;
        this.totalSize = totalSize;
        this.totalCount = 0;
        this.currentCount = 0;
        this.currentSize = 0;
    }

    public int getHashValue() {
        return this.hashValue;
    }

    public void setHashValue(int hashValue) {
        this.hashValue = hashValue;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalSize() {
        return this.totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getCurrentCount() {
        return this.currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public int getCurrentSize() {
        return this.currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    public void increase() {
        if (this.currentCount == 1000) {
            this.currentCount = 1;
            this.currentSize = (this.currentSize + 1) % this.totalSize;
        } else {
            ++this.currentCount;
        }
        ++this.totalCount;
    }
}

